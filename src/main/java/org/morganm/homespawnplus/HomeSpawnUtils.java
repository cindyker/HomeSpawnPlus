/**
 * 
 */
package org.morganm.homespawnplus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.morganm.homespawnplus.config.Config;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.i18n.Colors;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.HomeDAO;
import org.morganm.homespawnplus.strategy.EventType;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.General;

/** Utility methods related to spawn/home teleporting and simple entity management.
 * 
 * @author morganm
 *
 */
public class HomeSpawnUtils {
	private static final Logger log = HomeSpawnPlus.log;
	private final String logPrefix = HomeSpawnPlus.logPrefix;

	private final HomeSpawnPlus plugin;
    private final Server server;
	private final Random random = new Random(System.currentTimeMillis());
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
    
    public String getDefaultColor() {
    	return Colors.getDefaultColor();
    }

    public String getLocalizedMessage(final HSPMessages msgKey, final Object...args) {
    	return plugin.getLocale().getMessage(msgKey.toString(), args);
    }
    
	/** Send a message to a player using the default color. sendLocalizedMessage should
	 * be preferred to this method to allow for localization of the messages being
	 * sent, but this can be used in cases where no localization is needed (such as
	 * when printing out a list of results with no localizable strings).
	 *
	 * @param p
	 * @param message
	 */
	public void sendMessage(final CommandSender sender, final String message) {
		sender.sendMessage(getDefaultColor() + message);
	}
	/** Send a message to a player using the localized string.
	 * 
	 * @param target
	 * @param msgKey
	 * @param args
	 */
	public void sendLocalizedMessage(final CommandSender target, final HSPMessages msgKey, final Object...args) {
		target.sendMessage(getDefaultColor() + getLocalizedMessage(msgKey, args));
	}
	
	// convenience method proxy
	public Location getStrategyLocation(EventType event, Player player, String...args) {
		return plugin.getStrategyEngine().getStrategyLocation(event, player, args);
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
		else {
			World w = l.getWorld();
			String worldName = null;
			if( w != null )
				worldName = w.getName();
			else
				worldName = "(world deleted)";
			return worldName+","+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
		}
	}
	
	public String shortLocationString(Home h) {
		if( h == null )
			return "null";
		else {
			Location l = h.getLocation();
			if( l.getWorld() != null )
				return shortLocationString(l);
			else {
				return h.getWorld()+","+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
			}
		}
	}
	
	public String shortLocationString(Spawn s) {
		if( s == null )
			return "null";
		else {
			Location l = s.getLocation();
			if( l.getWorld() != null )
				return shortLocationString(l);
			else {
				return s.getWorld()+","+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
			}
		}
	}

	
	/**
	 * 
	 * @param playerName
	 * @param world if world is null, the global value is used instead
	 * @param perWorldLimit true if you want the perWorld limit, false if you want the global limit
	 */
	public int getHomeLimit(Player player, String worldName, boolean perWorldLimit) {
		int limit = -2;
		
		String limitKey = null;
		if( perWorldLimit )
			limitKey = ConfigOptions.HOME_LIMITS_PER_WORLD;
		else
			limitKey = ConfigOptions.HOME_LIMITS_GLOBAL;
		
		debug.debug("getHomeLimit(), player = ",player,", worldName = ",worldName,", limitKey = ",limitKey);
		
		Config config = plugin.getHSPConfig();
		
		// check permissions section; we iterate through the permissions of each section
		// and see if this player has that permission
		ConfigurationSection section = config.getConfigurationSection(
				ConfigOptions.HOME_LIMITS_BASE + "permission");
		if( section != null ) {
			Set<String> sections = section.getKeys(false);
			if( sections != null ) {
				for(String key : sections) {
					debug.debug("found limit section ",key,", checking permissions for section");
					List<String> perms = config.getStringList(ConfigOptions.HOME_LIMITS_BASE
							+ "permission." + key + ".permissions", null);
					if( perms != null ) {
						for(String perm : perms) {
							debug.debug("checking permission ",perm," for player ",player);
							if( plugin.hasPermission(player, perm) ) {
								limit = config.getInt(ConfigOptions.HOME_LIMITS_BASE
										+ "permission." + key + "." + limitKey, -2);

								debug.debug(limitKey," limit for permission ",perm," = ",limit);

								if( limit != -2 )
									break;
							}
						}
					}
					
					if( limit != -2 ) {
						debug.debug("Limit value of ",limit," found as a result of section ",key,"; stopping limit search");
						break;
					}
				}
			}
		}
		
		// try specific world setting if we haven't found a limit yet
		if( limit == -2 ) {
			limit = config.getInt(ConfigOptions.HOME_LIMITS_BASE + "world." + worldName + "." + limitKey, -2);
			debug.debug(limitKey," limit for world ",worldName," = ",limit);
		}
		
		if( limit == -2 ) {
			limit = config.getInt(ConfigOptions.HOME_LIMITS_BASE
					+ ConfigOptions.HOME_LIMITS_DEFAULT + "." + limitKey, -2);
			debug.debug(limitKey," default limit = ",limit);
		}
		
		// if we get to here and still haven't found a value, we assume a sane default of 1
		if( limit == -2 )
			limit = 1;
		
		debug.debug("getHomeLimit() returning ",limitKey," limit ",limit," for player ",player);
		
		return limit;
	}
	
	/** Get the home count for a given player/world combo. Passing null for the worldName
	 * will return the global count. 
	 * 
	 * @param playerName
	 * @param worldName
	 * @return
	 */
	public int getHomeCount(String playerName, String worldName)
	{
		Set<Home> homes = plugin.getStorage().getHomeDAO().findHomesByWorldAndPlayer(worldName, playerName);
		
		if( homes != null )
			return homes.size();
		else
			return 0;
	}
	
	/** Return true if the player has at least one free home slot (perWorld and global).
	 * 
	 * @param player
	 * @param worldName
	 * @param printMessage if true and the player is over the limit, a message will be pritned
	 * to the player to tell them they are over the limit
	 * @return
	 */
	public boolean canPlayerAddHome(Player p, String worldName, boolean printMessage) {
		int limit = getHomeLimit(p, worldName, true);
		int currentCount = getHomeCount(p.getName(), worldName);
		if( limit != -1 && currentCount + 1 > limit ) {
			if( printMessage )
				sendMessage(p, "You are at your limit of "+limit+" homes for world \""+worldName+"\"");
			return false;
		}
		
		// check global limit
		limit = getHomeLimit(p, null, false);
		currentCount = getHomeCount(p.getName(), null);
		if( limit != -1 && currentCount + 1 > limit ) {
			if( printMessage )
				sendMessage(p, "You are at your global limit of "+limit+" homes");
			return false;
		}
		
		return true;
	}
	
	private Home enforceSingleGlobalHome(String playerName) {
		Home home = null;
		
		debug.debug("enforceSingleGlobalHome() ENTER");
		Set<Home> homes = plugin.getStorage().getHomeDAO().findHomesByPlayer(playerName);
		
		if( homes != null ) {
			// if we find a single home in the DB, move it to the new location
			if( homes.size() == 1 ) {
    			debug.debug("enforceSingleGlobalHome() found 1 home, updated it");
				home = homes.iterator().next();
			}
			// otherwise, delete all homes and a new one will be created below
			else {
    			debug.debug("enforceSingleGlobalHome() found multiple homes, removing them");
				for(Home h : homes) {
					debug.debug("enforceSingleGlobalHome() removing home ",h);
					try {
						plugin.getStorage().getHomeDAO().deleteHome(h);
					}
					catch(StorageException e) {
						log.log(Level.WARNING, "Error caught in enforceSingleGlobalHome: "+e.getMessage(), e);
					}
				}
			}
		}
		
		debug.debug("enforceSingleGlobalHome() EXIT, home=",home);
		return home;
	}
	
	/** Check if we should enforce singleGlobalHome behavior for a given player.
	 * 
	 * @param world
	 * @param playerName
	 * @return
	 */
	private boolean isSingleGlobalHomeEnabled(String world, String playerName) {
		if( plugin.getHSPConfig().getBoolean(ConfigOptions.SINGLE_GLOBAL_HOME, false) &&
				!plugin.hasPermission(world, playerName, HomeSpawnPlus.BASE_PERMISSION_NODE+".singleGlobalHomeExempt") ) {
			return true;
		}
		else
			return false;
	}
	
	/** Set a player's home.
	 * 
	 * @param playerName
	 * @param l
	 * @param updatedBy
	 * @param defaultHome
	 * @param bedHome
	 * @return true if successful, false if not
	 */
    public boolean setHome(String playerName, Location l, String updatedBy, boolean defaultHome, boolean bedHome)
    {
    	final HomeDAO homeDAO = plugin.getStorage().getHomeDAO();
    	Home home = homeDAO.findDefaultHome(l.getWorld().getName(), playerName);
    	
    	debug.devDebug("setHome: (defaultHome) home=",home);
    	
    	// if bedHome arg is set and the defaultHome is NOT the bedHome, then try to find an
    	// existing bedHome that we can overwrite (should only be one bedHome per world)
    	if( bedHome && (home == null || !home.isBedHome()) ) {
    		home = homeDAO.findBedHome(l.getWorld().getName(), playerName);
    		
    		// if no bed home was found using existing bed name, check all other bed homes
    		// for the bed flag
    		if( home != null && !home.isBedHome() ) {
				Set<Home> homes = homeDAO.findHomesByWorldAndPlayer(l.getWorld().getName(), playerName);
    			if( homes != null ) {
    				for(Home h : homes) {
    					if( h.isBedHome() ) {
    						home = h;
    						break;
    					}
    				}
    			}
    		}
    		
    		if( home == null ) {
    			debug.debug("setHome: bedHome flag enabled, but no bedHome found. Using default home");
            	home = homeDAO.findDefaultHome(l.getWorld().getName(), playerName);
    		}
    	}
    	
    	// could be null if we are working with an offline player
    	Player p = plugin.getServer().getPlayer(playerName);
		
		// if we get an object back, we already have a Home set for this player/world combo, so we
		// just update the x/y/z location of it.
    	if( home != null ) {
    		if( isSingleGlobalHomeEnabled(l.getWorld().getName(), playerName) ) {
    			home = enforceSingleGlobalHome(playerName);
    			
    			// it's possible enforceSingleGlobalHome() just wiped all of our homes
    			if( home == null ) {
    	    		home = new Home(playerName, l, updatedBy);
    			}
    		}
    		// if the world changed, then we need to check world limits on the new world
    		else if( p != null && !l.getWorld().getName().equals(home.getWorld()) ) {
        		if( !canPlayerAddHome(p, l.getWorld().getName(), true) )
        			return false;
    		}
    		
    		home.setLocation(l);
			home.setUpdatedBy(updatedBy);
			if( home.isBedHome() )
				p.setBedSpawnLocation(l);
    	}
    	// this is a new home for this player/world combo, create a new object
    	else {
    		if( isSingleGlobalHomeEnabled(l.getWorld().getName(), playerName) ) {
    			home = enforceSingleGlobalHome(playerName);
    			if( home != null ) {
					home.setLocation(l);
					home.setUpdatedBy(updatedBy);
    			}
    		}
    		// check if they are allowed to add another home
    		else if( p != null && !canPlayerAddHome(p, l.getWorld().getName(), true) ) {
    			return false;
    		}
    		
    		// it's possible singleGlobalHome code has found/created a home object for us now
    		if( home == null )
    			home = new Home(playerName, l, updatedBy);
    	}
    	
    	// we don't set the value directly b/c the way to turn "off" an existing defaultHome is to
    	// just set another one.
    	if( defaultHome )
    		home.setDefaultHome(true);
    	
    	if( bedHome ) {
    		home.setName(l.getWorld().getName() + "_" + Storage.HSP_BED_RESERVED_NAME);
			p.setBedSpawnLocation(l);
    	}
    	// we don't allow use of the reserved suffix "_bed" name unless the bed flag is true
    	else if( home.getName() != null && home.getName().endsWith("_" + Storage.HSP_BED_RESERVED_NAME) ) {
    		home.setName(null);
    	}
		home.setBedHome(bedHome);
		
		debug.devDebug("setHome() pre-commit, home=",home);

		try {
			homeDAO.saveHome(home);
			return true;
		}
		catch(StorageException e) {
			log.log(Level.WARNING, "Error saving home: "+e.getMessage(), e);
		}
		
		return false;
    }
    
    /** Set a named home for a player.
     * 
     * @param playerName
     * @param l
     * @param homeName
     * @param updatedBy
     * @return true if success, false if not
     */
    public boolean setNamedHome(String playerName, Location l, String homeName, String updatedBy)
    {
    	Home home = plugin.getStorage().getHomeDAO().findHomeByNameAndPlayer(homeName, playerName);
    	
    	// could be null if we are working with an offline player
    	Player p = plugin.getServer().getPlayer(playerName);
    	
		// if we get an object back, we already have a Home set for this player/homeName combo,
		// so we just update the x/y/z location of it.
    	if( home != null ) {
    		// if the world changed, then we need to check world limits on the new world
    		if( p != null && !l.getWorld().getName().equals(home.getWorld()) ) {
        		if( !canPlayerAddHome(p, l.getWorld().getName(), true) )
        			return false;
    		}
    		
    		home.setLocation(l);
			home.setUpdatedBy(updatedBy);
    	}
    	// this is a new home for this player/world combo, create a new object
    	else {
    		// check if they are allowed to add another home
    		if( p != null && !canPlayerAddHome(p, l.getWorld().getName(), true) )
    			return false;
    		
    		home = new Home(playerName, l, updatedBy);
    		home.setName(homeName);
    	}
    	
    	try {
    		plugin.getStorage().getHomeDAO().saveHome(home);
    		return true;
    	}
    	catch(StorageException e) {
			log.log(Level.WARNING, "Error saving home: "+e.getMessage(), e);
    	}
		return false;
    }
    
    public Spawn getSpawnByName(String spawnName) {
    	Spawn spawn = null;
    	
    	if( spawnName != null )
    		spawn = plugin.getStorage().getSpawnDAO().findSpawnByName(spawnName);
    	
    	if( spawn == null && isVerboseLogging() )
        	log.warning(logPrefix + " Could not find or load spawnByName for '"+spawnName+"'!");
    	
    	return spawn;
    }
   
    public void setSpawn(String spawnName, Location l, String updatedBy)
    {
    	Spawn spawn = plugin.getStorage().getSpawnDAO().findSpawnByName(spawnName);
    	
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
    	
    	try {
    		plugin.getStorage().getSpawnDAO().saveSpawn(spawn);
    	}
    	catch(StorageException e) {
			log.log(Level.WARNING, "Error saving home: "+e.getMessage(), e);
    	}
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
    	Spawn spawn = plugin.getStorage().getSpawnDAO().findSpawnByWorldAndGroup(l.getWorld().getName(), group);
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
    	
    	try {
        	plugin.getStorage().getSpawnDAO().saveSpawn(spawn);
    	}
    	catch(StorageException e) {
			log.log(Level.WARNING, "Caught exception "+e.getMessage(), e);
    	}
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
    	Home home = plugin.getStorage().getHomeDAO().findDefaultHome(worldName, playerName);
    	
    	// if there is no default home defined and the LAST_HOME_IS_DEFAULT flag is
    	// set, check to see if there is a single home left on the world that we can
    	// assume is the default.
    	if( home == null && plugin.getHSPConfig().getBoolean(ConfigOptions.LAST_HOME_IS_DEFAULT, true) ) {
    		Set<Home> homes = plugin.getStorage().getHomeDAO().findHomesByWorldAndPlayer(worldName, playerName);
    		if( homes != null && homes.size() == 1 )
    			home = homes.iterator().next();
    	}
    	
    	return home;
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
    
    public Home getHomeByName(String playerName, String homeName) {
		return plugin.getStorage().getHomeDAO().findHomeByNameAndPlayer(homeName, playerName);
    }
    
    /** Look for a partial name match for a home on a given world
     * 
     * @param playerName
     * @param worldName
     * @return the Home object or null if none found
     */
    public Home getBestMatchHome(String playerName, String worldName) {
    	Set<Home> homes = plugin.getStorage().getHomeDAO().findAllHomes();
    	
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
    		spawn = plugin.getStorage().getSpawnDAO().findSpawnByWorld(worldName);
    	else
    		spawn = plugin.getStorage().getSpawnDAO().findSpawnByWorldAndGroup(worldName, group);
    	
    	if( spawn == null && isVerboseLogging() )
        	log.warning(logPrefix + " Could not find or load group spawn for '"+group+"' on world "+worldName+"!");
    	
    	return spawn;
    }
    
    /** Return the cost for a given command by a given player. This takes into account
     * any permissions they have and the world they are on for any specific overrides
     * other than the default options.
     * 
     * @param p
     * @param commandName
     * @return
     */
    public int getCommandCost(Player player, String commandName) {
    	int cost = 0;
    	
    	ConfigurationSection cs = plugin.getConfig().getConfigurationSection(ConfigOptions.COST_BASE
    			+ ConfigOptions.SETTING_EVENTS_PERMBASE);
    	if( cs != null ) {
    		Set<String> keys = cs.getKeys(false);
    		if( keys != null ) 
    			for(String entry : keys) {
    				debug.debug("getCommandCost(): checking entry ",entry);
    				// stop looping once we find a non-zero cost
    				if( cost != 0 )
    					break;

    				int entryCost  = plugin.getConfig().getInt(ConfigOptions.COST_BASE
    						+ ConfigOptions.SETTING_EVENTS_PERMBASE + "." + entry + "." + commandName, 0);

    				if( entryCost > 0 ) {
    					List<String> perms = plugin.getConfig().getStringList(ConfigOptions.COST_BASE
    							+ ConfigOptions.SETTING_EVENTS_PERMBASE + "."
    							+ entry + ".permissions");

    					for(String perm : perms) {
    						debug.debug("getCommandCost(): checking permission ",perm," for entry ",entry);

    						if( plugin.hasPermission(player, perm) ) {
    							cost = entryCost;
    							break;
    						}
    					}
    				}// end if( entryCost > 0 )
    			}// end for(String entry : keys)
    	}// end if( cs != null )

    	debug.debug("getCommandCost(): post-permission cost=",cost);
    	
    	// if cost is still 0, then check for world-specific cost
    	if( cost == 0 ) {
    		final String worldName = player.getWorld().getName();
    		cost = plugin.getConfig().getInt(ConfigOptions.COST_BASE
					+ ConfigOptions.SETTING_EVENTS_WORLDBASE + "."
					+ worldName + "." + commandName, 0);
			
	    	debug.debug("getCommandCost(): post-world world=",worldName,", cost=",cost);
    	}
    	
    	// if cost is still 0, then check global cost setting
    	if( cost == 0 ) {
    		cost = plugin.getConfig().getInt(ConfigOptions.COST_BASE + commandName, 0);
        	debug.debug("getCommandCost(): post-global cost=",cost);
    	}

    	// apply sethome-multiplier, if any
    	if( cost > 0 && commandName.equalsIgnoreCase("sethome") ) {
    		double multiplier = plugin.getConfig().getDouble(ConfigOptions.COST_SETHOME_MULTIPLIER, 0);
    		if( multiplier > 0 )
    		{
    			// by the time this method is called, the new home has already been created,
    			// so it is already part of our globalHomeCount
    			int globalHomeCount = getHomeCount(player.getName(), null);
    			if( globalHomeCount > 1 ) {
    				double totalCost = cost;
    				for(int i=1; i < globalHomeCount; i++)
    					totalCost *= multiplier; 
    				double additionalCost = totalCost - cost;
    				debug.debug("applying sethome-multplier ",multiplier," for player ",player,
    						", total global home count=",globalHomeCount,", original cost=",cost,
    						",additionalCost=",additionalCost);
    				// should always be true, but check just in case
    				if( additionalCost > 0 )
    					cost += additionalCost;
    			}
    		}
    	}
    	
    	return cost;
    }

    public void updateQuitLocation(Player p)
    {
    	if( plugin.getHSPConfig().getBoolean(ConfigOptions.ENABLE_RECORD_LAST_LOGOUT, false) ) {
    		debug.debug("updateQuitLocation: updating last logout location for player ",p.getName());
    		
	    	Location quitLocation = p.getLocation();
	    	org.morganm.homespawnplus.entity.Player playerStorage = plugin.getStorage().getPlayerDAO().findPlayerByName(p.getName());
	    	if( playerStorage == null )
	    		playerStorage = new org.morganm.homespawnplus.entity.Player(p);
	    	playerStorage.updateLastLogoutLocation(quitLocation);
	    	try {
	    		plugin.getStorage().getPlayerDAO().savePlayer(playerStorage);
	    	}
	    	catch(StorageException e) {
				log.log(Level.WARNING, "Caught exception "+e.getMessage(), e);
	    	}
    	}
    }
    
    public boolean isNewPlayer(Player p) {
    	String strategy = plugin.getHSPConfig().getString(ConfigOptions.NEW_PLAYER_STRATEGY, ConfigOptions.NewPlayerStrategy.PLAYER_DAT.toString());
    	
    	if( strategy.equals(ConfigOptions.NewPlayerStrategy.BUKKIT.toString()) ) {
    		boolean result = !p.hasPlayedBefore(); 
    		debug.debug("isNewPlayer: using BUKKIT strategy, result=",result);
        	return result;
    	}

    	if( strategy.equals(ConfigOptions.NewPlayerStrategy.ORIGINAL.toString()) ) {
        	if( plugin.getStorage().getPlayerDAO().findPlayerByName(p.getName()) != null ) {
        		debug.debug("isNewPlayer: using ORIGINAL strategy, player has DB record, player is NOT new");
        		return false;
        	}
    		debug.debug("isNewPlayer: using ORIGINAL strategy, player is NOT in the database");
    	}
    	
    	if( strategy.equals(ConfigOptions.NewPlayerStrategy.PLAYER_DAT.toString()) || 
    			strategy.equals(ConfigOptions.NewPlayerStrategy.ORIGINAL.toString()) ) {
    		File worldContainer = Bukkit.getWorldContainer();
    		
    		final List<World> worlds = Bukkit.getWorlds();
    		final String worldName = worlds.get(0).getName();
        	final String playerDat = p.getName() + ".dat";
        	
        	File file = new File(worldContainer, worldName+"/players/"+playerDat);
        	if( file.exists() ) {
        		debug.debug("isNewPlayer: using ",strategy," strategy, ",file," exists, player is NOT new");
        		return false;
        	}

    		debug.debug("isNewPlayer: using ",strategy," strategy, ",file," does not exist");
    	}
    	
		debug.debug("isNewPlayer: using ",strategy," strategy, player is determined to be NEW player");
    	// if we didn't find any record of this player, they must be new
    	return true;
    }
    
    /** Given two equal integer values, figure out their "distance delta".
     * For example, assume two Locations A and B where we're trying to find
     * the distanceDelta between A.x and B.x. Here are examples:
     * 
     *   A.x: -550, B.x: -570 = 20
     *   A.x: 550, B.x: 570 = 20
     *   A.x: -50, B.x: 50 = 100
     *   
     * @param i
     * @param j
     * @return
     */
    private int getDistanceDelta(int i, int j) {
    	int highest = i;
    	int lowest = j;
    	// swap them if it's wrong
    	if( lowest > highest ) {
    		highest = j;
    		lowest = i;
    	}
    	
    	// if both are < 0, swap sign and subtract lowest from highest
    	// (since with swapped sign, the lowest/highest will be reversed)
    	if( highest < 0 && lowest < 0 )
    		return Math.abs(lowest) - Math.abs(highest);
    	else
    		return highest - lowest;
    }
    
    /** Given two integers representing a location component (such as x, y or z),
     * pick a random number that falls between them.
     * 
     * @param i
     * @param j
     * @return
     */
    private int randomDeltaInt(int i, int j) {
    	int result = 0;
    	int delta = getDistanceDelta(i, j);
    	debug.debug("randomDeltaInt(): i=",i,", j=",j,", delta=",delta);
    	if( delta == 0 )
    		return 0;
    	
    	int r = random.nextInt(delta);
    	if( i < j )
    		result = i + r;
    	else
    		result = j + r;
    	
    	debug.debug("randomDeltaInt(): i=",i,", j=",j,", delta=",delta,", r=",r,", result=",result);
    	return result;
    }
    
    /** Given a min and max (that define a square cube "region"), randomly pick
     * a location in between them, and then find a "safe spawn" point based on
     * that location (ie. that won't suffocate or be right above lava, etc).
     * 
     * @param min
     * @param max
     * @return the random safe Location, or null if one couldn't be located
     */
    public Location findRandomSafeLocation(Location min, Location max) {
    	if( min == null || max == null )
    		return null;
    	
    	if( !min.getWorld().equals(max.getWorld()) ) {
    		log.warning(logPrefix+" Attempted to find random location between two different worlds: "+min.getWorld()+", "+max.getWorld());
    		return null;
    	}
    	
    	debug.debug("findRandomSafeLocation(): min: ",min,", max: ",max);
    	
    	int x = randomDeltaInt(min.getBlockX(), max.getBlockX());
    	int y = randomDeltaInt(min.getBlockY(), max.getBlockY());
    	int z = randomDeltaInt(min.getBlockZ(), max.getBlockZ());
    	
    	Location newLoc = new Location(min.getWorld(), x, y, z);
    	debug.debug("findRandomSafeLocation(): newLoc=",newLoc);
    	Location safeLoc = General.getInstance().safeLocation(newLoc);
    	debug.debug("findRandomSafeLocation(): safeLoc=",safeLoc);

    	return safeLoc;
    }
    
    /** Teleport a player, optionally safe teleporting them if specified in the config
     * 
     * @param p
     * @param l
     * @param cause
     */
    public void teleport(Player p, Location l, TeleportCause cause) {
    	if( l == null || p == null )
    		return;
    	if( cause == null )
    		cause = TeleportCause.UNKNOWN;
    	
		if( plugin.getConfig().getBoolean(ConfigOptions.SAFE_TELEPORT, true) )
			General.getInstance().safeTeleport(p, l, TeleportCause.PLUGIN);
		else
			p.teleport(l, cause);
    }

    /** Can be used to teleport the player on a slight delay, which gets around a nasty issue that can crash
     * the server if you teleport them during certain events (such as onPlayerJoin).
     * 
     * @param p
     * @param l
     */
    public void delayedTeleport(Player p, Location l) {
    	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedTeleport(p, l), 2);
    }
    
    private class DelayedTeleport implements Runnable {
    	private Player p;
    	private Location l;
    	
    	public DelayedTeleport(Player p, Location l) {
    		this.p = p;
    		this.l = l;
    	}
    	
    	public void run() {
    		debug.debug(logPrefix+" delayed teleporting "+p+" to "+l);
    		teleport(p, l, TeleportCause.PLUGIN);
    	}
    }
}
