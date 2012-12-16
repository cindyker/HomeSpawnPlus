/**
 * 
 */
package org.morganm.homespawnplus.server.api;

/** API for registering events.
 * 
 * @author morganm
 * @deprecated
 */
public interface EventRegister {
    /**
     * This method should look for any event annotations on methods
     * in the given object and register those events with the target
     * server.
     * 
     * @param o
     */
    public void registerEvents(Object o);
}
