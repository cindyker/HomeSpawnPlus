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
package com.andune.minecraft.hsp.util;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.*;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.Permissions;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.server.api.Server;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Utility methods related to manipulating beds in the environment or
 * player.
 *
 * @author andune
 */
@Singleton
public class BedUtilsImpl implements BedUtils {
    private static final BlockFace[] cardinalFaces = new BlockFace[]{BlockFace.NORTH,
            BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    private static final BlockFace[] adjacentFaces = new BlockFace[]{BlockFace.NORTH,
            BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
            BlockFace.UP, BlockFace.DOWN
    };

    private final Logger log = LoggerFactory.getLogger(BedUtilsImpl.class);
    private final Permissions permissions;
    private final ConfigCore configCore;
    private final Server server;
    private final HomeUtil homeUtil;

    // map sorted by PlayerName->Location->Time of event
    private final HashMap<String, ClickedEvent> bedClicks = new HashMap<String, ClickedEvent>();

    @Inject
    public BedUtilsImpl(Permissions permissions, ConfigCore configCore, Server server, HomeUtil homeUtil) {
        this.permissions = permissions;
        this.configCore = configCore;
        this.server = server;
        this.homeUtil = homeUtil;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.util.BedUtils#findBed(com.andune.minecraft.hsp.server.api.Block, int)
     */
    @Override
    public Location findBed(Block b, int maxDepth) {
        return findBedRecursive(b, new HashSet<Location>(50), 0, maxDepth);
    }

    /**
     * Recursive method to efficiently search for a bed within a given distance.
     *
     * @param b
     * @param checkedLocs
     * @param currentLevel
     * @param maxDepth
     * @return
     */
    private Location findBedRecursive(Block b, HashSet<Location> checkedLocs, int currentLevel, int maxDepth) {
        log.debug("findBed: b={} currentLevel={}", b, currentLevel);
        if (b.getType().getBlockType() == BlockTypes.BED) { // it's a bed! make sure the other half is there
            log.debug("findBed: Block ", b, " is bed block");
            for (BlockFace bf : cardinalFaces) {
                Block nextBlock = b.getRelative(bf);
                if (b.getType().getBlockType() == BlockTypes.BED) {
                    log.debug("findBed: Block {} is second bed block", nextBlock);
                    return b.getLocation();
                }
            }
        }

        // first we check for a bed in all the adjacent blocks, before recursing to move out a level
        for (BlockFace bf : adjacentFaces) {
            Block nextBlock = b.getRelative(bf);
            if (checkedLocs.contains(nextBlock.getLocation())) // don't check the same block twice
                continue;

            if (b.getType().getBlockType() == BlockTypes.BED) { // it's a bed! make sure the other half is there
                log.debug("findBed: Block {} is bed block", nextBlock);
                for (BlockFace cardinal : cardinalFaces) {
                    Block possibleBedBlock = nextBlock.getRelative(cardinal);
                    if (b.getType().getBlockType() == BlockTypes.BED) {
                        log.debug("findBed: Block {} is second bed block", possibleBedBlock);
                        return nextBlock.getLocation();
                    }
                }
            }
        }

        // don't recurse beyond the maxDepth
        if (currentLevel + 1 > maxDepth)
            return null;

        // if we get here, there were no beds in the adjacent blocks, so now we recurse out one
        // level of blocks to check at the next depth.
        Location l = null;
        for (BlockFace bf : adjacentFaces) {
            Block nextBlock = b.getRelative(bf);
            if (checkedLocs.contains(nextBlock.getLocation())) // don't recurse to the same block twice
                continue;
            checkedLocs.add(nextBlock.getLocation());

            l = findBedRecursive(nextBlock, checkedLocs, currentLevel + 1, maxDepth);
            if (l != null)
                break;
        }

        return l;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.util.BedUtils#setBukkitBedHome(com.andune.minecraft.hsp.server.api.Player, com.andune.minecraft.hsp.server.api.Location)
     */
    @Override
    public void setBukkitBedHome(final Player player, final Location l) {
        if (l == null)
            return;

        // First check the existing bed location. If it exists and is within
        // 10 blocks already, do nothing.
        Location oldBedLoc = player.getBedSpawnLocation();
        if (oldBedLoc != null) {
            double distance = oldBedLoc.distance(l);
            if (distance < 10)
                return;
        }

        // look up to 10 blocks away for the bed
        Location bedLoc = findBed(l.getBlock(), 10);

        if (bedLoc != null)
            player.setBedSpawnLocation(bedLoc);
    }

    /**
     * Look for a nearby bed to the given home.
     *
     * @param home
     * @return true if a bed is nearby, false if not
     */
    public boolean isBedNearby(final Home home) {
        if (home == null)
            return false;

        Location l = home.getLocation();
        if (l == null)
            return false;

        Location bedLoc = findBed(l.getBlock(), 5);
        return bedLoc != null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.util.BedUtils#doBedClick(com.andune.minecraft.hsp.server.api.Player, com.andune.minecraft.hsp.server.api.Block)
     */
    @Override
    public boolean doBedClick(final Player player, final Block bedBlock) {
        // someone clicked on a bed, good time to keep the 2-click hash clean
        cleanupBedClicks();

        // make sure player has permission
        if (!permissions.hasBedSetHome(player)) {
            log.debug("onPlayerInteract(): player {} has no permission", player);
            return false;
        }

        final boolean require2Clicks = configCore.isBedHome2Clicks();

        ClickedEvent ce = bedClicks.get(player.getName());

        // if there is an event in the cache, then this is their second click - save their home
        if (ce != null || !require2Clicks) {
            if (ce == null || bedBlock.getLocation().equals(ce.location)) {
                boolean setDefaultHome = false;

                // we set the bed to be the default home only if there isn't another non-bed
                // default home that exists
                Home existingDefaultHome = homeUtil.getDefaultHome(player.getName(), player.getWorld().getName());
                if (existingDefaultHome == null || existingDefaultHome.isBedHome())
                    setDefaultHome = true;

                // we update the Bukkit bed first as this avoids setHome() having to
                // guess which bed we clicked on. However, it's possible setHome() will
                // refuse to set the home for some reason, so we first record the
                // old location so we can restore it if the setHome() call fails.
                Location oldBedLoc = player.getBedSpawnLocation();
                player.setBedSpawnLocation(bedBlock.getLocation()); // update Bukkit bed

                String errorMsg = homeUtil.setHome(player.getName(), player.getLocation(), player.getName(), setDefaultHome, true);
                if (errorMsg == null) {        // success!
                    server.sendLocalizedMessage(player, HSPMessages.HOME_BED_SET);
                } else {
                    player.sendMessage(errorMsg);
                    player.setBedSpawnLocation(oldBedLoc);  // restore old bed if setHome() failed
                }

                bedClicks.remove(player.getName());
            }
        }
        // otherwise this is first click, tell them to click again to save their home
        else {
            bedClicks.put(player.getName(), new ClickedEvent(bedBlock.getLocation(), System.currentTimeMillis()));
            server.sendLocalizedMessage(player, HSPMessages.HOME_BED_ONE_MORE_CLICK);

            // cancel the first-click event if 2 clicks is required
            return require2Clicks;
        }

        return false;
    }

    private long lastCleanup = 0L;

    private void cleanupBedClicks() {
        // skip cleanup if nothing to do
        if (bedClicks.size() == 0)
            return;

        // don't run a cleanup if we just ran one in the last 5 seconds
        if (System.currentTimeMillis() < lastCleanup + 5000)
            return;

        lastCleanup = System.currentTimeMillis();

        long currentTime = System.currentTimeMillis();

        Set<Entry<String, ClickedEvent>> set = bedClicks.entrySet();
        for (Iterator<Entry<String, ClickedEvent>> i = set.iterator(); i.hasNext(); ) {
            Entry<String, ClickedEvent> e = i.next();
            // if the click is older than 5 seconds, remove it
            if (currentTime > e.getValue().timestamp + 5000) {
                i.remove();
            }
        }
    }

    private class ClickedEvent {
        public Location location;
        public long timestamp;

        public ClickedEvent(Location location, long timestamp) {
            this.location = location;
            this.timestamp = timestamp;
        }
    }
}
