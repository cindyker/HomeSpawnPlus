/**
 * 
 */
package com.aranai.spawncontrol;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.aranai.spawncontrol.config.ConfigOptions;
import com.aranai.spawncontrol.entity.Home;
import com.aranai.spawncontrol.entity.Spawn;
import com.nijiko.permissions.Entry;
import com.nijiko.permissions.EntryType;
import com.nijiko.permissions.Group;
import com.nijiko.permissions.User;

/** Utility methods related to spawn/home teleporting and simple entity management.
 * 
 * @author morganm
 *
 */
public class SpawnUtils {
	private static final Logger log = SpawnControl.log;
	private static final String MSC_WORLD_SPAWN_GROUP = "MSC_GLOBAL";
	
	private final String logPrefix = SpawnControl.logPrefix;

	private final SpawnControl plugin;
    private final Server server;
	
	// set when we first find the defaultSpawnWorld, cached for future reference
    private String defaultSpawnWorld;
	
	public SpawnUtils(SpawnControl plugin) {
		this.plugin = plugin;
		this.server = plugin.getServer();
	}
	
    public void sendHome(Player p)
    {
    	if( p == null )
    		return;
    	
    	// try the home they have set for this world
		Location l = getHome(p.getName(), p.getWorld());
		
		if( l != null )
	    	p.teleport(l);
		else
			sendToSpawn(p);					// if no home is set, send them to spawn
    }
    
    /** Send a player to the spawn for their given world, or if none is set for that world,
     * to the global default spawn.
     * 
     * @param p
     */
    public void sendToSpawn(Player p)
    {
    	Location l = getSpawn(p.getWorld().getName());
    	if( l == null )
    		l = getDefaultSpawn();
    	
		if( l == null ) {
			log.severe(logPrefix+ " No valid spawn found for player "+p.getName()+"!");
			return;
		}
		
    	p.teleport(l);
    }
    
    /** Find the group spawn (if any) for a player and send them there.  If no group spawn can
     * be found, the player will be sent to the default spawn.
     * 
     * @param p
     */
    public void sendToGroupSpawn(Player p) {
    	// if group spawning is disabled, or this player doesn't have group spawn permissions
    	// or if we aren't even using permissions at all, then just use the default spawn
    	if( !plugin.getConfig().getBoolean(ConfigOptions.ENABLE_GROUP_SPAWN, false) 
    			|| !plugin.isUsePermissions()
    			|| !plugin.hasPermission(p, SpawnControl.BASE_PERMISSION_NODE + ".groupspawn.use") ) {
    		sendToSpawn(p);
    		return;
    	}
    	
    	String world = p.getWorld().getName();
    	String userName = p.getName();
    	
    	// In Permissions 3, a user can be a member of multiple groups.  So we loop through
    	// all groups and check for a spawn in this world for any of them.
    	if( plugin.isUsePerm3() ) {
        	Set<String> spawnGroups = plugin.getStorage().getSpawnDefinedGroups();
        	Group highestWeightedGroup = null;
        	Spawn groupSpawn = null;

        	// get all groups the user is a member of
        	User user = plugin.getPermissionHandler().getUserObject(world, userName);
        	LinkedHashSet<Entry> entries = user.getParents(world);
        	
        	for(Entry e : entries) {
        		if( e.getType() != EntryType.GROUP )
        			continue;
        		
        		Group group = (Group) e;
        		
        		// do we have a spawngroup defined for this group? (ie. does this group have any
        		// spawns defined on any world at all?)
        		if( spawnGroups.contains(group.getName()) )
        		{
        			// ok now see if we have a spawn defined on this world for this group
        			Spawn spawn = plugin.getStorage().getSpawn(world, group.getName());
        			
        			// if we did find a spawn for this group on this world, compare it to any spawns
        			// we've found so far for this player to see which group has the higher weight
        			if( spawn != null ) {
        				if( highestWeightedGroup == null ||
        						group.getWeight() > highestWeightedGroup.getWeight() )
        				{
        					highestWeightedGroup = group;
        					groupSpawn = spawn;
        				}
        			}
        		}
        	}
        	
        	if( groupSpawn != null )
        		p.teleport(groupSpawn.getLocation());
    		// if no spawn defined for any of the players groups, send them to default spawn
    		else
    			sendToSpawn(p);
    	}
    	// Permissions 2 the player could only have a single group defined - just use that.
    	else {
    		@SuppressWarnings("deprecation")
			String group = plugin.getPermissionHandler().getGroup(p.getWorld().getName(), userName);
    		Spawn spawn = plugin.getStorage().getSpawn(world, group);
    		
    		if( spawn != null )
    			p.teleport(spawn.getLocation());
    		// if no spawn defined for the players group, send them to default spawn
    		else
    			sendToSpawn(p);
    	}
    }
    
    public void setHome(String playerName, Location l, String updatedBy)
    {
    	Home home = plugin.getStorage().getHome(l.getWorld().getName(), playerName);
    	
		// if we get an object back, we already have a Home set for this player/world combo, so we
		// just update the x/y/z location of it.
    	if( home != null ) {
    		home.setLocation(l);
			home.setUpdatedBy(updatedBy);
    	}
    	// this is a new home for this player/world combo, create a new object
    	else
    		home = new Home(playerName, l, updatedBy);
    	
    	plugin.getStorage().writeHome(home);
    }
   
    /** Set the spawn for a given world.
     * 
     * @param l
     * @param updatedBy
     */
    public void setSpawn(Location l, String updatedBy)
    {
    	setGroupSpawn(MSC_WORLD_SPAWN_GROUP, l, updatedBy);
    }
    
    /** Set the spawn for a given world and group.
     * 
     * @param group the group this spawn is related to. Can be null, in which case this update sets the default for the given world.
     * @param l
     * @param updatedBy
     */
    public void setGroupSpawn(String group, Location l, String updatedBy)
    {
    	Spawn spawn = plugin.getStorage().getSpawn(l.getWorld().getName(), group);
    	log.info(logPrefix + " setGroupSpawn(), spawn lookup = "+spawn);
    	
		// if we get an object back, we already have a Spawn set for this world/group combo, so we
		// just update the x/y/z location of it.
    	if( spawn != null ) {
    		spawn.setLocation(l);
    		spawn.setPitch(l.getPitch());
    	}
    	// this is a new spawn for this world/group combo, create a new object
    	else
    		spawn = new Spawn(l, updatedBy, group);
    	
    	plugin.getStorage().writeSpawn(spawn);
    }

    // Get spawn
    public Location getSpawn(String worldName)
    {
    	return getGroupSpawn(MSC_WORLD_SPAWN_GROUP, worldName);
    }
    
    /** Return the global default spawn (ie. there is only one, this is not the multi-world spawn).
     * 
     *  This checks, in order:
     *    * The world defined by the admin in spawn.defaultWorld
     *    * The world named "world" (if any)
     *    * The first world it can find as returned by server.getWorlds()
     *    
     *  For each case, it checks our database for any spawn record.  If the world is valid, but we
     *  have no spawn location on record, then we ask Bukkit what the world spawn location is and
     *  update ours to be the same.
     * 
     * @return
     */
    public Location getDefaultSpawn() {
    	Location l = null;
    	
    	// once we find the defaultSpawnWorld, it's cached for efficiency, so if we've already
    	// cached it, just use that.
    	// Note that if something bizarre happens (like the default world spawn gets deleted from
    	// the underlying database), this just safely falls through and looks for the default
    	// world again.
    	if( defaultSpawnWorld != null ) {
    		l = getSpawn(defaultSpawnWorld);
    		if( l != null )
    			return l;
    	}
    	
    	// first, try to get the default spawn based upon the config 
		String configDefaultWorldName = plugin.getConfig().getString(ConfigOptions.DEFAULT_WORLD, "world");
		World world = server.getWorld(configDefaultWorldName);
		
		// if that didn't work, just get the first world that Bukkit has in it's list
		if( world == null )
			world = server.getWorlds().get(0);

		// Should be impossible to enter this next if(), so throw an exception if we ever get here.
		if( world == null )
			throw new NullPointerException("Couldn't find spawn world!  world is null");

		l = getSpawn(world.getName());
		if( l == null ) {
			// if we didn't find the spawn in our database, then get the spawn location from Bukkit
			// and update our database with that as the default spawn for that world. 
			l = world.getSpawnLocation();
			setSpawn(l, logPrefix);
		}
		
		defaultSpawnWorld = world.getName();
		
		return l;
    }
    
    /** Return the home location of the given player and world.
     * 
     * @param playerName
     * @param world
     * @return the home location or null if no home is set
     */
    public Location getHome(String playerName, World world)
    {
    	Location l = null;
    	
    	Home home = plugin.getStorage().getHome(world.getName(), playerName);
    	if( home != null )
    		l = home.getLocation();
    	
    	return l;
    }
    
    // Get group spawn
    public Location getGroupSpawn(String group, String worldName)
    {
    	Location l = null;
    	Spawn spawn = null;
    	
    	if( group == null )
    		spawn = plugin.getStorage().getSpawn(worldName);
    	else
    		spawn = plugin.getStorage().getSpawn(worldName, group);
    	
    	if( spawn != null )
    		l = spawn.getLocation();
    	else
        	SpawnControl.log.warning("[SpawnControl] Could not find or load group spawn for '"+group+"'!");
    	
    	return l;
    	
    }
}
