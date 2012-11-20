/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import javax.inject.Inject;

import org.morganm.homespawnplus.server.api.events.EventListener;

/** Bridge class between Bukkit event system and HSP API event interface.
 * 
 * @author morganm
 *
 */
public class EventDispatcher implements org.morganm.homespawnplus.server.api.events.EventDispatcher {
    private EventListener listener;
    
    @Inject
    public EventDispatcher(EventListener listener) {
        this.listener = listener;
    }

    /**
     * Register events with Bukkit server.
     * 
     */
    public void registerEvents() {
        
    }
    
    public void playerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        org.morganm.homespawnplus.server.api.events.PlayerJoinEvent apiEvent = new org.morganm.homespawnplus.server.bukkit.events.PlayerJoinEvent(event);
        listener.playerJoin(apiEvent);
    }
    
    public void playerRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        org.morganm.homespawnplus.server.api.events.PlayerRespawnEvent apiEvent = new org.morganm.homespawnplus.server.bukkit.events.PlayerRespawnEvent(event);
        listener.playerRespawn(apiEvent);
    }
}
