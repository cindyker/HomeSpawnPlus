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
package com.andune.minecraft.hsp.strategies;

import com.andune.minecraft.commonlib.server.api.*;
import com.andune.minecraft.hsp.manager.DeathManager;
import com.andune.minecraft.hsp.strategy.*;

import javax.inject.Inject;

/**
 * Allow spawning at a random radius from the last death location.
 *
 * Config file Format:
 *   lastDeathRandomRadius:maxRadius;minRadius;yVariance
 *
 * yVariance is optional. If you set it to 0, this strategy will try very
 * hard to keep the random spawn location chosen at the same Y level as
 * the original death location, if possible. If you don't set a yVariance,
 * then it is whatever radius you defined. For example, if you set radius
 * at 10, then the yVariance will also be 10, meaning they could spawn 10
 * blocks higher or lower (if a safe spot can be found).
 *
 * Note also this uses HSP's "safe teleport" routines, so it's possible
 * if someone dies in a very small cave that they could be teleported up
 * to the surface, because after a number of random location attempts, the
 * final random location is taken and if it's in a middle of a bunch of
 * brick, the safeTeleport algorithm will move them to the surface.
 *
 * @author andune
 */
@OneArgStrategy
public class LastDeathRandomRadius extends BaseStrategy {
    // max number of times to try a random location that meets the requested
    // admin criteria before giving up and just using the one we have.
    private static final int MAX_TRIES = 15;

    @Inject
    private DeathManager deathManager;
    @Inject
    private Factory factory;
    @Inject
    private Teleport teleport;
    private final String arg;
    private int minRadius = 0;
    private int maxRadius = 0;
    private int yVariance = -1;

    public LastDeathRandomRadius(final String arg) {
        this.arg = arg;
    }

    @Override
    public StrategyResult evaluate(StrategyContext context) {
        final Player p = context.getPlayer();
        final Location l = deathManager.getLastDeathLocation(p);
        log.debug("last death location = {}", l);

        Location result = null;
        if( l != null ) {
            log.debug("minRadius = {}, maxRadius = {}, yVariance = {}", minRadius, maxRadius, yVariance);
            final TeleportOptions teleportOptions = context.getTeleportOptions();

            int i=0;
            for (; i < MAX_TRIES; i++) {
                final int minX = l.getBlockX() - maxRadius;
                final int minZ = l.getBlockZ() - maxRadius;
                // if yVariance was given, use that. Otherwise calculate based on radius
                final int minY = yVariance > -1 ? l.getBlockY() - yVariance : l.getBlockY() - maxRadius;
                final Location min = factory.newLocation(l.getWorld().getName(), minX, minY, minZ, 0, 0);
                log.debug("min = {}", min);

                final int maxX = l.getBlockX() + maxRadius;
                final int maxZ = l.getBlockZ() + maxRadius;
                // if yVariance was given, use that. Otherwise calculate based on radius
                final int maxY = yVariance > -1 ? l.getBlockY() - yVariance : l.getBlockY() + maxRadius;
                Location max = factory.newLocation(l.getWorld().getName(), maxX, maxY, maxZ, 0, 0);
                log.debug("max = {}", max);
                result = teleport.findRandomSafeLocation(min, max, teleportOptions);
                if (result==null) {
                    log.debug("result==null. No random safe location found this loop");
                    continue;
                }

                // random "safe" result might be outside of admin requested
                // bounds. If so, loop and try again.
                if( yVariance > -1 ) {
                    final int y = result.getBlockY();
                    if( y < minY || y > maxY ) {
                        log.debug("result y={}, minY={}, maxY={}. Retrying random location", y, minY, maxY);
                        continue;
                    }
                }
                if( minRadius > 0 ) {
                    double distance = result.distance(l);
                    if( distance < minRadius ) {
                        log.debug("distance = {}, minRadius = {}. distance < minRadius, retrying random location", distance, minRadius);
                        continue;
                    }
                }

                // If we get here, we have a good random location, we're done.
                break;
            }

            if (i==MAX_TRIES) {
                log.info(getStrategyConfigName()+" unable to find random location that met configured range after MAX_TRIES({}) loops. Returning null result.", MAX_TRIES);
                result = null;
            }
        }

        return resultFactory.create(result);
    }

    @Override
    public void validate() throws StrategyException {
        if (arg == null)
            throw new StrategyException(getStrategyConfigName() + " requires an argument. None was given");

        String maxRadiusString = arg;
        String minRadiusString = null;
        String yVarianceString = null;

        int i = arg.indexOf(";");
        if (i != -1) {
            final String[] args = arg.split(";");
            maxRadiusString = args[0];

            if (args.length > 1) {
                minRadiusString = args[1];
            }
            if (args.length > 2) {
                yVarianceString = args[2];
            }
        }

        try {
            maxRadius = Integer.valueOf(maxRadiusString);
        }
        catch (NumberFormatException e) {
            throw new StrategyException(getStrategyConfigName() + " caught exception processing maxRadius argument. A number is required.", e);
        }

        if (minRadiusString != null) {
            try {
                minRadius = Integer.valueOf(minRadiusString);
            } catch (NumberFormatException e) {
                throw new StrategyException(getStrategyConfigName() + " caught exception processing minRadiusString argument. A number is required.", e);
            }
        }

        if (yVarianceString != null) {
            try {
                yVariance = Integer.valueOf(yVarianceString);
            } catch (NumberFormatException e) {
                throw new StrategyException(getStrategyConfigName() + " caught exception processing yVariance argument. A number is required.", e);
            }
        }

        if (maxRadius < 1)
            throw new StrategyException(getStrategyConfigName() + ": radius > 0 is required. Was given " + maxRadius + " instead");
    }
}
