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
import com.andune.minecraft.commonlib.server.api.Effect;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Scheduler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * HSP allows an admin to tie effects to a strategy. In this mode, if the
 * teleport actually goes through (it could be canceled after the strategy has
 * processed), the effects will be shown. Because of the disconnected nature of
 * the strategy processing from the actual teleport, this class is responsible
 * for keeping track of effects that should be displayed for players as
 * strategies are run and teleports happen.
 *
 * @author andune
 */
@Singleton
public class EffectsManager {
    private static final Logger log = LoggerFactory.getLogger(EffectsManager.class);
    private Scheduler scheduler;

    @Inject
    public EffectsManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    // map to keep track of players who should have effects on next teleport
    private final Map<String, List<PlayerEffect>> playerEffects = new HashMap<String, List<PlayerEffect>>();

    /**
     * Add a pending playerEffect, which will fire when the player is
     * teleported and doTeleportEffects() is called.
     *
     * @param player the player at which the effect is triggered
     * @param type   the type of effect to use
     * @param to     true if the effect is played at the target teleport location
     * @param from   true if the effect is played at the source teleport location
     */
    public void addPlayerEffect(Player player, Effect type, boolean to, boolean from) {
        log.debug("addPlayerEffect() player = {}, type = {}", player, type);
        List<PlayerEffect> effects = playerEffects.get(player.getName());
        if (effects == null) {
            effects = new ArrayList<PlayerEffect>();
            playerEffects.put(player.getName(), effects);
        }
        effects.add(new PlayerEffect(type, to, from));
    }

    /**
     * Add a pending playerEffect, which will fire when the player is
     * teleported and doTeleportEffects() is called. This is a shortcut method
     * which sets the to and from boolean to true, so the effect is played
     * both at the source and target teleport location.
     *
     * @param player the player at which the effect is triggered
     * @param type   the type of effect to use
     */
    public void addPlayerEffect(Player player, Effect type) {
        addPlayerEffect(player, type, true, true);
    }

    /**
     * To be called when a successful teleport has been detected. If there
     * are any effects that should be displayed, this method will take care
     * of it.
     *
     * @param player
     */
    public void doTeleportEffects(final Player player) {
        log.debug("doTeleportEffects(), player = {}", player);
        final List<PlayerEffect> effects = playerEffects.get(player.getName());

        if (effects == null || effects.size() == 0)
            return;

        for (Iterator<PlayerEffect> i = effects.iterator(); i.hasNext(); ) {
            final PlayerEffect effect = i.next();
            i.remove();

            // basic sanity check; if it's been more than 1 full second since
            // the effect was requested, then we don't fire. While this might
            // mean we miss the occasional effect because of lag, it does prevent
            // effects from get "primed" and then firing a long time later from
            // a teleport they weren't supposed to be fired for.
            if ((System.currentTimeMillis() - effect.timestamp) > 1000)
                continue;

            if (effect.from)
                doEffect(player, effect.effect);

            // for effects at the other end, we set them up to fire a tick later,
            // which allows the teleport to happen and then shows the effect at
            // the other end
            if (effect.to) {
                scheduler.scheduleSyncDelayedTask(new Runnable() {
                    public void run() {
                        doEffect(player, effect.effect);
                    }
                }, 1);
            }
        }
    }

    private void doEffect(Player player, Effect effect) {
        Location l = player.getLocation();
        l.playEffect(effect, 0);
    }

    private class PlayerEffect {
        long timestamp;
        Effect effect;
        boolean to;
        boolean from;

        public PlayerEffect(Effect effect, boolean to, boolean from) {
            this.effect = effect;
            this.to = to;
            this.from = from;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
