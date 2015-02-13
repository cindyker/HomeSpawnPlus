/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
/**
 *
 */
package com.andune.minecraft.hsp;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.events.*;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.entity.PlayerLastLocation;
import com.andune.minecraft.hsp.integration.multiverse.MultiverseCore;
import com.andune.minecraft.hsp.integration.multiverse.MultiversePortals;
import com.andune.minecraft.hsp.manager.CooldownManager;
import com.andune.minecraft.hsp.manager.DeathManager;
import com.andune.minecraft.hsp.manager.EffectsManager;
import com.andune.minecraft.hsp.manager.WarmupManager;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.PlayerLastLocationDAO;
import com.andune.minecraft.hsp.strategy.EventType;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyEngine;
import com.andune.minecraft.hsp.strategy.StrategyResult;
import com.andune.minecraft.hsp.util.BedUtils;
import com.andune.minecraft.hsp.util.SpawnUtil;
import com.andune.minecraft.hsp.util.NameChangeUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author andune
 */
@Singleton
public class EventListener implements com.andune.minecraft.commonlib.server.api.event.EventListener {
    private final Logger log = LoggerFactory.getLogger(EventListener.class);
    private final Storage storage;
    private final StrategyEngine engine;
    private final ConfigCore config;
    private final Factory factory;
    private final MultiverseCore multiverseCore;
    private final MultiversePortals multiversePortals;
    private final SpawnUtil spawnUtil;
    private final BedUtils bedUtil;
    private final WarmupManager warmupManager;
    private final EffectsManager effectsManager;
    private final CooldownManager cooldownManager;
    private final DeathManager deathManager;
    private final NameChangeUtil nameChangeUtil;

    /**
     * We record the last known player/location for common events so that we can
     * later check at a MONITOR priority to see if it changed.
     * <p/>
     * This allows us to warn the admin if another plugin changed the respawn/join
     * locations to something other than what they specified in HSP.
     * <p/>
     * Note that this behavior depends on the fact that event processing is single-
     * threaded. This is true for Bukkit; if HSP is ever implemented upon a server
     * framework where this is not true, this will have to be changed.
     */
    private Player lastRespawnPlayer;
    private Location lastRespawnLocation;

    @Inject
    public EventListener(ConfigCore config, Storage storage, StrategyEngine engine, Factory factory,
                         MultiverseCore multiverseCore, MultiversePortals multiversePortals, SpawnUtil spawnUtil,
                         BedUtils bedUtil, WarmupManager warmupManager, EffectsManager effectsManager,
                         CooldownManager cooldownManager, DeathManager deathManager,
                         NameChangeUtil nameChangeUtil) {
        this.config = config;
        this.storage = storage;
        this.engine = engine;
        this.factory = factory;
        this.multiverseCore = multiverseCore;
        this.multiversePortals = multiversePortals;
        this.spawnUtil = spawnUtil;
        this.bedUtil = bedUtil;
        this.warmupManager = warmupManager;
        this.effectsManager = effectsManager;
        this.cooldownManager = cooldownManager;
        this.deathManager = deathManager;
        this.nameChangeUtil = nameChangeUtil;
    }

    @Override
    public void playerJoin(PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        final boolean isNewPlayer = p.isNewPlayer();

        if (isNewPlayer) {
            if (config.isVerboseLogging())
                log.info("New player {} detected.", p.getName());
        }

        // Try to find them by UUID first. If not found, try playerName instead
        com.andune.minecraft.hsp.entity.Player storagePlayer = storage.getPlayerDAO().findPlayerByUUID(p.getUUID());
        if (storagePlayer == null) {
            storagePlayer = storage.getPlayerDAO().findPlayerByName(p.getName());
        }

        // detect player name change
        if (storagePlayer != null && !p.getName().equals(storagePlayer.getName())) {
            final String oldName = storagePlayer.getName();
            log.info("Player {} changed names (oldName={}, uid={})", p.getName(),
                    oldName, p.getUUID());
            int count = nameChangeUtil.changeName(p.getName(), storagePlayer.getName());
            log.info("Updated {} rows in database from oldName ({}) to newName ({}) (uid={})", count, oldName, p.getName(), p.getUUID());
        }

        // if they don't have a player record yet, create one.
        if (storagePlayer == null) {
            storagePlayer = new com.andune.minecraft.hsp.entity.Player(p);
            try {
                storage.getPlayerDAO().savePlayer(storagePlayer);
            } catch (StorageException e) {
                log.warn("Caught exception writing to storage ", e);
            }
        }
        // or if they do have a player record, make sure UUID is set correctly
        else if (storagePlayer.getUUIDString() == null ||
                !storagePlayer.getUUIDString().equals(p.getUUID()) ) {
            storagePlayer.setUUIDString(p.getUUID().toString());
            try {
                storage.getPlayerDAO().savePlayer(storagePlayer);
            } catch (StorageException e) {
                log.warn("Caught exception writing to storage ", e);
            }
        }

        if (config.isVerboseLogging())
            log.info("Attempting to respawn player {} (joining).", p.getName());

        StrategyResult result = null;
        // execute NEW_PLAYER strategy if player is new. If no results are returned, this
        // will fall through to the ON_JOIN strategy instead.
        if (isNewPlayer)
            result = engine.getStrategyResult(EventType.NEW_PLAYER, p);

        // execute ON_JOIN strategy to find out where we should put the player, but only
        // if there was no result from newPlayer checks
        if (result == null || result.isExplicitDefault() || result.getLocation() == null)
            result = engine.getStrategyResult(EventType.ON_JOIN, p);

        Location joinLocation = null;
        if (result != null)
            joinLocation = result.getLocation();

        if (joinLocation != null)
            event.setJoinLocation(joinLocation);
    }

    @Override
    public void playerRespawn(PlayerRespawnEvent event) {
        long start = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            Location bedSpawn = event.getPlayer().getBedSpawnLocation();
            log.debug("onPlayerRespawn(): isBedSpawn={}, bedSpawn={}", event.isBedSpawn(), bedSpawn);
        }

        if (config.isVerboseLogging())
            log.info("Attempting to respawn player {} (respawning).", event.getPlayer().getName());

        // execute ON_DEATH strategy to find out where we should spawn the player
        Location l = engine.getStrategyLocation(EventType.ON_DEATH, event.getPlayer());
        if (l != null) {
            event.setRespawnLocation(l);
            lastRespawnLocation = l;
            lastRespawnPlayer = event.getPlayer();
        }

        int warnMillis = config.getPerformanceWarnMillis();
        if (warnMillis > 0) {
            long totalTime = System.currentTimeMillis() - start;
            if (totalTime > warnMillis) {
                log.info("**LONG RESPAWN** Respawn for player {} took {} ms to run (> warning threshold of {}ms)",
                        event.getPlayer(), totalTime, warnMillis);
            }
        }
    }

    /**
     * Method to monitor player teleports and possibly change the location
     * based on HSP strategies.
     */
    @Override
    public void playerTeleport(PlayerTeleportEvent event) {
        EventType type = null;
        // cross-world teleport event?
        if (!event.getTo().getWorld().equals(event.getFrom().getWorld())) {
            if (event.getPlayer().getName().equals(multiverseCore.getCurrentTeleporter())) {
                type = EventType.MULTIVERSE_TELEPORT_CROSSWORLD;
                log.debug("multiverse crossworld teleport detected");
            } else {
                type = EventType.CROSS_WORLD_TELEPORT;
                log.debug("crossworld teleport detected");
            }

        }
        // same-world multiVerse teleport?
        else if (multiverseCore.getCurrentTeleporter() != null) {
            type = EventType.MULTIVERSE_TELEPORT;
            log.debug("multiverse same world teleport detected");
        }

        // if this was some sort of teleport event that we should fire an HSP
        // event for, then set one up and invoke the strategy engine
        if (type != null) {
            final StrategyContext context = factory.newStrategyContext();
            context.setEventType(type.toString());
            context.setPlayer(event.getPlayer());
            context.setLocation(event.getTo()); // location involved is the target location
            context.setFromLocation(event.getFrom());

            StrategyResult result = engine.getStrategyResult(context);
            if (result != null && result.getLocation() != null)
                event.setTo(result.getLocation());
        }

        // teleport is finished, clear current teleporter
        if (multiverseCore.isEnabled()) {
            multiverseCore.setCurrentTeleporter(null);
        }
        if (multiversePortals.isEnabled()) {
            multiversePortals.setSourcePortalName(null);
            multiversePortals.setDestinationPortalName(null);
        }

        // code search shows this is no longer used, commenting for now in case
        // I find a bug later that depends on this..
//        teleport.setCurrentTeleporter(null);
    }

    /**
     * Method to monitor successful cross-world teleports and
     * update PlayerLastLocation accordingly.
     */
    @Override
    public void observePlayerTeleport(PlayerTeleportEvent event) {
        log.debug("ENTER observePlayerTeleport(): player={}, to={}, from={}",
                event.getPlayer(), event.getTo(), event.getFrom());

        // a new behavior is that we get sometimes a half dozen or more
        // observe events going "from/to" the same location. I'm not sure
        // what causes this but we want to de-dup these events so they
        // don't spam the log or cause any other unwanted behaviors.
        if (event.getTo().getWorld().equals(event.getFrom().getWorld()) &&
            event.getTo().getBlockX() == event.getFrom().getBlockX() &&
            event.getTo().getBlockY() == event.getFrom().getBlockY() &&
            event.getTo().getBlockZ() == event.getFrom().getBlockZ() ) {
            log.debug("de-duping same-location observePlayerTeleport event");
            return;
        }

        // cross-world teleport event?
        if (config.isRecordLastLocation() && !event.getTo().getWorld().equals(event.getFrom().getWorld())) {
            PlayerLastLocationDAO dao = storage.getPlayerLastLocationDAO();
            PlayerLastLocation playerLastLocation = dao.findByWorldAndPlayerName(event.getFrom().getWorld().getName(), event.getPlayer().getName());
            if (playerLastLocation == null) {
                playerLastLocation = new PlayerLastLocation();
                playerLastLocation.setPlayerName(event.getPlayer().getName());
            }
            playerLastLocation.setLocation(event.getFrom());

            try {
                dao.save(playerLastLocation);
            } catch (StorageException e) {
                log.warn("Error writing to database", e);
            }
            log.debug("Saved player {} location as {}", event.getPlayer(), playerLastLocation);
        }

        // we fire an "onTeleportObserve" event but we ignore the result,
        // because this event is not to change locations. However, this event
        // could be used with modeEffect to create teleport effects.
        final StrategyContext context = factory.newStrategyContext();
        context.setEventType(EventType.TELEPORT_OBSERVE.toString());
        context.setPlayer(event.getPlayer());
        context.setLocation(event.getTo()); // location involved is the target location
        engine.getStrategyResult(context);

        // fire any teleport effects. Since this is a MONITOR priority, we
        // count on the fact that we know the event won't be canceled and so
        // we can properly do any effects at this point.
        effectsManager.doTeleportEffects(event.getPlayer());
    }

    @Override
    public void observeRespawn(PlayerRespawnEvent event) {
        if (!config.isWarnLocationChange())
            return;

        if (lastRespawnPlayer != null && lastRespawnLocation != null) {
            // shouldn't happen, but protect from silliness in case it does
            if (!lastRespawnPlayer.equals(event.getPlayer())) {
                lastRespawnPlayer = null;
                lastRespawnLocation = null;
                return;
            }

            final Location respawnLoc = event.getRespawnLocation();

            // do manual world/x/y/z check instead of .equals() so that we avoid comparing
            // pitch/yaw and also so we round to integer blocks instead of exact double loc
            if (!lastRespawnLocation.getWorld().equals(respawnLoc.getWorld())
                    || respawnLoc.getBlockX() != lastRespawnLocation.getBlockX()
                    || respawnLoc.getBlockY() != lastRespawnLocation.getBlockY()
                    || respawnLoc.getBlockZ() != lastRespawnLocation.getBlockZ()) {
                log.info("onDeath: final player location is different than where HSP sent player, another plugin has changed the location."
                        + " Player " + lastRespawnPlayer.getName() + ", HSP location " + lastRespawnLocation.shortLocationString()
                        + ", final player location " + respawnLoc.shortLocationString());
            }

        }

        lastRespawnPlayer = null;
        lastRespawnLocation = null;
    }

    @Override
    public void bedRightClick(com.andune.minecraft.commonlib.server.api.events.PlayerBedRightClickEvent event) {
        if (!config.isBedSetHome())
            return;

        // if BED_HOME_MUST_BE_NIGHT config is set, then we ignore this click and let
        // the PlayerBedEnterEvent handler handle it instead.
        if (config.isBedHomeMustBeNight())
            return;

        // we do nothing further if the player is sneaking; this allows sneaking to override
        // all HSP home behavior, to let players actually sleep in a bed without setting their
        // homes, for example.
        if (event.getPlayer().isSneaking())
            return;

        // if we get here, we know they clicked on a bed and configs are enabled for us
        // to something with that click
        log.debug("bedRightClick: calling doBedSet for player {}", event.getPlayer());

        if (bedUtil.doBedClick(event.getPlayer(), event.getClickedBlock())) {
            // if original behavior is not enabled, then we cancel the event to avoid
            // a spurious "you can only sleep at night" message
            if (!config.isBedHomeOriginalBehavior())
                event.setCancelled(true);
        }

        // if we're never supposed to display "You can only sleep at night" message, then
        // cancel the event to avoid the message
        if (config.isBedNeverDisplayNightMessage())
            event.setCancelled(true);
    }

    @Override
    public void bedEvent(PlayerBedEnterEvent event) {
        log.debug("bedEvent: invoked");

        if (!config.isBedSetHome())
            return;

        // we do nothing further if the player is sneaking
        if (event.getPlayer().isSneaking())
            return;

        // we only handle events if BED_HOME_MUST_BE_NIGHT config is true, otherwise
        // the PlayerInteractEvent handler takes care of it.
        if (config.isBedHomeMustBeNight()) {
            log.debug("bedEvent: calling doBedSet for player ", event.getPlayer());
            if (bedUtil.doBedClick(event.getPlayer(), event.getBed()))
                event.setCancelled(true);
        }

    }

    @Override
    public void playerQuit(PlayerQuitEvent event) {
        spawnUtil.updateQuitLocation(event.getPlayer());
    }

    @Override
    public void playerKick(PlayerKickEvent event) {
        spawnUtil.updateQuitLocation(event.getPlayer());
    }

    @Override
    public void playerDamage(PlayerDamageEvent event) {
        warmupManager.processEntityDamage(event);
    }

    @Override
    public void playerDeath(PlayerDeathEvent event) {
        cooldownManager.onDeath(event.getPlayer());
        deathManager.playerDied(event.getPlayer());
    }
}
