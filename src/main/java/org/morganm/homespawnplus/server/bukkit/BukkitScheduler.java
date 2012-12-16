/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import javax.inject.Inject;

import org.bukkit.Server;
import org.morganm.homespawnplus.server.api.Scheduler;

/**
 * @author morganm
 *
 */
public class BukkitScheduler implements Scheduler {
    private org.bukkit.plugin.Plugin plugin;
    private Server bukkitServer;

    @Inject
    public BukkitScheduler(org.bukkit.plugin.Plugin plugin, Server bukkitServer) {
        this.plugin = plugin;
        this.bukkitServer = bukkitServer;
    }

    @Override
    public int scheduleSyncDelayedTask(Runnable task, long delay) {
        return bukkitServer.getScheduler().scheduleSyncDelayedTask(plugin, task, delay);
    }

    @Override
    public int scheduleAsyncDelayedTask(Runnable task, long delay) {
        return bukkitServer.getScheduler().runTaskLaterAsynchronously(plugin, task, delay).getTaskId();
    }

}
