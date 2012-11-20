/**
 * 
 */
package org.morganm.homespawnplus.server.api;

/**
 * @author morganm
 *
 */
public interface Scheduler {
    /**
     * Schedules a one off task to occur after a delay
     * This task will be executed by the main server thread
     *
     * @param plugin Plugin that owns the task
     * @param task Task to be executed
     * @param delay Delay in server ticks before executing task
     * @return Task id number (-1 if scheduling failed)
     */
    public int scheduleSyncDelayedTask(Runnable task, long delay);
}
