/**
 * 
 */
package org.morganm.homespawnplus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.SpawnStrategy.Type;
import org.morganm.homespawnplus.config.Config;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.i18n.Colors;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.WorldGuardInterface;

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
    private WorldGuardInterface wgInterface;
	private Random random = new Random(System.currentTimeMillis());
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

	private void logStrategyResult(final SpawnStrategy.Type type, final Location l, final boolean verbose) {
		if( verbose )
			log.info(logPrefix + " Evaluated "+type+", location = "+shortLocationString(l));
	}
	
	private class SpawnStrategyResult {
		public Location location = null;
		public boolean explicitDefault = false;
	}
	
	private static final BlockFace[] cardinalFaces = new BlockFace[] {BlockFace.NORTH,
		BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
	private static final BlockFace[] adjacentFaces = new BlockFace[] {BlockFace.NORTH,
			BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
			BlockFace.UP, BlockFace.DOWN
	};
	/** Find a bed starting at a given Block, up to maxDepth blocks away.
	 * 
	 * @param l
	 * @param currentLevel
	 * @param maxDepth
	 * @return
	 */
	private Location findBed(Block b, HashSet<Location> checkedLocs, int currentLevel, int maxDepth) {
		debug.devDebug("findBed: b=",b," currentLevel=",currentLevel);
		if( b.getTypeId() == 26 ) {	// it's a bed! make sure the other half is there
			debug.devDebug("findBed: Block ",b," is bed block");
			for(BlockFace bf : cardinalFaces) {
				Block nextBlock = b.getRelative(bf);
				if( nextBlock.getTypeId() == 26 ) {
					debug.devDebug("findBed: Block ",nextBlock," is second bed block");
					return b.getLocation();
				}
			}
		}
		
		// first we check for a bed in all the adjacent blocks, before recursing to move out a level
		for(BlockFace bf : adjacentFaces) {
			Block nextBlock = b.getRelative(bf);
			if( checkedLocs.contains(nextBlock.getLocation()) )	// don't check the same block twice
				continue;
			
			if( nextBlock.getTypeId() == 26 ) {	// it's a bed! make sure the other half is there
				debug.devDebug("findBed: Block ",nextBlock," is bed block");
				for(BlockFace cardinal : cardinalFaces) {
					Block possibleBedBlock = nextBlock.getRelative(cardinal);
					if( possibleBedBlock.getTypeId() == 26 ) {
						debug.devDebug("findBed: Block ",possibleBedBlock," is second bed block");
						return nextBlock.getLocation();
					}
				}
			}
		}
		
		// don't recurse beyond the maxDepth
		if( currentLevel+1 > maxDepth )
			return null;
		
		// if we get here, there were no beds in the adjacent blocks, so now we recurse out one
		// level of blocks to check at the next depth.
		Location l = null;
		for(BlockFace bf : adjacentFaces) {
			Block nextBlock = b.getRelative(bf);
			if( checkedLocs.contains(nextBlock.getLocation()) )	// don't recurse to the same block twice
				continue;
			checkedLocs.add(nextBlock.getLocation());
			
			l = findBed(nextBlock, checkedLocs, currentLevel+1, maxDepth);
			if( l != null )
				break;
		}
		
		return l;
	}
	
	/** Look for a nearby bed to the given home.
	 * 
	 * @param home
	 * @return true if a bed is nearby, false if not
	 */
	public boolean isBedNearby(final Home home) {
		if( home == null )
			return false;
		
		Location l = home.getLocation();
		if( l == null )
			return false;
		
		HashSet<Location> checkedLocs = new HashSet<Location>(50);
		Location bedLoc = findBed(l.getBlock(), checkedLocs, 0, 5);
		
		return bedLoc != null;
	}
	
	/** Loop through all existing modes that have been set to see if a given mode
	 * has been enabled.
	 * 
	 * @param modes
	 * @param mode
	 * @return
	 */
	private boolean isModeEnabled(final List<SpawnStrategy> modes, final Type mode) {
		if( modes == null || modes.size() == 0 ) {
			if( mode == Type.MODE_HOME_NORMAL )		// default mode is assumed true
				return true;
			else
				return false;
		}
		
		for(SpawnStrategy currentMode : modes) {
			if( currentMode.getType() == mode )
				return true;
		}
		
		return false;
	}
	
	/** Taking mode into account, find the default home on a given world. This may
	 * return just a bed home, or not a bed at all or even any home, all depending on the
	 * home mode that is set.
	 * 
	 * @param currentMode
	 * @param playerName
	 * @param worldName
	 * @return the home matching the current mode on the given world, or null
	 */
//	private Home getModeHome(SpawnStrategy currentMode, String playerName, String worldName) {
	private Home getModeHome(List<SpawnStrategy> modes, String playerName, String worldName) {
		Home home = null;
		
		if( isModeEnabled(modes, Type.MODE_HOME_NORMAL)
				|| isModeEnabled(modes, Type.MODE_HOME_DEFAULT_ONLY)
				|| isModeEnabled(modes, Type.MODE_HOME_NO_BED) ) {
			home = getDefaultHome(playerName, worldName);
			if( home != null && home.isBedHome() && isModeEnabled(modes, Type.MODE_HOME_NO_BED) ) {
				if( plugin.getHSPConfig().getBoolean(ConfigOptions.STRATEGY_VERBOSE_LOGGING, false) )
					log.info(logPrefix + " Home "+home+" skipped because MODE_HOME_NO_BED is true and home was set by bed");
				home = null;	// if mode is MODE_HOME_NO_BED and the default home is a bed, don't use it
			}
			
			if( isModeEnabled(modes, Type.MODE_HOME_REQUIRES_BED) && !isBedNearby(home) ) {
				if( plugin.getHSPConfig().getBoolean(ConfigOptions.STRATEGY_VERBOSE_LOGGING, false) )
					log.info(logPrefix + " Home "+home+" skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
				home = null;
			}
		}
		
		if( home == null && (isModeEnabled(modes, Type.MODE_HOME_NORMAL) ||
				isModeEnabled(modes, Type.MODE_HOME_BED_ONLY)) &&
				!isModeEnabled(modes, Type.MODE_HOME_NO_BED) ) {
			home = getBedHome(playerName, worldName);
			
			if( isModeEnabled(modes, Type.MODE_HOME_REQUIRES_BED) && !isBedNearby(home) ) {
				if( plugin.getHSPConfig().getBoolean(ConfigOptions.STRATEGY_VERBOSE_LOGGING, false) )
					log.info(logPrefix + " Home "+home+" skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
				home = null;
			}
		}
		
		if( home == null && isModeEnabled(modes, Type.MODE_HOME_ANY) ) {
			Set<Home> homes = plugin.getStorage().getHomes(worldName, playerName);
			// just grab the first one we find
			if( homes != null && homes.size() != 0 ) {
				home = homes.iterator().next();
				
				if( isModeEnabled(modes, Type.MODE_HOME_REQUIRES_BED) && !isBedNearby(home) ) {
					if( plugin.getHSPConfig().getBoolean(ConfigOptions.STRATEGY_VERBOSE_LOGGING, false) )
						log.info(logPrefix + " Home "+home+" skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
					home = null;
				}
			}
		}
		
		return home;
	}
	
	/** This method is the heart of spawn strategies. A strategy chain is passed into it via
	 * the (now poorly named) SpawnInfo class and it will evaluate those strategies and try to
	 * return a location. If no location can be found from the given strategies, the
	 * SpawnStrategyresult.location returned will be null.
	 * 
	 * @param player
	 * @param spawnInfo
	 * @return
	 */
	private SpawnStrategyResult evaluateSpawnStrategies(Player player, SpawnInfo spawnInfo) {
		final boolean verbose = plugin.getHSPConfig().getBoolean(ConfigOptions.STRATEGY_VERBOSE_LOGGING, false);

		SpawnStrategyResult result = new SpawnStrategyResult();
		Location l = null;
		final String playerName = player.getName();
		
		// this is set to true if we encounter the default strategy in the list
		boolean defaultFlag = false;
		
//		SpawnStrategy currentMode = new SpawnStrategy(Type.MODE_HOME_NORMAL);
		final List<SpawnStrategy> currentModes = new ArrayList<SpawnStrategy>(3);
		currentModes.add(new SpawnStrategy(Type.MODE_HOME_NORMAL));
		
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
					if( verbose )
						log.info(logPrefix+" strategy "+type+", player is detemined to be a new player");
					spawn = getSpawnByName(ConfigOptions.VALUE_NEW_PLAYER_SPAWN);
					if( spawn != null )
						l = spawn.getLocation();
				}
				else if( verbose )
					log.info(logPrefix+" strategy "+type+", player is detemined to NOT be a new player");
				
				logStrategyResult(type, l, verbose);
				break;
				
			case HOME_MULTI_WORLD:
			case HOME_THIS_WORLD_ONLY:
				home = getModeHome(currentModes, playerName, player.getWorld().getName());

				if( home != null )
					l = home.getLocation();
				
				logStrategyResult(type, l, verbose);
				// if it's HOME_MULTI_WORLD strategy, we fall through to HOME_DEFAULT_WORLD
				if( s.getType() != SpawnStrategy.Type.HOME_MULTI_WORLD || l != null )
					break;

			case HOME_DEFAULT_WORLD:
				home = getModeHome(currentModes, playerName, getDefaultWorld());
				
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
						// skip this home if MODE_HOME_REQUIRES_BED is set and no bed is nearby
						if( isModeEnabled(currentModes, Type.MODE_HOME_REQUIRES_BED) && !isBedNearby(h) ) {
							if( verbose )
								log.info(logPrefix + " Home "+h+" skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
							continue;
						}
						
						// in "normal" or "any" mode, we just grab the first home we find
						if( isModeEnabled(currentModes, Type.MODE_HOME_NORMAL) ||
								isModeEnabled(currentModes, Type.MODE_HOME_ANY) ) {
							home = h;
							break;
						}
						else if( isModeEnabled(currentModes, Type.MODE_HOME_BED_ONLY) && h.isBedHome() ) {
							home = h;
							break;
						}
						else if( isModeEnabled(currentModes, Type.MODE_HOME_DEFAULT_ONLY) && h.isDefaultHome() ) {
							home = h;
							break;
						}
					}
				}
				
				if( home != null )
					l = home.getLocation();
				logStrategyResult(type, l, verbose);
				break;
				
			case HOME_NAMED_HOME:
				home = getHomeByName(player.getName(), spawnInfo.argData);
				if( isModeEnabled(currentModes, Type.MODE_HOME_DEFAULT_ONLY) && !home.isDefaultHome() )
					home = null;
				if( isModeEnabled(currentModes, Type.MODE_HOME_BED_ONLY) && !home.isBedHome() )
					home = null;
				if( isModeEnabled(currentModes, Type.MODE_HOME_NO_BED) && home.isBedHome() )
					home = null;
				if( isModeEnabled(currentModes, Type.MODE_HOME_REQUIRES_BED) && !isBedNearby(home) ) {
					if( verbose )
						log.info(logPrefix + " Home "+home+" skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
					home = null;
				}
				
				if( home != null )
					l = home.getLocation();
				logStrategyResult(type, l, verbose);
				break;

			case MODE_HOME_NORMAL:
			case MODE_HOME_BED_ONLY:
			case MODE_HOME_NO_BED:
			case MODE_HOME_DEFAULT_ONLY:
			case MODE_HOME_ANY:
				currentModes.clear();
				
			case MODE_HOME_REQUIRES_BED:		// additive with other modes, does not .clear() first
				currentModes.add(new SpawnStrategy(type));
//				currentMode = new SpawnStrategy(type);
				if( verbose )
					log.info(logPrefix + " Evaluated mode change strategy, new mode = "+currentModes.toString());
				break;
				
			case HOME_SPECIFIC_WORLD:
			{
				final String worldName = s.getData();
				home = getModeHome(currentModes, playerName, worldName);
				
				if( home != null )
					l = home.getLocation();
				logStrategyResult(type, l, verbose);
	    		break;
			}
	    		
			case HOME_NEAREST_HOME:
			{
				// simple algorithm for now, it's not called that often and we assume the list
				// of homes is relatively small (ie. not in the hundreds or thousands).
				Set<Home> allHomes = plugin.getStorage().getHomes(player.getWorld().getName(), playerName);
				Location playerLoc = player.getLocation();
				
				double shortestDistance = -1;
				Home closestHome = null;
				for(Home theHome : allHomes) {
					if( isModeEnabled(currentModes, Type.MODE_HOME_NO_BED) && theHome.isBedHome() )
						continue;
					
					if( isModeEnabled(currentModes, Type.MODE_HOME_REQUIRES_BED) && !isBedNearby(theHome) ) {
						if( verbose )
							log.info(logPrefix + " Home "+theHome+" skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
						continue;
					}
					
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
				
			case SPAWN_LOCAL_WORLD_RANDOM:
			{
				String playerLocalWorld = player.getWorld().getName();
				Set<Spawn> allSpawns = plugin.getStorage().getAllSpawns();
				ArrayList<Spawn> spawnChoices = new ArrayList<Spawn>(5);
				for(Spawn theSpawn : allSpawns) {
					if( playerLocalWorld.equals(theSpawn.getWorld()) ) {
						spawnChoices.add(theSpawn);
					}
				}
				if( spawnChoices.size() > 0 ) {
					int randomChoice = random.nextInt(spawnChoices.size());
					l = spawnChoices.get(randomChoice).getLocation();
				}
				break;
			}
				
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
				
				final String playerWorld = playerLoc.getWorld().getName();
				double shortestDistance = -1;
				Spawn closestSpawn = null;
				for(Spawn theSpawn : allSpawns) {
					// this fixes a bug in R5+ where non-loaded worlds apparently won't even
					// return valid location or world objects anymore. So we check the String
					// world values before we do anything else and skip worlds that the
					// player is not on.
					if( !playerWorld.equals(theSpawn.getWorld()) )
						continue;
					
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
				result.explicitDefault = true;
				defaultFlag = true;
				if( verbose )
					log.info(logPrefix + " Evaluated "+ConfigOptions.STRATEGY_DEFAULT+", evaluation chain aborted");
				break;
			}
		}
		
		result.location = l;
		return result;
	}
	
	/** This is called to determine the correct location to send a player by following
	 * the defined strategies.
	 * 
	 * @param player
	 * @return
	 */
	public Location getStrategyLocation(Player player, SpawnInfo spawnInfo) {
		SpawnStrategyResult result = null;
		boolean foundStrategy = false;
		
		final boolean verbose = plugin.getHSPConfig().getBoolean(ConfigOptions.STRATEGY_VERBOSE_LOGGING, false);

		// if spawnStrategies were explicitly passed in, evaluate those and exit 
		if( spawnInfo.spawnStrategies != null && spawnInfo.spawnEventType == null ) {
			if( verbose )
				log.info(logPrefix + " evaluating specific spawnStrategies that were passed in, count = "+spawnInfo.spawnStrategies.size());
			result = evaluateSpawnStrategies(player, spawnInfo);
			return result.location;
		}
		
		if( verbose )
			log.info(logPrefix + " evaluating strategies for player "+player.getName()+", eventType = "+spawnInfo.spawnEventType);
		
		// *** START permission-specific strategies  ***
		
		// try permission-specific strategies first;
		// get any permission nodes that are defined in the config and then loop through them
		// to see if this user has any of them set.
		Set<String> permStrategies = plugin.getHSPConfig().getPermStrategies();
		for(String entry : permStrategies) {
			List<String> perms = plugin.getHSPConfig().getStringList(ConfigOptions.SETTING_EVENTS_BASE
					+ "." + ConfigOptions.SETTING_EVENTS_PERMBASE + "."
					+ entry + ".permissions", null);

			for(String perm : perms) {
				debug.debug("checking permission ",perm);

				if( plugin.hasPermission(player, perm) ) {
					debug.debug("player ",player," does have perm ",perm,", looking up strategies");
					spawnInfo.spawnStrategies = plugin.getHSPConfig().getStrategies(ConfigOptions.SETTING_EVENTS_BASE
							+ "." + ConfigOptions.SETTING_EVENTS_PERMBASE + "."
							+ entry + "." + spawnInfo.spawnEventType);

					// stop checking permissions when we find one that has strategies to use
					if( spawnInfo.spawnStrategies != null && !spawnInfo.spawnStrategies.isEmpty() )
						break;
				}
			}
		}

		if( verbose ) {
			int stratCount = 0;
			if( spawnInfo.spawnStrategies != null )
				stratCount = spawnInfo.spawnStrategies.size();
			log.info(logPrefix + " found "+stratCount+" permission-specific strategies for player "+player.getName());
		}

		// if there are permission strategies to evaluate, do so now
		if( spawnInfo.spawnStrategies != null && spawnInfo.spawnStrategies.size() > 0 ) {
			foundStrategy = true;
			if( verbose )
				log.info(logPrefix + " evaluating permission strategies");

			result = evaluateSpawnStrategies(player, spawnInfo);
		}
		// *** END permission-specific strategies  ***

		// *** START world-specific strategies  ***
		// if no permission-specific strategy returned a location, fall back to default world strategy
		if( result == null || (result.location == null && !result.explicitDefault) ) {
			spawnInfo.spawnStrategies = plugin.getHSPConfig().getStrategies(ConfigOptions.SETTING_EVENTS_BASE
					+ "." + ConfigOptions.SETTING_EVENTS_WORLDBASE + "."
					+ player.getWorld().getName() + "." + spawnInfo.spawnEventType);
			if( verbose )
				log.info(logPrefix + " found "+spawnInfo.spawnStrategies.size()+" world-specific strategies for world "+player.getWorld().getName());

			// if there are world-specific strategies to evaluate, do so now
			if( spawnInfo.spawnStrategies.size() > 0 ) {
				foundStrategy = true;
				if( verbose )
					log.info(logPrefix + " evaluating world strategies");
				
				result = evaluateSpawnStrategies(player, spawnInfo);
			}
		}
		// *** END world-specific strategies  ***

		// *** START default global strategies  ***
		// if no more specific strategy exists, fall back to default global strategy
		if( result == null || (result.location == null && !result.explicitDefault) ) {
			spawnInfo.spawnStrategies = plugin.getHSPConfig().getStrategies(ConfigOptions.SETTING_EVENTS_BASE
					+ "." + spawnInfo.spawnEventType);
			if( verbose )
				log.info(logPrefix + " Found "+spawnInfo.spawnStrategies.size()+" default global strategies");

			// evaluate any global default strategies now
			if( spawnInfo.spawnStrategies.size() > 0 ) {
				foundStrategy = true;
				if( verbose )
					log.info(logPrefix + " evaluating default global strategies");
				
				result = evaluateSpawnStrategies(player, spawnInfo);
			}
		}
		// *** END default global strategies  ***

		// if no strategy exists at all for this event, print a warning to the log
		if( !foundStrategy )
			log.warning(logPrefix + " Warning: no event strategy defined for event "+spawnInfo.spawnEventType+". If this is intentional, just define the event in config.yml with the single strategy \"default\" to avoid this warning.");
		
		Location l = null;
		if( result != null )
			l = result.location;
		if( verbose )
			log.info(logPrefix + " Evaluation chain complete, location = "+shortLocationString(l));
		return l;
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
		Set<Home> homes = plugin.getStorage().getHomes(worldName, playerName);
		
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
		
		debug.debug("singleGlobalHome config enabled, entered enforceSingleGlobalHome");
		Set<Home> homes = plugin.getStorage().getHomes("*", playerName);
		
		if( homes != null ) {
			// if we find a single home in the DB, move it to the new location
			if( homes.size() == 1 ) {
    			debug.debug("singleGlobalHome: found 1 home, updated it");
				home = homes.iterator().next();
			}
			// otherwise, delete all homes and a new one will be created below
			else {
    			debug.debug("singleGlobalHome: found multiple homes, removing them");
				for(Home h : homes) {
					debug.debug("removing home ",h);
					plugin.getStorage().deleteHome(h);
				}
			}
		}
		
		return home;
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
    	Home home = plugin.getStorage().getDefaultHome(l.getWorld().getName(), playerName);
    	
    	debug.devDebug("setHome: (defaultHome) home=",home);
    	
    	// if bedHome arg is set and the defaultHome is NOT the bedHome, then try to find an
    	// existing bedHome that we can overwrite (should only be one bedHome per world)
    	if( bedHome && (home == null || !home.isBedHome()) ) {
    		home = plugin.getStorage().getBedHome(l.getWorld().getName(), playerName);
    		
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
    	
    	// could be null if we are working with an offline player
    	Player p = plugin.getServer().getPlayer(playerName);
		
		// if we get an object back, we already have a Home set for this player/world combo, so we
		// just update the x/y/z location of it.
    	if( home != null ) {
    		if( plugin.getHSPConfig().getBoolean(ConfigOptions.SINGLE_GLOBAL_HOME, false) &&
    				!plugin.hasPermission(l.getWorld().getName(), playerName, HomeSpawnPlus.BASE_PERMISSION_NODE+".singleGlobalHomeExempt") ) {
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
    		if( plugin.getHSPConfig().getBoolean(ConfigOptions.SINGLE_GLOBAL_HOME, false) &&
    				!plugin.hasPermission(l.getWorld().getName(), playerName, HomeSpawnPlus.BASE_PERMISSION_NODE+".singleGlobalHomeExempt") ) {
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

    	plugin.getStorage().writeHome(home);
		return true;
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
    	Home home = plugin.getStorage().getNamedHome(homeName, playerName);
    	
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
    	
    	plugin.getStorage().writeHome(home);
		return true;
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
    	Home home = plugin.getStorage().getDefaultHome(worldName, playerName);
    	
    	// if there is no default home defined and the LAST_HOME_IS_DEFAULT flag is
    	// set, check to see if there is a single home left on the world that we can
    	// assume is the default.
    	if( home == null && plugin.getHSPConfig().getBoolean(ConfigOptions.LAST_HOME_IS_DEFAULT, true) ) {
    		Set<Home> homes = plugin.getStorage().getHomes(worldName, playerName);
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
    	
    	ConfigurationSection cs = plugin.getHSPConfig().getConfigurationSection(ConfigOptions.COST_BASE
    			+ ConfigOptions.SETTING_EVENTS_PERMBASE);
    	if( cs != null ) {
    		Set<String> keys = cs.getKeys(false);
    		if( keys != null ) 
    			for(String entry : keys) {
    				debug.debug("getCommandCost(): checking entry ",entry);
    				// stop looping once we find a non-zero cost
    				if( cost != 0 )
    					break;

    				int entryCost  = plugin.getHSPConfig().getInt(ConfigOptions.COST_BASE
    						+ ConfigOptions.SETTING_EVENTS_PERMBASE + "." + entry + "." + commandName, 0);

    				if( entryCost > 0 ) {
    					List<String> perms = plugin.getHSPConfig().getStringList(ConfigOptions.COST_BASE
    							+ ConfigOptions.SETTING_EVENTS_PERMBASE + "."
    							+ entry + ".permissions", null);

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
    		cost = plugin.getHSPConfig().getInt(ConfigOptions.COST_BASE
					+ ConfigOptions.SETTING_EVENTS_WORLDBASE + "."
					+ worldName + "." + commandName, 0);
			
	    	debug.debug("getCommandCost(): post-world world=",worldName,", cost=",cost);
    	}
    	
    	// if cost is still 0, then check global cost setting
    	if( cost == 0 ) {
    		cost = plugin.getHSPConfig().getInt(ConfigOptions.COST_BASE + commandName, 0);
        	debug.debug("getCommandCost(): post-global cost=",cost);
    	}
    	
    	return cost;
    }

    public void updateQuitLocation(Player p)
    {
    	if( plugin.getHSPConfig().getBoolean(ConfigOptions.ENABLE_RECORD_LAST_LOGOUT, false) ) {
    		debug.debug("updateQuitLocation: updating last logout location for player ",p.getName());
    		
	    	Location quitLocation = p.getLocation();
	    	org.morganm.homespawnplus.entity.Player playerStorage = plugin.getStorage().getPlayer(p.getName());
	    	if( playerStorage == null )
	    		playerStorage = new org.morganm.homespawnplus.entity.Player(p);
	    	playerStorage.updateLastLogoutLocation(quitLocation);
	    	plugin.getStorage().writePlayer(playerStorage);
    	}
    }
    
    public boolean isNewPlayer(Player p) {
    	String strategy = plugin.getHSPConfig().getString(ConfigOptions.NEW_PLAYER_STRATEGY, ConfigOptions.NewPlayerStrategy.PLAYER_DAT.toString());
    	
    	if( strategy.equals(ConfigOptions.NewPlayerStrategy.BUKKIT.toString()) ) {
    		boolean result = p.hasPlayedBefore(); 
    		debug.debug("isNewPlayer: using BUKKIT strategy, result=",result);
        	return result;
    	}

    	if( strategy.equals(ConfigOptions.NewPlayerStrategy.ORIGINAL.toString()) ) {
        	if( plugin.getStorage().getPlayer(p.getName()) != null ) {
        		debug.debug("isNewPlayer: using ORIGINAL strategy, player has DB record, player is NOT new");
        		return false;
        	}
    		debug.debug("isNewPlayer: using ORIGINAL strategy, player is NOT in the database");
    	}
    	
    	if( strategy.equals(ConfigOptions.NewPlayerStrategy.PLAYER_DAT.toString()) || 
    			strategy.equals(ConfigOptions.NewPlayerStrategy.ORIGINAL.toString()) ) {
    		final List<World> worlds = Bukkit.getWorlds();
    		final String worldName = worlds.get(0).getName();
        	final String playerDat = p.getName() + ".dat";
        	
        	File file = new File(worldName+"/players/"+playerDat);
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
    		p.teleport(l);
    	}
    }
}
