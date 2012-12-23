/**
 * 
 */
package org.morganm.homespawnplus.server.api.event;

import org.morganm.homespawnplus.server.api.events.PlayerBedEnterEvent;
import org.morganm.homespawnplus.server.api.events.PlayerBedRightClickEvent;
import org.morganm.homespawnplus.server.api.events.PlayerDamageEvent;
import org.morganm.homespawnplus.server.api.events.PlayerJoinEvent;
import org.morganm.homespawnplus.server.api.events.PlayerKickEvent;
import org.morganm.homespawnplus.server.api.events.PlayerMoveEvent;
import org.morganm.homespawnplus.server.api.events.PlayerQuitEvent;
import org.morganm.homespawnplus.server.api.events.PlayerRespawnEvent;
import org.morganm.homespawnplus.server.api.events.PlayerTeleportEvent;

/** API for HSP to implement in order to receive event callbacks from
 * the server implementation.
 * 
 * @author morganm
 *
 */
public interface EventListener {
    public void playerJoin(PlayerJoinEvent event);
    public void playerRespawn(PlayerRespawnEvent event);
    public void playerTeleport(PlayerTeleportEvent event);
    public void playerMove(PlayerMoveEvent event);
    public void playerQuit(PlayerQuitEvent event);
    public void playerKick(PlayerKickEvent event);
    public void playerDamage(PlayerDamageEvent event);
    
    /**
     * Called when a player right-clicks a bed.
     * @param event
     */
    public void bedRightClick(PlayerBedRightClickEvent event);

    /**
     * Called when a player is about to sleep in a bed at night.
     * @param event
     */
    public void bedEvent(PlayerBedEnterEvent event);

    /**
     * A player Teleport event that is at an observe priority
     * (such as Bukkit MONITOR priority)
     * 
     * @param event
     */
    public void observePlayerTeleport(PlayerTeleportEvent event);

    /**
     * A player Respawn event that is at an observe priority
     * (such as Bukkit MONITOR priority)
     * 
     * @param event
     */
    public void observeRespawn(PlayerRespawnEvent event);
}
