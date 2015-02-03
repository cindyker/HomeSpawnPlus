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
package com.andune.minecraft.hsp.manager;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Scheduler;
import com.andune.minecraft.commonlib.server.api.events.PlayerDamageEvent;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.Permissions;
import com.andune.minecraft.hsp.config.ConfigWarmup;
import com.andune.minecraft.hsp.config.ConfigWarmup.WarmupsPerPermission;
import com.andune.minecraft.hsp.server.api.Server;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * @author andune
 */
@Singleton
public class WarmupManager {
    private static final Logger log = LoggerFactory.getLogger(WarmupManager.class);
    private static final int ONE_SECOND = 1000;
    private static final int TICKS_PER_SECOND = 20;

    private static int uniqueWarmupId = 0;

    private final Map<Integer, PendingWarmup> warmupsPending;
    private final Map<String, List<PendingWarmup>> warmupsPendingByPlayerName;

    private final Server server;
    private final Permissions permissions;
    private final ConfigWarmup configWarmups;
    private final Scheduler scheduler;

    @Inject
    public WarmupManager(Server server, Permissions permissions, ConfigWarmup configWarmups,
                         Scheduler scheduler) {
        this.server = server;
        this.permissions = permissions;
        this.configWarmups = configWarmups;
        this.scheduler = scheduler;

        warmupsPending = new HashMap<Integer, PendingWarmup>();
        warmupsPendingByPlayerName = new HashMap<String, List<PendingWarmup>>();
    }

    private boolean isExemptFromWarmup(Player p, String warmup) {
        return permissions.isWarmupExempt(p, warmup);
    }

    /**
     * Check to see if a given warmup should be enforced for a given player.
     *
     * @param p
     * @param warmupName
     * @return true if warmup should be enforced, flase if not
     */
    public boolean hasWarmup(Player p, String warmupName) {
        return configWarmups.isEnabled() &&
                getWarmupTime(p, warmupName).warmupTime > 0 &&
                !isExemptFromWarmup(p, warmupName);
    }

    /**
     * Utility method for making sure a warmup is not currently pending already.
     * This method does not start the warmup timer, in order to actually start
     * the warmup you need to use startWarmup().
     *
     * @param playerName
     * @param warmupName
     * @return true if warmup is already pending, false if not
     */
    public boolean isWarmupPending(String playerName, String warmupName) {
        boolean warmupPending = false;

        List<PendingWarmup> pendingWarmups = warmupsPendingByPlayerName.get(playerName);
        if (pendingWarmups != null) {
            for (PendingWarmup warmup : pendingWarmups) {
                if (warmup.warmupName.equals(warmupName)) {
                    warmupPending = true;
                    break;
                }
            }
        }

        return warmupPending;
    }

    public WarmupTime getWarmupTime(final Player player, final String warmup) {
        final WarmupTime wut = new WarmupTime();

        // default to existing warmup name
        wut.warmupName = warmup;

        log.debug("getWarmupTime(): warmup={}", warmup);

        if (wut.warmupTime <= 0) {
            Map<String, WarmupsPerPermission> entries = configWarmups.getPerPermissionEntries();

            MATCH_FOUND:
            // iterate over each per-permission entry
            for (Map.Entry<String, WarmupsPerPermission> entry : entries.entrySet()) {
                Integer value = entry.getValue().getWarmups().get(warmup);

                // only if there is a warmup value for this name do we do any extra processing
                if (value != null && value > 0) {
                    // ok now check to see if player has a permisson in the list
                    for (String perm : entry.getValue().getPermissions()) {
                        log.debug("processing per-permission permission {}", perm);
                        if (player.hasPermission(perm)) {
                            wut.warmupTime = value;

                            if (wut.warmupTime > 0) {
                                wut.warmupName = warmup + "." + perm;
                                break MATCH_FOUND;
                            }
                        }
                    }
                }
            }

            log.debug("getWarmupTime(): post-permission warmup={}, name={}", wut.warmupTime, wut.warmupName);
        }

        // if warmupTime is still 0, then check for world-specific warmup
        if (wut.warmupTime <= 0) {
            final String worldName = player.getWorld().getName();
            wut.warmupTime = configWarmups.getPerWorldWarmup(warmup, worldName);
            wut.warmupName = warmup + "." + worldName;

            log.debug("getWarmupTime(): post-world world={}, warmup={}, name={}",
                    worldName, wut.warmupTime, wut.warmupName);
        }

        // if warmupTime is still 0, then check global warmup setting
        if (wut.warmupTime == 0) {
            wut.warmupTime = configWarmups.getGlobalWarmup(warmup);
            wut.warmupName = warmup;
            log.debug("getWarmupTime(): post-global warmup={}, name={}", wut.warmupTime, wut.warmupName);
        }

        return wut;
    }

    /**
     * Start a given warmup.  Return true if the warmup was started successfully, false if not.
     *
     * @param playerName
     * @param warmupRunnable
     * @return
     */
    public boolean startWarmup(String playerName, WarmupRunner warmupRunnable) {
        Player p = server.getPlayer(playerName);
        if (p == null) {
            log.warn("startWarmup() found null player object for name {}" + playerName);
            return false;
        }

        WarmupTime wut = getWarmupTime(p, warmupRunnable.getWarmupName());
        final String warmupName = wut.warmupName;

        // don't let two of the same warmups start
        if (isWarmupPending(playerName, warmupName)) {
            return false;
        }

        int warmupId = 0;
        synchronized (this) {
            warmupId = ++uniqueWarmupId;
        }

        List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
        if (playerWarmups == null) {
            playerWarmups = new ArrayList<PendingWarmup>();
            warmupsPendingByPlayerName.put(playerName, playerWarmups);
        }

        PendingWarmup warmup = new PendingWarmup();
        warmup.warmupId = warmupId;
        warmup.playerName = playerName;
        warmup.warmupName = wut.warmupName;
        warmup.runner = warmupRunnable;
        warmup.startTime = System.currentTimeMillis();
        warmup.warmupTime = wut.warmupTime * 1000;
        warmup.playerLocation = p.getLocation();

        warmupRunnable.setWarmupId(warmupId);
        warmupRunnable.setPlayerName(playerName);

        // keep track of the warmups we have pending
        warmupsPending.put(warmupId, warmup);
        playerWarmups.add(warmup);

        // kick off a Bukkit scheduler to the Runnable for the timer given.  We run
        // every 20 ticks (average 20 ticks = 1 second), then check real clock time.
        //
        // This allows us two things: A) we can manage to real clock time without a
        // separate scheduling mechanism, thus if the warmupTime is 5 seconds, we'll
        // be pretty close to that even on a server running at only 10 TPS.
        // and B) it allows us to cancelOnMove close to the player move event
        // without having to hook the expensive onPlayerMove() event.
        scheduler.scheduleSyncDelayedTask(warmup, TICKS_PER_SECOND);

        return true;
    }

    /**
     * @param warmupId
     * @return true if the event is canceled, false if it is still active
     */
    public boolean isCanceled(int warmupId) {
        PendingWarmup warmup = warmupsPending.get(warmupId);

        return warmup != null && warmup.cancelled == false;
    }

    /**
     * To be called when an entity takes damage so that we can respond appropriately
     * to any pending warmups.
     *
     * @param event
     */
    public void processEntityDamage(PlayerDamageEvent event) {
        // if we aren't supposed to cancel on damage, no further processing required
        if (!configWarmups.isCanceledOnDamage())
            return;

        // don't do any extra processing if there are no pending warmups
        if (warmupsPending.isEmpty())
            return;

        Player p = event.getPlayer();

        if (p != null) {
            String playerName = p.getName();
            List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);

            if (playerWarmups != null && !playerWarmups.isEmpty()) {
                for (Iterator<PendingWarmup> i = playerWarmups.iterator(); i.hasNext(); ) {
                    PendingWarmup warmup = i.next();

                    // remove it directly to avoid ConcurrentModification exception.  Below
                    // warmup.cancel() will also try to remove the object, but it will just
                    // result in a NOOP since we already remove the element here.
                    i.remove();

                    warmup.cancel();

                    p.sendMessage(server.getLocalizedMessage(HSPMessages.WARMUP_CANCELLED_DAMAGE, "name", warmup.warmupName));
                }
            }
        }
    }

    public static class WarmupTime {
        private int warmupTime = 0;
        private String warmupName;

        public int getWarmupTime() {
            return warmupTime;
        }
    }

    private class PendingWarmup implements Runnable {
        private int warmupId;
        private boolean cancelled = false;
        private String playerName;
        private String warmupName;
        private WarmupRunner runner;
        private int warmupTime = 0;
        private long startTime = 0;

        public Location playerLocation;

        private void cleanup() {
            warmupsPending.remove(warmupId);
            List<PendingWarmup> playerWarmups = warmupsPendingByPlayerName.get(playerName);
            if (playerWarmups != null)
                playerWarmups.remove(this);
        }

        public void cancel() {
            cleanup();
            runner.cancel();
        }

        public void run() {
            Player p = server.getPlayer(playerName);
            // this can happen if the player logs out before the warmup fires.  So just cleanup and exit.
            if (p == null) {
                cleanup();
                return;
            }

            // has the warmup fired?  If so, run it.
            if (System.currentTimeMillis() > (startTime + warmupTime)) {
                cleanup();

                // now do whatever the warmup action is
                runner.run();
            }
            // otherwise do some checks and then schedule another run for another 20 ticks out
            else {
                boolean scheduleNext = true;

                // do movement checks to see if player has moved since the warmup started
                if (configWarmups.isCanceledOnMovement()) {
                    Location currentLoc = p.getLocation();
                    if (playerLocation.getBlockX() != currentLoc.getBlockX() ||
                            playerLocation.getBlockY() != currentLoc.getBlockY() ||
                            playerLocation.getBlockZ() != currentLoc.getBlockZ() ||
                            !playerLocation.getWorld().getName().equals(currentLoc.getWorld().getName())) {
                        p.sendMessage(server.getLocalizedMessage(HSPMessages.WARMUP_CANCELLED_YOU_MOVED, "name", warmupName));
                        cleanup();
                        scheduleNext = false;
                    }
                }

                if (scheduleNext)
                    scheduler.scheduleSyncDelayedTask(this, TICKS_PER_SECOND);
            }
        }
    }
}
