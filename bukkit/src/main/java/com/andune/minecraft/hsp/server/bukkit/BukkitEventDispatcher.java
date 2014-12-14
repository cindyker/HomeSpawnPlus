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
package com.andune.minecraft.hsp.server.bukkit;

import com.andune.minecraft.commonlib.server.api.Server;
import com.andune.minecraft.commonlib.server.api.event.EventListener;
import com.andune.minecraft.commonlib.server.bukkit.BukkitFactory;
import com.andune.minecraft.commonlib.server.bukkit.event.BukkitEventPriority;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.config.ConfigWarmup;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Bridge class between Bukkit event system and HSP API event interface.
 *
 * @author andune
 */
@Singleton
public class BukkitEventDispatcher implements com.andune.minecraft.commonlib.server.api.event.EventDispatcher, org.bukkit.event.Listener {
    private final EventListener eventListener;
    private final Plugin plugin;
    private final BukkitFactory bukkitFactory;
    private final ConfigWarmup configWarmup;
    private final ConfigCore configCore;
    private final Server server;

    @Inject
    public BukkitEventDispatcher(EventListener listener, Plugin plugin, BukkitFactory bukkitFactory,
                                 ConfigWarmup configWarmup, ConfigCore configCore, Server server) {
        this.eventListener = listener;
        this.plugin = plugin;
        this.bukkitFactory = bukkitFactory;
        this.configWarmup = configWarmup;
        this.configCore = configCore;
        this.server = server;
    }

    /**
     * Register events with Bukkit server.
     */
    public void registerEvents() {
        // register annotated event methods
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // register events with config-defined priorities 
        plugin.getServer().getPluginManager().registerEvent(PlayerJoinEvent.class,
                this,
                getEventPriority(),
                new EventExecutor() {
                    public void execute(Listener listener, Event event) throws EventException {
                        try {
                            com.andune.minecraft.commonlib.server.api.events.PlayerJoinEvent apiEvent =
                                    new com.andune.minecraft.commonlib.server.bukkit.events.PlayerJoinEvent((PlayerJoinEvent) event, bukkitFactory, plugin, server);
                            eventListener.playerJoin(apiEvent);
                        } catch (Exception e) {
                            throw new EventException(e);
                        }
                    }
                },
                plugin);

        plugin.getServer().getPluginManager().registerEvent(PlayerRespawnEvent.class,
                this,
                getEventPriority(),
                new EventExecutor() {
                    public void execute(Listener listener, Event event) throws EventException {
                        try {
                            com.andune.minecraft.commonlib.server.api.events.PlayerRespawnEvent apiEvent =
                                    new com.andune.minecraft.commonlib.server.bukkit.events.PlayerRespawnEvent((PlayerRespawnEvent) event, bukkitFactory);
                            eventListener.playerRespawn(apiEvent);
                        } catch (Exception e) {
                            throw new EventException(e);
                        }
                    }
                },
                plugin);

        // only hook EntityDamageEvent if it's needed by warmups
        if (configWarmup.isEnabled() && configWarmup.isCanceledOnDamage()) {
            plugin.getServer().getPluginManager().registerEvent(EntityDamageEvent.class,
                    this,
                    EventPriority.MONITOR,
                    new EventExecutor() {
                        public void execute(Listener listener, Event event) throws EventException {
                            try {
                                onEntityDamage((EntityDamageEvent) event);
                            } catch (Exception e) {
                                throw new EventException(e);
                            }
                        }
                    },
                    plugin);
        }
    }

    /**
     * Return event priority as defined by the admin in the config file.
     *
     * @return
     */
    private EventPriority getEventPriority() {
        return BukkitEventPriority.convertApiToBukkit(configCore.getEventPriority());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void playerTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
        com.andune.minecraft.commonlib.server.api.events.PlayerTeleportEvent apiEvent =
                new com.andune.minecraft.commonlib.server.bukkit.events.PlayerTeleportEvent(event, bukkitFactory);
        eventListener.playerTeleport(apiEvent);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerTeleportObserver(org.bukkit.event.player.PlayerTeleportEvent event) {
        com.andune.minecraft.commonlib.server.api.events.PlayerTeleportEvent apiEvent =
                new com.andune.minecraft.commonlib.server.bukkit.events.PlayerTeleportEvent(event, bukkitFactory);
        eventListener.observePlayerTeleport(apiEvent);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block b = event.getClickedBlock();
        // did they click on a bed? short-circuit this method if not (fail-fast)
        if (b.getTypeId() != 26)
            return;

        // if we get here, it was a bed right-click event, so fire one
        com.andune.minecraft.commonlib.server.api.events.PlayerBedRightClickEvent apiEvent =
                new com.andune.minecraft.commonlib.server.bukkit.events.PlayerBedRightClickEvent(event, bukkitFactory);
        eventListener.bedRightClick(apiEvent);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBedEvent(PlayerBedEnterEvent event) {
        com.andune.minecraft.commonlib.server.api.events.PlayerBedEnterEvent apiEvent =
                new com.andune.minecraft.commonlib.server.bukkit.events.PlayerBedEnterEvent(event, bukkitFactory);
        eventListener.bedEvent(apiEvent);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        com.andune.minecraft.commonlib.server.api.events.PlayerQuitEvent apiEvent =
                new com.andune.minecraft.commonlib.server.bukkit.events.PlayerQuitEvent(event, bukkitFactory);
        eventListener.playerQuit(apiEvent);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(org.bukkit.event.player.PlayerKickEvent event) {
        com.andune.minecraft.commonlib.server.api.events.PlayerKickEvent apiEvent =
                new com.andune.minecraft.commonlib.server.bukkit.events.PlayerKickEvent(event, bukkitFactory);
        eventListener.playerKick(apiEvent);
    }

    // this event is dynamically hooked only if needed
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.isCancelled())
            return;
        if (!(event.getEntity() instanceof org.bukkit.entity.Player))
            return;

        com.andune.minecraft.commonlib.server.api.events.PlayerDamageEvent apiEvent =
                new com.andune.minecraft.commonlib.server.bukkit.events.PlayerDamageEvent((Player) event.getEntity(), bukkitFactory);
        eventListener.playerDamage(apiEvent);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if( event.getEntity() instanceof Player ) {
            com.andune.minecraft.commonlib.server.api.events.PlayerDeathEvent apiEvent =
                    new com.andune.minecraft.commonlib.server.bukkit.events.PlayerDeathEvent((Player) event.getEntity(), bukkitFactory);
            eventListener.playerDeath(apiEvent);
        }
    }

    /* Not needed, not used outside of WorldGuard module which will register
     * the event natively if needed.
    @Override
    public void registerMoveEvent() {
        plugin.getServer().getPluginManager().registerEvent(PlayerMoveEvent.class,
                this,
                getEventPriority(),
                new EventExecutor() {
                    public void execute(Listener listener, Event event) throws EventException {
                        try {
                            com.andune.minecraft.hsp.server.api.events.PlayerMoveEvent apiEvent =
                                    new com.andune.minecraft.hsp.server.api.events.PlayerMoveEvent((PlayerMoveEvent) event, bukkitFactory);
                            eventListener.playerMove(apiEvent);
                        } catch (Throwable t) {
                            throw new EventException(t);
                        }
                    }
                },
                plugin);
    }
    */
}
