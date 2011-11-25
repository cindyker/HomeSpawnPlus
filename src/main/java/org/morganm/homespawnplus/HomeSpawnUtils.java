/**
 * 
 */
package org.morganm.homespawnplus;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.SpawnStrategy.Type;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.storage.Storage;

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
    private WorldGuardInterface wgInterface;
    private Debug debug;
	
	// set when we first find the defaultSpawnWorld, cached for future reference
    private String defaultSpawnWorld;
	
	public HomeSpawnUtils(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.server = plugin.getServer();
		this.debug = Debug.getInstance();
	}
	
    public boolean isVerboseLogging() {
    	return plugin.getHSPConfig().getBoolean(ConfigOptions.VERBOSE_LOGGING, false);
    }

	/** Send a message to a player in our default mod color.
	 * 
	 * @param p
	 * @param message
	 */
	public void sendMessage(Player p, String message) {
		p.sendMessage(Yellow + message);
	}
	
	/** Given a location, return a short string format of the form:
	 *    world,x,y,z
	 * 
	 * @param l
	 * @return
	 */
	public String shortLocationString(Location l) {
		if( l == null )
			return "null";
		else
			return l.getWorld().getName()+","+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
	}
	
	private void logStrategyResult(final SpawnStrategy.Type type, final Location l, final boolean verbose) {
		if( verbose )
			log.info(logPrefix + " Evaluated "+type+", location = "+shortLocationString(l));
	}
	
	/** This is called when a player is spawning (onJoin, onDeath or from a command) and its job
	 * is to follow the strategies given to find the preferred Location to send the player.
	 * 
	 * @param player
	 * @return
	 */
	public Location getSpawnLocation(Player player, SpawnInfo spawnInfo) {
		Location l = null;
		
		// this is set to true if we encounter the default strategy in the list
		boolean defaultFlag = false;
		
		String playerName = player.getName();

		final boolean verbose = plugin.getHSPConfig().getBoolean(ConfigOptions.STRATEGY_VERBOSE_LOGGING, false);

		if( verbose )
			log.info(logPrefix + " evaluating strategies for player "+player.getName()+", eventType = "+spawnInfo.spawnEventType);
		// if spawnStrategies is empty, populate spawnStrategies based on spawnEventType 
		if( spawnInfo.spawnStrategies == null && spawnInfo.spawnEventType != null ) {
			// try permission-specific strategies first
			/* this needs more work, we must iterate through any permission-defined strategies and see
			 * if this player has any of those permissions assigned
	    	spawnInfo.spawnStrategies = plugin.getHSPConfig().getStrategies(ConfigOptions.SETTING_EVENTS_BASE
	    			+ "." + ConfigOptions.SETTING_EVENTS_PERMBASE + "."
	    			+ player.getWorld().getName() + "." + spawnInfo.spawnEventType);
			if( verbose )
				log.info(logPrefix + " found "+spawnInfo.spawnStrategies.size()+" permission-specific strategies for player "+player.getName());
				*/
	    	
	    	// if no permission-specific strategy exists, fall back to default world strategy
	    	if( spawnInfo.spawnStrategies == null || spawnInfo.spawnStrategies.isEmpty() ) {
		    	spawnInfo.spawnStrategies = plugin.getHSPConfig().getStrategies(ConfigOptions.SETTING_EVENTS_BASE
		    			+ "." + ConfigOptions.SETTING_EVENTS_WORLDBASE + "."
		    			+ player.getWorld().getName() + "." + spawnInfo.spawnEventType);
				if( verbose )
					log.info(logPrefix + " found "+spawnInfo.spawnStrategies.size()+" world-specific strategies for world "+player.getWorld().getName());
	    	}
						
	    	// if no more specific strategy exists, fall back to default global strategy
	    	if( spawnInfo.spawnStrategies == null || spawnInfo.spawnStrategies.isEmpty() ) {
	    		spawnInfo.spawnStrategies = plugin.getHSPConfig().getStrategies(ConfigOptions.SETTING_EVENTS_BASE
	    				+ "." + spawnInfo.spawnEventType);
				if( verbose )
					log.info(logPrefix + " No world-specific stratgies found, found "+spawnInfo.spawnStrategies.size()+" default strategies");
	    	}
		}
		
		SpawnStrategy currentMode = new SpawnStrategy(SpawnStrategy.Type.MODE_HOME_NORMAL);
		
		for(SpawnStrategy s : spawnInfo.spawnStrategies) {
			// we stop as soon as we have a valid location to return
			if( l != null || defaultFlag )
				break;
			
			Home home = null;
			Spawn spawn = null;
			
			SpawnStrategy.Type type = s.getType();
			/* Switch style comment: I think the braces are ugly, but they are used in some
			 * cases below to scope variables that are local to each case to avoid duplicate
			 * variable names from other cases that might happen to use the same variable
			 * name (of course I could just define a bunch of local variables up top as I've
			 * done with home/spawn, but that just gets even more messy eventually)
			 * 
			 */
			switch(type) {
			case SPAWN_NEW_PLAYER:
				if( spawnInfo.isFirstLogin ) {
					spawn = getSpawnByName(ConfigOptions.VALUE_NEW_PLAYER_SPAWN);
					if( spawn != null )
						l = spawn.getLocation();
				}
				
				logStrategyResult(type, l, verbose);
				break;
				
			case HOME_MULTI_WORLD:
			case HOME_THIS_WORLD_ONLY:
				if( currentMode.getType() == Type.MODE_HOME_NORMAL
						|| currentMode.getType() == Type.MODE_HOME_DEFAULT_ONLY )
					home = getDefaultHome(playerName, player.getWorld());
				
				if( home == null && (currentMode.getType() == Type.MODE_HOME_NORMAL ||
						currentMode.getType() == Type.MODE_HOME_BED_ONLY) )
					home = getBedHome(playerName, player.getWorld().getName());
				
				if( home == null && currentMode.getType() == Type.MODE_HOME_ANY ) {
					Set<Home> homes = plugin.getStorage().getHomes(player.getWorld().getName(), playerName);
					// just grab the first one we find
					if( homes != null && homes.size() != 0 )
						home = homes.iterator().next();
				}

				if( home != null )
					l = home.getLocation();
				
				logStrategyResult(type, l, verbose);
				// if it's HOME_MULTI_WORLD strategy, we fall through to HOME_DEFAULT_WORLD
				if( s.getType() != SpawnStrategy.Type.HOME_MULTI_WORLD || l != null )
					break;

			case HOME_DEFAULT_WORLD:
				if( currentMode.getType() == Type.MODE_HOME_NORMAL
						|| currentMode.getType() == Type.MODE_HOME_DEFAULT_ONLY )
					home = getDefaultHome(playerName, getDefaultWorld());
				
				if( home == null && (currentMode.getType() == Type.MODE_HOME_NORMAL ||
						currentMode.getType() == Type.MODE_HOME_BED_ONLY) )
					home = getBedHome(playerName, getDefaultWorld());
				
				if( home == null && currentMode.getType() == Type.MODE_HOME_ANY ) {
					Set<Home> homes = plugin.getStorage().getHomes(getDefaultWorld(), playerName);
					// just grab the first one we find
					if( homes != null && homes.size() != 0 )
						home = homes.iterator().next();
				}
				
				if( home != null )
					l = home.getLocation();
				logStrategyResult(type, l, verbose);
				break;
			
			case HOME_ANY_WORLD:
				// get the Set of homes for this player for ALL worlds
				Set<Home> homes = plugin.getStorage().getHomes(null, playerName);
				log.info(logPrefix + " [DEBUG] homes = "+homes);
				if( homes != null && homes.size() > 0 ) {
					for(Home h: homes) {
						// in "normal" or "any" mode, we just grab the first home we find
						if( currentMode.getType() == Type.MODE_HOME_NORMAL ||
								currentMode.getType() == Type.MODE_HOME_ANY ) {
							home = h;
							break;
						}
						else if( currentMode.getType() == Type.MODE_HOME_BED_ONLY && h.isBedHome() ) {
							home = h;
							break;
						}
						else if( currentMode.getType() == Type.MODE_HOME_DEFAULT_ONLY && h.isDefaultHome() ) {
							home = h;
							break;
						}
					}
				}
				
				if( home != null )
					l = home.getLocation();
				logStrategyResult(type, l, verbose);
				break;
				
			case MODE_HOME_NORMAL:
			case MODE_HOME_BED_ONLY:
			case MODE_HOME_DEFAULT_ONLY:
			case MODE_HOME_ANY:
				currentMode = new SpawnStrategy(type);
				if( verbose )
					log.info(logPrefix + " Evaluated mode change strategy, new mode = "+currentMode.toString());
				break;
				
			case HOME_SPECIFIC_WORLD:
			{
				String worldName = s.getData();
				home = getDefaultHome(playerName, worldName);
				if( home != null )
					l = home.getLocation();
				logStrategyResult(type, l, verbose);
			}
	    		break;
	    		
			case HOME_NEAREST_HOME:
			{
				// simple algorithm for now, it's not called that often and we assume the list
				// of homes is relatively small (ie. not in the hundreds or thousands).
				Set<Home> allHomes = plugin.getStorage().getHomes(player.getWorld().getName(), playerName);
				Location playerLoc = player.getLocation();
				
				double shortestDistance = -1;
				Home closestHome = null;
				for(Home theHome : allHomes) {
					Location theLocation = theHome.getLocation();
					if( theLocation.getWorld().equals(playerLoc.getWorld()) ) {	// must be same world
						double distance = theLocation.distance(playerLoc);
						if( distance < shortestDistance || shortestDistance == -1 ) {
							shortestDistance = distance;
							closestHome = theHome;
						}
					}
				}
				
				if( closestHome != null )
					l = closestHome.getLocation();
				logStrategyResult(type, l, verbose);
				break;
			}
				
			case SPAWN_THIS_WORLD_ONLY:
				spawn = getSpawn(player.getWorld().getName());
				if( spawn != null )
					l = spawn.getLocation();
				logStrategyResult(type, l, verbose);
				break;
				
			case SPAWN_DEFAULT_WORLD:
				l = getDefaultSpawn().getLocation();
				logStrategyResult(type, l, verbose);
				break;
				
			case SPAWN_GROUP:
			{
				String group = plugin.getPlayerGroup(player.getWorld().getName(), player.getName());
	    		if( group != null )
	    			spawn = getGroupSpawn(group, player.getWorld().getName());
	    		
	    		if( spawn != null )
	    			l = spawn.getLocation();
				logStrategyResult(type, l, verbose);
	    		break;
			}
	    		
			case SPAWN_GROUP_SPECIFIC_WORLD:
			{
				String worldName = s.getData();
				String group = plugin.getPlayerGroup(worldName, player.getName());
	    		if( group != null )
	    			spawn = getGroupSpawn(group, worldName);
	    		
//				log.info(logPrefix + "[DEBUG] SPAWN_GROUP_SPECIFIC_WORLD: worldName = "+worldName+", group = "+group);
	    		if( spawn != null )
	    			l = spawn.getLocation();
				logStrategyResult(type, l, verbose);
	    		break;
			}

			case SPAWN_SPECIFIC_WORLD:
			{
				String worldName = s.getData();
				spawn = getSpawn(worldName);
				if( spawn != null )
					l = spawn.getLocation();
				else
					log.info("No spawn found for world \""+worldName+"\" for \""+ConfigOptions.STRATEGY_SPAWN_SPECIFIC_WORLD+"\" strategy");
				
				logStrategyResult(type, l, verbose);
				break;
			}
			
			case SPAWN_NAMED_SPAWN:
				String namedSpawn = s.getData();
				spawn = getSpawnByName(namedSpawn);
				if( spawn != null )
					l = spawn.getLocation();
				else
					log.info("No spawn found for name \""+namedSpawn+"\" for \""+ConfigOptions.STRATEGY_SPAWN_NAMED_SPAWN+"\" strategy");
				
				logStrategyResult(type, l, verbose);
				break;
				
			case SPAWN_WG_REGION:
				if( wgInterface == null )
					wgInterface = new WorldGuardInterface(plugin);
				
				l = wgInterface.getWorldGuardSpawnLocation(player);
				logStrategyResult(type, l, verbose);
				break;
				
			case SPAWN_NEAREST_SPAWN:
			{
				// simple algorithm for now, it's not called that often and we assume the list
				// of spawns is relatively small (ie. not in the hundreds or thousands).
				Set<Spawn> allSpawns = plugin.getStorage().getAllSpawns();
				Location playerLoc = player.getLocation();
				
				double shortestDistance = -1;
				Spawn closestSpawn = null;
				for(Spawn theSpawn : allSpawns) {
					Location theLocation = theSpawn.getLocation();
					if( theLocation.getWorld().equals(playerLoc.getWorld()) ) {	// must be same world
						double distance = theLocation.distance(playerLoc);
						if( distance < shortestDistance || shortestDistance == -1 ) {
							shortestDistance = distance;
							closestSpawn = theSpawn;
						}
					}
				}
				
				if( closestSpawn != null )
					l = closestSpawn.getLocation();
				logStrategyResult(type, l, verbose);
				break;
			}
				
			case DEFAULT:
				defaultFlag = true;
				if( verbose )
					log.info(logPrefix + " Evaluated "+ConfigOptions.STRATEGY_DEFAULT+", evaluation chain aborted");
				break;
			}
		}
		
		if( verbose )
			log.info(logPrefix + " Evaluation chain complete, location = "+shortLocationString(l));
		return l;
	}
	
	public Location sendHome(Player p, String world) {
		if( p == null || world == null )
			return null;
		
		Home home = getDefaultHome(p.getName(), world);
		
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
    
    public void setHome(String playerName, Location l, String updatedBy, boolean defaultHome, boolean bedHome)
    {
    	Home home = plugin.getStorage().getDefaultHome(l.getWorld().getName(), playerName);
    	
    	debug.devDebug("setHome: (defaultHome) home=",home);
    	
    	// if bedHome arg is set and the defaultHome is NOT the bedHome, then try to find an
    	// existing bedHome that we can overwrite (should only be one bedHome per world)
    	if( bedHome && (home == null || !home.isBedHome()) ) {
    		// first try the bed reserved name (generally should always work)
    		home = plugin.getStorage().getNamedHome(Storage.HSP_BED_RESERVED_NAME, playerName);
    		
    		// if no bed home was found using existing bed name, check all other bed homes
    		// for the bed flag
    		if( home != null && !home.isBedHome() ) {
    			Set<Home> homes = plugin.getStorage().getHomes(l.getWorld().getName(), playerName);
    			if( homes != null ) {
    				for(Home h : homes) {
    					if( h.isBedHome() ) {
    						home = h;
    						break;
    					}
    				}
    			}
    		}
    	}
    	
		// if we get an object back, we already have a Home set for this player/world combo, so we
		// just update the x/y/z location of it.
    	if( home != null ) {
    		home.setLocation(l);
			home.setUpdatedBy(updatedBy);
    	}
    	// this is a new home for this player/world combo, create a new object
    	else
    		home = new Home(playerName, l, updatedBy);
    	
    	// we don't set the value directly b/c the way to turn "off" an existing defaultHome is to
    	// just set another one.
    	if( defaultHome )
    		home.setDefaultHome(true);
    	
    	if( bedHome ) {
    		home.setName(Storage.HSP_BED_RESERVED_NAME);
    	}
    	// we don't allow use of the reserved "bed" name unless the bed flag is true
    	else if( Storage.HSP_BED_RESERVED_NAME.equals(home.getName()) ) {
    		home.setName(null);
    	}
		home.setBedHome(bedHome);
		
		debug.devDebug("home=",home);

    	plugin.getStorage().writeHome(home);
    }
    
    /*
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
    	
		home.setDefaultHome(true);
    	plugin.getStorage().writeHome(home);
    }
    */
    
    public void setNamedHome(String playerName, Location l, String homeName, String updatedBy)
    {
    	Home home = plugin.getStorage().getNamedHome(homeName, playerName);
    	
		// if we get an object back, we already have a Home set for this player/homeName combo,
		// so we just update the x/y/z location of it.
    	if( home != null ) {
    		home.setLocation(l);
			home.setUpdatedBy(updatedBy);
    	}
    	// this is a new home for this player/world combo, create a new object
    	else {
    		home = new Home(playerName, l, updatedBy);
    		home.setName(homeName);
    	}
    	
    	plugin.getStorage().writeHome(home);
    }
    
    public Spawn getSpawnByName(String spawnName) {
    	Spawn spawn = null;
    	
    	if( spawnName != null )
    		spawn = plugin.getStorage().getSpawnByName(spawnName);
    	
    	if( spawn == null && isVerboseLogging() )
        	log.warning(logPrefix + " Could not find or load spawnByName for '"+spawnName+"'!");
    	
    	return spawn;
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
    
    /** Set the default spawn for a given world.
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
		String configDefaultWorldName = plugin.getHSPConfig().getString(ConfigOptions.DEFAULT_WORLD, "world");
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
    public Home getDefaultHome(String playerName, String worldName)
    {
    	return plugin.getStorage().getDefaultHome(worldName, playerName);
    }
    
    /** Return the home location of the given player and world.
     * 
     * @param playerName
     * @param world
     * @return the home location or null if no home is set
     */
    public Home getDefaultHome(String playerName, World world) {
    	return getDefaultHome(playerName, world.getName());
    }
    
    public Home getBedHome(String playerName, String worldName) {
    	Home bedHome = null;
    	
    	Set<Home> homes = plugin.getStorage().getHomes(worldName, playerName);
    	if( homes != null && homes.size() != 0 ) {
	    	for(Home home : homes) {
	    		if( home.isBedHome() ) {
	    			bedHome = home;
	    			break;
	    		}
	    	}
    	}
    	
    	return bedHome;
    }
    
    public Home getHomeByName(String playerName, String homeName) {
    	return plugin.getStorage().getNamedHome(homeName, playerName);
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
    	
    	if( spawn == null && isVerboseLogging() )
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
//    		log.info(logPrefix+" delayed teleporting "+p+" to "+l);
    		p.teleport(l);
    	}
    }
}
