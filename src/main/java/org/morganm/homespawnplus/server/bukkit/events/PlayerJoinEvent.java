/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit.events;

import javax.inject.Inject;

import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Bukkit implementation of PlayerJoinEvent API.
 * 
 * @author morganm
 *
 */
public class PlayerJoinEvent extends PlayerEvent
implements org.morganm.homespawnplus.server.api.events.PlayerJoinEvent
{
    private final Logger log = LoggerFactory.getLogger(PlayerJoinEvent.class);
    private Plugin plugin;
    private Server server;
    private org.bukkit.event.player.PlayerJoinEvent event;

    public PlayerJoinEvent(org.bukkit.event.player.PlayerJoinEvent event) {
        this.event = event;
        this.bukkitPlayerEvent = event;
    }
    
    @Inject
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    @Inject
    public void setServer(Server server) {
        this.server = server;
    }

    @Override
    public void setJoinLocation(Location joinLocation) {
        /*
         * Bukkit does not support this event and teleporting the player directly
         * during a join event can crash the server. So we have to setup a delayed
         * event and teleport them after a small delay.
         */
        server.delayedTeleport(player, joinLocation);
        
        // verify they ended up where we sent them by checking 5 tics later
        final Location hspLocation = joinLocation;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                Location currentLocation = getPlayer().getLocation();
                
                // do manual world/x/y/z check instead of .equals() so that we avoid comparing
                // pitch/yaw and also so we round to integer blocks instead of exact double loc
                if( currentLocation.getWorld() != hspLocation.getWorld()
                        || currentLocation.getBlockX() != hspLocation.getBlockX()
                        || currentLocation.getBlockY() != hspLocation.getBlockY()
                        || currentLocation.getBlockZ() != hspLocation.getBlockZ() ) {
                    log.info("onJoin: final player location is different than where HSP sent player, another plugin has changed the location."
                            +" Player {}, HSP location {}"
                            +", final player location {}",
                            getPlayer().getName(), hspLocation.shortLocationString(),
                            currentLocation.shortLocationString());
                }
            }
        }, 5); 
    }
}
