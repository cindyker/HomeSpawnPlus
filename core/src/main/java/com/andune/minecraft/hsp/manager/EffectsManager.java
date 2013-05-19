/**
 * 
 */
package com.andune.minecraft.hsp.manager;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Effect;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Scheduler;

/**
 * HSP allows an admin to tie effects to a strategy. In this mode, if the
 * teleport actually goes through (it could be canceled after the strategy has
 * processed), the effects will be shown. Because of the disconnected nature of
 * the strategy processing from the actual teleport, this class is responsible
 * for keeping track of effects that should be displayed for players as
 * strategies are run and teleports happen.
 * 
 * @author andune
 * 
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
    private final Map<String, PlayerEffect> playerEffects = new HashMap<String, PlayerEffect>();
    
    /**
     * Add a pending playerEffect, which will fire when the player is
     * teleported and doTeleportEffects() is called.
     * 
     * @param player the player at which the effect is triggered
     * @param type the type of effect to use
     * @param to true if the effect is played at the target teleport location
     * @param from true if the effect is played at the source teleport location
     */
    public void addPlayerEffect(Player player, Effect type, boolean to, boolean from) {
        playerEffects.put(player.getName(), new PlayerEffect(type, to, from));
    }
    /**
     * Add a pending playerEffect, which will fire when the player is
     * teleported and doTeleportEffects() is called. This is a shortcut method
     * which sets the to and from boolean to true, so the effect is played
     * both at the source and target teleport location.
     * 
     * @param player the player at which the effect is triggered
     * @param type the type of effect to use
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
        final PlayerEffect effect = playerEffects.get(player.getName());
        
        if( effect == null )
            return;
        
        // basic sanity check; if it's been more than 1 full second since
        // the effect was requested, then we don't fire. While this might
        // mean we miss the occasional effect because of lag, it does prevent
        // effects from get "primed" and then firing a long time later from
        // a teleport they weren't supposed to be fired for.
        if( (System.currentTimeMillis() - effect.timestamp) > 1000 )
            return;
        
        if( effect.from )
            doEffect(player, effect.effect);
        
        // for effects at the other end, we set them up to fire a tick later,
        // which allows the teleport to happen and then shows the effect at
        // the other end
        if( effect.to ) {
            scheduler.scheduleSyncDelayedTask(new Runnable() {
                public void run() {
                    doEffect(player, effect.effect);
                }
            }, 1);
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
