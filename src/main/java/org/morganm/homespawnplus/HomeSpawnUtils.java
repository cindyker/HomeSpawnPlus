/**
 * 
 */
package org.morganm.homespawnplus;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.storage.Storage;

import com.nijiko.permissions.Entry;
import com.nijiko.permissions.EntryType;
import com.nijiko.permissions.Group;
import com.nijiko.permissions.User;

/** Utility methods related to spawn/home teleporting and simple entity management.
 * 
 * @author morganm
 *
 */
public class HomeSpawnUtils {
	private static final Logger log = HomeSpawnPlus.log;
	
//    private static final String LightPurple = "\u00A7d";
    private static final String Yellow = "\u00A7e";
	
	private final String logPrefix = HomeSpawnPlus.logPrefix;

	private final HomeSpawnPlus plugin;
    private final Server server;
	
	// set when we first find the defaultSpawnWorld, cached for future reference
    private String defaultSpawnWorld;
	
	public HomeSpawnUtils(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.server = plugin.getServer();
	}
	
	/** Send a message to a player in our default mod color.
	 * 
	 * @param p
	 * @param message
	 */
	public void sendMessage(Player p, String message) {
		p.sendMessage(Yellow + message);
	}
	
	/** This is called when a player is spawning (onJoin, onDeath or from a command) and its job
	 * is to follow the strategies given to find the preferred Location to send the player.
	 * 
	 * @param p
	 * @return
	 */
	public Location getSpawnLocation(Player p, SpawnInfo spawnInfo) {
		Location l = null;
		
		// this is set to true if we encounter the default strategy in the list
		boolean defaultFlag = false;
		
		String playerName = p.getName();
		
		for(SpawnStrategy s : spawnInfo.spawnStrategies) {
			// we stop as soon as we have a valid location to return
			if( l != null || defaultFlag )
				break;
			
			Home home = null;
			Spawn spawn = null;
			switch(s) {
			case SPAWN_NEW_PLAYER:
				if( spawnInfo.isFirstLogin ) {
				}
				break;
				
			case HOME_THIS_WORLD_ONLY:
				home = getHome(playerName, p.getWorld());
				if( home != null )
					l = home.getLocation();
				break;

				// try home on this world first, if not, use home on default world
			case HOME_MULTI_WORLD:
				home = getHome(playerName, p.getWorld());
				if( home != null )
					l = home.getLocation();
				else {
					home = getHome(playerName, getDefaultWorld());
					if( home != null )
						l = home.getLocation();
				}
				break;

			case HOME_DEFAULT_WORLD:
				home = getHome(playerName, getDefaultWorld());
				if( home != null )
					l = home.getLocation();
				break;
				
			case SPAWN_THIS_WORLD_ONLY:
				spawn = getSpawn(p.getWorld().getName());
				if( spawn != null )
					l = spawn.getLocation();
				break;
				
			case SPAWN_DEFAULT_WORLD:
				l = getDefaultSpawn().getLocation();
				break;
				
			case SPAWN_GROUP:
				// TODO: this should be refactored into it's own method when I get around to refactoring
				// this whole class.
				String group = plugin.getPermissionHandler().getGroup(p.getWorld().getName(), playerName);
				spawn = getGroupSpawn(group, p.getWorld().getName());
	    		
	    		if( spawn != null )
	    			l = spawn.getLocation();
	    		break;
				
			case SPAWN_NEAREST_SPAWN:
				// TODO: not yet implemented
				break;
				
			case DEFAULT:
				defaultFlag = true;
				break;
			}
		}
		
		// if all strategies fail, we default to spawn on the default world, unless the
		// default flag is set, in which case we just return null so the caller knows to
		// not send the player anywhere
		if( l == null && !defaultFlag )
			l = getDefaultSpawn().getLocation();
		
		return l;
	}
	
	public Location sendHome(Player p, String world) {
		if( p == null || world == null )
			return null;
		
		Home home = getHome(p.getName(), world);
		
		Location l = null;
		if( home != null )
			l = home.getLocation();
		
//		log.info(logPrefix + " sendHome got object "+home+" for player "+p.getName()+", location = "+l);
		
		if( l != null ) {
	    	p.teleport(l);
	    	return l;
		}
		else {
			return sendToSpawn(p);					// if no home is set, send them to spawn
		}
	}
	
    public Location sendHome(Player p)
    {
    	return sendHome(p, p.getWorld().getName());
    }
    
    public Location sendToGlobalSpawn(Player p)
    {
    	Location l = getDefaultSpawn().getLocation(); 
    	p.teleport(l);
    	return l;
    }
    
    /** Send a player to the spawn for their given world, or if none is set for that world,
     * to the global default spawn.
     * 
     * @param p
     */
    public Location sendToSpawn(Player p)
    {
    	Spawn spawn = getSpawn(p.getWorld().getName());
    	if( spawn == null )
    		spawn = getDefaultSpawn();
    	
		if( spawn == null ) {
			log.severe(logPrefix+ " No valid spawn found for player "+p.getName()+"!");
			return null;
		}
		
		Location l = spawn.getLocation();
    	p.teleport(l);
    	return l;
    }
    
    /** Find the group spawn (if any) for a player and send them there.  If no group spawn can
     * be found, the player will be sent to the default spawn.
     * 
     * @param p
     */
    public Location sendToGroupSpawn(Player p) {
    	// if group spawning is disabled, or this player doesn't have group spawn permissions
    	// or if we aren't even using permissions at all, then just use the default spawn
    	if( !plugin.getConfig().getBoolean(ConfigOptions.ENABLE_GROUP_SPAWN, false) 
    			|| !plugin.isUsePermissions()
    			|| !plugin.hasPermission(p, HomeSpawnPlus.BASE_PERMISSION_NODE + ".groupspawn.use") ) {
    		return sendToSpawn(p);
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
        	
        	if( groupSpawn != null ) {
        		Location l = groupSpawn.getLocation();
        		p.teleport(groupSpawn.getLocation());
        		return l;
        	}
    		// if no spawn defined for any of the players groups, send them to default spawn
    		else
    			return sendToSpawn(p);
    	}
    	// Permissions 2 the player could only have a single group defined - just use that.
    	else {
    		@SuppressWarnings("deprecation")
			String group = plugin.getPermissionHandler().getGroup(p.getWorld().getName(), userName);
    		Spawn spawn = plugin.getStorage().getSpawn(world, group);
    		
    		if( spawn != null ) {
    			Location l = spawn.getLocation();
    			p.teleport(l);
    			return l;
    		}
    		// if no spawn defined for the players group, send them to default spawn
    		else
    			return sendToSpawn(p);
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
   
    public void setSpawn(String spawnName, Location l, String updatedBy)
    {
    	Spawn spawn = plugin.getStorage().getSpawnByName(spawnName);
    	
		// if we get an object back, we already have a Spawn set for this spawnName, so we
		// just update the x/y/z location of it.
    	if( spawn != null ) {
    		spawn.setLocation(l);
    		spawn.setUpdatedBy(updatedBy);
    	}
    	// this is a new spawn for this world/group combo, create a new object
    	else {
    		spawn = new Spawn(l, updatedBy);
    		spawn.setName(spawnName);
    	}
    	
    	plugin.getStorage().writeSpawn(spawn);    	
    }
    
    /** Set the spawn for a given world.
     * 
     * @param l
     * @param updatedBy
     */
    public void setSpawn(Location l, String updatedBy)
    {
    	setGroupSpawn(Storage.HSP_WORLD_SPAWN_GROUP, l, updatedBy);
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
//    	log.info(logPrefix + " setGroupSpawn(), spawn lookup = "+spawn);
    	
		// if we get an object back, we already have a Spawn set for this world/group combo, so we
		// just update the x/y/z location of it.
    	if( spawn != null ) {
    		spawn.setLocation(l);
    		spawn.setUpdatedBy(updatedBy);
    	}
    	// this is a new spawn for this world/group combo, create a new object
    	else {
    		spawn = new Spawn(l, updatedBy);
    		spawn.setGroup(group);
    	}
    	
    	plugin.getStorage().writeSpawn(spawn);
    }

    public Spawn getSpawn(String worldName)
    {
    	return getGroupSpawn(Storage.HSP_WORLD_SPAWN_GROUP, worldName);
    }
    
    public String getDefaultWorld() {
    	if( defaultSpawnWorld == null )
    		getDefaultSpawn();		// this will find the default spawn world and set defaultSpawnWorld variable
    	
    	return defaultSpawnWorld;
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
    public Spawn getDefaultSpawn() {
    	Spawn spawn;
    	
    	// once we find the defaultSpawnWorld, it's cached for efficiency, so if we've already
    	// cached it, just use that.
    	// Note that if something bizarre happens (like the default world spawn gets deleted from
    	// the underlying database), this just safely falls through and looks for the default
    	// world again.
    	if( defaultSpawnWorld != null ) {
    		spawn = getSpawn(defaultSpawnWorld);
    		if( spawn != null )
    			return spawn;
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

		spawn = getSpawn(world.getName());
		if( spawn == null ) {
			// if we didn't find the spawn in our database, then get the spawn location from Bukkit
			// and update our database with that as the default spawn for that world. 
			Location l = world.getSpawnLocation();
			setSpawn(l, logPrefix);
			
			spawn = getSpawn(world.getName());	// now get the Spawn object we just inserted
			
			// shouldn't ever happen, but we know how that goes ...  If there's a problem getting
			// the object back we just inserted, then we just create a new object with default
			// world spawn coordinates and complain loudly in the logs.
			if( spawn == null ) {
				log.warning(logPrefix + " ERROR: could not find default Spawn - improvising!");
				spawn = new Spawn(l, logPrefix);
				spawn.setGroup(Storage.HSP_WORLD_SPAWN_GROUP);
			}
		}
		
		defaultSpawnWorld = world.getName();
		
		return spawn;
    }
    
    /** Return the home location of the given player and world.
     * 
     * @param playerName
     * @param worldName
     * @return the home location or null if no home is set
     */
    public Home getHome(String playerName, String worldName)
    {
    	return plugin.getStorage().getHome(worldName, playerName);
    }
    /** Return the home location of the given player and world.
     * 
     * @param playerName
     * @param world
     * @return the home location or null if no home is set
     */
    public Home getHome(String playerName, World world) {
    	return getHome(playerName, world.getName());
    }
    
    /** Look for a partial name match for a home on a given world
     * 
     * @param playerName
     * @param worldName
     * @return the Home object or null if none found
     */
    public Home getBestMatchHome(String playerName, String worldName) {
    	Set<Home> homes = plugin.getStorage().getAllHomes();
    	
    	// first find any possible homes based on the input
    	ArrayList<Home> possibles = new ArrayList<Home>();
    	for(Home home : homes) {
    		String homeOwner = home.getPlayerName();
    		if( worldName.equals(home.getWorld()) && homeOwner.contains(playerName) ) {
    			possibles.add(home);
    		}
    	}
    	
    	if( possibles.size() == 0 )
    		return null;
    	else if( possibles.size() == 1 )
    		return possibles.get(0);
    	
    	Home bestMatch = null;
    	// now find the best match out of all the possibilities.  Could have fancier algorithm later,
    	// but for now it just returns the first name it finds that startswith the player input.
    	for(Home home : possibles) {
    		String homeOwner = home.getPlayerName();
    		if( homeOwner.startsWith(playerName) ) {
    			bestMatch = home;
    			break;
    		}
    	}
    	// still no match out of the possibilities?  just take the first one on the list
    	if( bestMatch == null )
    		bestMatch = possibles.get(0);
    	
    	return bestMatch;
    }
    
    // Get group spawn
    public Spawn getGroupSpawn(String group, String worldName)
    {
    	Spawn spawn = null;
    	
    	if( group == null )
    		spawn = plugin.getStorage().getSpawn(worldName);
    	else
    		spawn = plugin.getStorage().getSpawn(worldName, group);
    	
    	if( spawn == null )
        	log.warning(logPrefix + " Could not find or load group spawn for '"+group+"' on world "+worldName+"!");
    	
    	return spawn;
    	
    }
    
    /** Can be used to teleport the player on a slight delay, which gets around a nasty issue that can crash
     * the server if you teleport them during certain events (such as onPlayerJoin).
     * 
     * @param p
     * @param l
     */
    public void delayedTeleport(Player p, Location l) {
    	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedTeleport(p, l));
    }
    
    private class DelayedTeleport implements Runnable {
    	private Player p;
    	private Location l;
    	
    	public DelayedTeleport(Player p, Location l) {
    		this.p = p;
    		this.l = l;
    	}
    	
    	public void run() {
    		p.teleport(l);
    	}
    }
}
