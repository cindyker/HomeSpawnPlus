/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.PlayerSpawn;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.General;
import org.morganm.homespawnplus.util.Teleport;

/** Class responsible for processing strategies at run-time.
 * 
 * @author morganm
 *
 */
public class StrategyEngine {
	private static final Debug debug = Debug.getInstance();
	private final HomeSpawnPlus plugin;
	private final StrategyConfig strategyConfig;
	private final Logger log;
	private final String logPrefix;
	
	public StrategyEngine(final HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.log = plugin.getLogger();
		this.logPrefix = plugin.getLogPrefix();
		this.strategyConfig = new StrategyConfig(this.plugin);
	}
	
	public StrategyConfig getStrategyConfig() {
		return strategyConfig;
	}
	
	/** Convenience method for routines only interested in an output location (as
	 * opposed to other result details).
	 * 
	 * @param event
	 * @param player
	 * @return
	 */
	public StrategyResult getStrategyResult(StrategyContext context, String...args) {
		if( context == null ) {
			log.warning(logPrefix+" null context received, doing nothing");
			return null;
		}
		
    	if( args != null && args.length > 0 )
    		context.setArg(args[0]);
    	
    	StrategyResult result = plugin.getStrategyEngine().evaluateStrategies(context);
    	// decided these warnings are just annoying and of no value. -morganm 7/26/12
//    	if( result == null && plugin.getConfig().getBoolean(ConfigOptions.WARN_NULL_STRATEGY, true) ) {
//			log.info(logPrefix + " strategy result is null for event "+context.getEventType()+"."
//					+ " This could indicate a configuration mistake."
//					+ " Either include \"default\" as the end of your strategy chains to avoid this warning, or set "
//					+ ConfigOptions.WARN_NULL_STRATEGY + " in your config.yml to false");
//    	}

    	return result;
	}
	
	/** Convenience method for routines only interested in an output location (as
	 * opposed to other result details).
	 * 
	 * @param event
	 * @param player
	 * @return
	 */
	public Location getStrategyLocation(String event, Player player, String...args) {
		StrategyResult result = getStrategyResult(event, player, args);
		if( result != null )
			return result.getLocation();
		else
			return null;
	}
	public Location getStrategyLocation(EventType event, Player player, String...args) {
		return getStrategyLocation(event.toString(), player, args);
	}
	
	public StrategyResult getStrategyResult(String event, Player player, String...args) {
    	final StrategyContext context = new StrategyContext(plugin);
    	context.setPlayer(player);
    	context.setEventType(event);
		return getStrategyResult(context, args);
	}
	public StrategyResult getStrategyResult(EventType event, Player player, String...args) {
		return getStrategyResult(event.toString(), player, args);
	}
	
	/** Given a StrategyContext, evaluate the strategies for that context.
	 * 
	 * @param context
	 */
	public StrategyResult evaluateStrategies(StrategyContext context) {
    	long start = System.currentTimeMillis();
		debug.debug("evaluateStrategies: INVOKED. type=",context.getEventType()," player=",context.getPlayer());
		debug.debug("evaluateStrategies: context=",context);
		StrategyResult result = null;
		
		logVerbose("Strategy evaluation started, type=",context.getEventType()," player=",context.getPlayer());
		
		debug.debug("evaluateStrategies: evaluating permission-based strategies");
		List<Set<Strategy>> permStrategies = strategyConfig.getPermissionStrategies(context.getEventType(), context.getPlayer());
		if( permStrategies != null && permStrategies.size() > 0 ) {
			debug.debug("evaluateStrategies: evaluating ",permStrategies.size()," permission strategies");
			for(Set<Strategy> set : permStrategies) {
				context.resetCurrentModes();
				
				result = evaluateStrategies(context, set);
				if( result != null && result.isSuccess() )
					break;
			}
		}
		debug.debug("evaluateStrategies: permission-based strategies result = ",result);

		// need to check world strategies if we don't yet have a successful result
		if( result == null || (result != null && !result.isSuccess()) ) {
			// is it possible for player to have a null world when they first login? not sure
			// but lets be sure we don't blow up if it is. Only process world strategy if
			// player is in a world.
			if( context.getEventLocation().getWorld() != null ) {
				debug.debug("evaluateStrategies: evaluating world-based strategies");
				Set<Strategy> worldStrategies = strategyConfig.getWorldStrategies(context.getEventType(), context.getEventLocation().getWorld().getName());
				if( worldStrategies != null && worldStrategies.size() > 0 ) {
					debug.debug("evaluateStrategies: evaluating ",worldStrategies.size()," world strategies");
					context.resetCurrentModes();
					result = evaluateStrategies(context, worldStrategies);
				}
				debug.debug("evaluateStrategies: world-based strategies result = ",result);
			}
		}
		
		// need to check default strategies if we don't yet have a successful result
		if( result == null || (result != null && !result.isSuccess()) ) {
			debug.debug("evaluateStrategies: evaluating default strategies");
			Set<Strategy> defaultStrategies = strategyConfig.getDefaultStrategies(context.getEventType());
			if( defaultStrategies != null && defaultStrategies.size() > 0 ) {
				debug.debug("evaluateStrategies: evaluating ",defaultStrategies.size()," default strategies");
				context.resetCurrentModes();
				result = evaluateStrategies(context, defaultStrategies);
			}
			debug.debug("evaluateStrategies: default strategies result = ",result);
		}
		
		if( result == null ) {
	    	// decided these warnings are just annoying and of no value. -morganm 7/26/12
//			log.warning(logPrefix + " Warning: no event strategy defined for event "+context.getEventType()+". If this is intentional, just define the event in config.yml with the single strategy \"default\" to avoid this warning.");
		}
		else {
			result.setContext(context);	// associate context with the result
			
			if( result.isExplicitDefault() )
				logVerbose("Evaluation chain complete, result = explicit default");
			else
				logVerbose("Evaluation chain complete, result = ", result);
		}

		// if we have a result, make sure it's a safe location
		if( result != null && result.getLocation() != null && plugin.getConfig().getBoolean(ConfigOptions.SAFE_TELEPORT, true) ) {
			Location oldLocation = result.getLocation();
			int flags = context.getModeSafeTeleportFlags();
			Teleport.Bounds bounds = context.getModeBounds();

			debug.devDebug("evaluateStrategies(): Invoking safeLocation() for event=",context.getEventType(),", current startegy result=",result);
			debug.devDebug("evaluateStrategies(): bounds=",bounds);
			Location safeLocation = General.getInstance().getTeleport().safeLocation(oldLocation, bounds, flags);
			if( safeLocation != null )
				result.setLocation(safeLocation);
			
			if( !oldLocation.equals(result.getLocation()) )
				debug.debug("evaluateStrategies: safeLocation changed to ",result.getLocation()," from ",oldLocation);
		}

		// are we supposed to remember a spawn?
		if( result != null && result.getSpawn() != null && context.isModeEnabled(StrategyMode.MODE_REMEMBER_SPAWN) ) {
			PlayerSpawnDAO dao = plugin.getStorage().getPlayerSpawnDAO();
			PlayerSpawn ps = dao.findByWorldAndPlayerName(result.getSpawn().getWorld(), context.getPlayer().getName());
			if( ps == null ) {
				ps = new PlayerSpawn();
				ps.setPlayerName(context.getPlayer().getName());
			}
			ps.setSpawn(result.getSpawn());
			try {
				dao.save(ps);
			} catch(StorageException e) { e.printStackTrace(); }
			debug.debug("evaluateStrategies: recorded PlayerSpawn spawn as directed by ",StrategyMode.MODE_REMEMBER_SPAWN);
		}
		// no.. are we supposed to remember a location?
		else if( result != null && result.getLocation() != null && context.isModeEnabled(StrategyMode.MODE_REMEMBER_LOCATION) ) {
			PlayerSpawnDAO dao = plugin.getStorage().getPlayerSpawnDAO();
			PlayerSpawn ps = dao.findByWorldAndPlayerName(result.getLocation().getWorld().getName(), context.getPlayer().getName());
			if( ps == null ) {
				ps = new PlayerSpawn();
				ps.setPlayerName(context.getPlayer().getName());
			}
			ps.setSpawn(null);	// be sure to clear out spawn so this new location is used instead
			ps.setLocation(result.getLocation());
			try {
				dao.save(ps);
			} catch(StorageException e) { e.printStackTrace(); }
			debug.debug("evaluateStrategies: recorded PlayerSpawn location as directed by ",StrategyMode.MODE_REMEMBER_LOCATION);
		}
		
    	int warnMillis = plugin.getConfig().getInt(ConfigOptions.WARN_PERFORMANCE_MILLIS, 250); 
    	if( warnMillis > 0 ) {
            long totalTime = System.currentTimeMillis() - start;
            if( totalTime > warnMillis ) {
            	log.info("**LONG STRATEGY** Strategy took "+totalTime+" ms to run. (> warning threshold of "+warnMillis+"ms) Context: "+context);
            }
    	}

		debug.debug("evaluateStrategies: exit result = ",result);
		return result;
	}
	
	/** Private method that takes an input Set of strategies and iterates through the set
	 * looking for a positive result match.
	 * 
	 * @param context
	 * @param strategies
	 * @return the StrategyResult of executing the strategies, possibly null
	 */
	private StrategyResult evaluateStrategies(final StrategyContext context, final Set<Strategy> strategies) {
		StrategyResult result = null;
		
		for(Strategy strat : strategies) {
			// mode strategies are always allowed. otherwise, check to see if we are allowed
			// to process strategies based on the current modes
			if( !(strat instanceof ModeStrategy) && !context.isStrategyProcessingAllowed() ) {
				logVerboseStrategy(strat, "Strategy skipped due to current mode settings");
				continue;
			}
			
			result = strat.evaluate(context);
			logStrategyResult(strat, result);
			if( result != null && result.isSuccess() )
				break;
		}
		
		return result;
	}
	
	protected boolean isVerbose() {
		return plugin.getConfig().getBoolean(ConfigOptions.STRATEGY_VERBOSE_LOGGING, false);
	}
	
	protected void logVerbose(final Object...args) {
		if( isVerbose() || debug.isDebug() ) {
			final StringBuilder sb = new StringBuilder(logPrefix);
			if( !logPrefix.endsWith(" ") )
				sb.append(" ");
			
			for(int i=0; i<args.length;i++) {
				sb.append(args[i]);
			}
			
			final String msg = sb.toString();
			if( isVerbose() )
				log.info(msg);
			if( debug.isDebug() )
				debug.debug(msg);
		}
	}
	
	/** Log a verbose message, including the strategy identifier.
	 * 
	 * @param strat
	 * @param args
	 */
	protected void logVerboseStrategy(final Strategy strat, final Object...args) {
		if( isVerbose() ) {
			final String prefix = "(strategy " + strat.getStrategyConfigName() + ") ";
			Object[] newArgs = prepend(args, prefix);
			logVerbose(newArgs);
		}
	}
	
	protected void logStrategyResult(final Strategy strat, final StrategyResult result) {
		// we ignore logging for mode strategies
		if( !(strat instanceof ModeStrategy) )
			logVerboseStrategy(strat, "result is ", result);
	}
	
	/** prepend an element in front of an existing array, creating and returning
	 * the new array.
	 * 
	 * @param arr
	 * @param firstElement
	 * @return
	 */
	static private <T> T[] prepend(T[] arr, T firstElement) {
	    final int N = arr.length;
	    arr = java.util.Arrays.copyOf(arr, N+1);
	    System.arraycopy(arr, 0, arr, 1, N);
	    arr[0] = firstElement;
	    return arr;
	}
}
