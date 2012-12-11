/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.server.api.CommandSender;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.OfflinePlayer;
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
    
    /* A cached list of worlds, so we don't have to constantly recreate new world
     * wrapper objects.
     */
    private Map<String, World> worlds;
    private List<World> worldList;
    /* A flag to tell us when the underlying Bukkit worlds have possibly changed,
     * in which case we reload our world cache before using it. 
     */
    private boolean clearWorldCache = true;
    
    @Inject
    public BukkitServer(EventListener listener, Plugin plugin, Teleport teleport) {
        this.dispatcher = new org.morganm.homespawnplus.server.bukkit.EventDispatcher(listener);
        this.plugin = plugin;
        this.teleport = teleport;
        
        this.plugin.getServer().getPluginManager().registerEvents(new WorldListener(), this.plugin);
    }

    @Override
    public EventDispatcher getEventDispatcher() {
        return dispatcher;
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
        // TODO
    }

    @Override
    public String sendLocalizedMessage(CommandSender sender, HSPMessages key, Object... args) {
        // TODO
    }
    
    @Override
    public Player getPlayer(String playerName) {
        return new BukkitPlayer(plugin.getServer().getPlayer(playerName));
    }

    /** Given a string, look for the best possible player match. Returned
     * object could be of subclass Player (if the player is online).
     * 
     * @param playerName
     * @return the found OfflinePlayer object (possibly class Player) or null
     */
    public OfflinePlayer getBestMatchPlayer(String playerName) {
        final String lowerName = playerName.toLowerCase();
        int onlineMatchDistance = Integer.MAX_VALUE;
        Player onlineMatch = getPlayer(playerName);
        if( onlineMatch != null )
            onlineMatchDistance = onlineMatch.getName().length() - playerName.length();
        
        int offlineMatchDistance = Integer.MAX_VALUE;
        org.bukkit.OfflinePlayer offlineMatch = plugin.getServer().getOfflinePlayer(playerName);
        // if the player hasn't played before, then Bukkit just returned a bogus
        // object; there's no real player behind it. If so, we have to go the hard
        // route of looping through all "seen" offline players to look for the best
        // match
        if( !offlineMatch.hasPlayedBefore() ) {
            // implemented with same algorithm that Bukkit's online "getPlayer()"
            // routine is
            org.bukkit.OfflinePlayer found = null;
            int delta = Integer.MAX_VALUE;
            org.bukkit.OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();
            for(org.bukkit.OfflinePlayer offlinePlayer : offlinePlayers) {
                if (offlinePlayer.getName().toLowerCase().startsWith(lowerName)) {
                    int curDelta = offlinePlayer.getName().length() - lowerName.length();
                    if (curDelta < delta) {
                        found = offlinePlayer;
                        delta = curDelta;
                    }
                    if (curDelta == 0) break;
                }
            }
            if( found != null ) {
                offlineMatch = found;
                offlineMatchDistance = delta;
            }
        }
        // offlineplayer HAS played before, calculate string distance
        else {
            if (offlineMatch.getName().toLowerCase().startsWith(lowerName)) {
                offlineMatchDistance = offlineMatch.getName().length() - lowerName.length();
            }
        }
        
        if( onlineMatchDistance <= offlineMatchDistance ) {
            if( onlineMatchDistance == Integer.MAX_VALUE )
                log.debug("getBestMatchPlayer() playerName={}, no online or offline player found, returning null", playerName);
            else
                log.debug("getBestMatchPlayer() playerName={}, returning online player {}", playerName, onlineMatch);
            return onlineMatch;
        }
        else {
            log.debug("getBestMatchPlayer() playerName={}, returning offline player {}", playerName, offlineMatch);
            return new BukkitOfflinePlayer(offlineMatch);
        }
    }

    @Override
    public String translateColorCodes(String stringToTranslate) {
        return ChatColor.translateAlternateColorCodes('&', stringToTranslate);
    }

    private void cacheWorlds() {
        List<org.bukkit.World> bukkitWorlds = plugin.getServer().getWorlds();
        
        // (re-)initialize array of the appropriate size 
        worlds = new HashMap<String, World>(bukkitWorlds.size());
        
        for(org.bukkit.World bukkitWorld : bukkitWorlds) {
            worlds.put(bukkitWorld.getName(), new BukkitWorld(bukkitWorld));
        }
        
        worldList = Collections.unmodifiableList(new ArrayList<World>(worlds.values()));
    }

    @Override
    public List<World> getWorlds() {
        if( clearWorldCache )
            cacheWorlds();
        
        return worldList;
    }

    @Override
    public World getWorld(String worldName) {
        if( clearWorldCache )
            cacheWorlds();
        
        return worlds.get(worldName);
    }
    
    private class WorldListener implements Listener {
        @EventHandler
        public void worldLoadEvent(WorldLoadEvent e) {
            clearWorldCache = true;
        }
        @EventHandler
        public void worldUnloadEvent(WorldUnloadEvent e) {
            clearWorldCache = true;
        }
    }
}
