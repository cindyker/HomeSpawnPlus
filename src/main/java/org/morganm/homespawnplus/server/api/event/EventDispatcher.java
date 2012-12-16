/**
 * 
 */
package org.morganm.homespawnplus.server.api.event;

/** API for server to implement in order to setup event hooks.
 *
 * @author morganm
 *
 */
public interface EventDispatcher {
    public void registerEvents();

    /**
     * Move event is high frequency and only used in the event that
     * a WorldGuard strategy that requires it is used. So we register
     * it separately, only if needed.
     */
    public void registerMoveEvent();
}
