/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import javax.inject.Inject;

import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.Server;
import org.morganm.homespawnplus.server.api.Teleport;
import org.morganm.homespawnplus.server.api.World;
import org.morganm.homespawnplus.server.api.events.EventDispatcher;
import org.morganm.homespawnplus.server.api.events.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author morganm
 *
 */
public class BukkitServer implements Server {
    private final Logger log = LoggerFactory.getLogger(BukkitServer.class);

    private final org.morganm.homespawnplus.server.bukkit.EventDispatcher dispatcher;
    private final Plugin plugin;
    private final Teleport teleport;
    
    @Inject
    public BukkitServer(EventListener listener, Plugin plugin, Teleport teleport) {
        this.dispatcher = new org.morganm.homespawnplus.server.bukkit.EventDispatcher(listener);
        this.plugin = plugin;
        this.teleport = teleport;
    }

    @Override
    public EventDispatcher getEventDispatcher() {
        return dispatcher;
    }
    
    @Override
    public World getWorld(String worldName) {
        return new BukkitWorld(plugin.getServer().getWorld(worldName));
    }

    @Override
    public void delayedTeleport(Player player, Location location) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                new DelayedTeleport(player, location), 2);
    }
    
    private class DelayedTeleport implements Runnable {
        private Player p;
        private Location l;
        
        public DelayedTeleport(Player p, Location l) {
            this.p = p;
            this.l = l;
        }
        
        public void run() {
            log.debug("delayed teleporting {} to {}", p, l);
            teleport.safeTeleport(p, l);
            
//            Teleport.getInstance().setCurrentTeleporter(p.getName());
//            Teleport.getInstance().setCurrentTeleporter(null);
        }
    }

    @Override
    public String getLocalizedMessage(HSPMessages key, Object... args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Player getPlayer(String playerName) {
        return new BukkitPlayer(plugin.getServer().getPlayer(playerName));
    }
}
