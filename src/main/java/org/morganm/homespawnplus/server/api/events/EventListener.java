/**
 * 
 */
package org.morganm.homespawnplus.server.api.events;

/** API for HSP to implement in order to receive event callbacks from
 * the server implementation.
 * 
 * @author morganm
 *
 */
public interface EventListener {
    public void playerJoin(PlayerJoinEvent event);
    public void playerRespawn(PlayerRespawnEvent event);
}
