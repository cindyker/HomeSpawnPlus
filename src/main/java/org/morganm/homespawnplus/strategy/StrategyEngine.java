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
import org.morganm.homespawnplus.util.Debug;

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
	public Location getStrategyLocation(EventType event, Player player, String...args) {
    	StrategyContext context = new StrategyContext();
    	context.setPlayer(player);
    	context.setSpawnEventType(event);
    	if( args != null && args.length > 0 )
    		context.setArg(args[0]);
    	
    	StrategyResult result = plugin.getStrategyEngine().evaluateStrategies(context);
    	if( result != null && result.getLocation() != null )
    		return result.getLocation();
    	else
    		return null;
	}
	
	/** Given a StrategyContext, evaluate the strategies for that context.
	 * 
	 * @param context
	 */
	public StrategyResult evaluateStrategies(StrategyContext context) {
		StrategyResult result = null;
		
		debug.debug("evaluateStrategies: INVOKED. type=",context.getEventType()," player=",context.getPlayer());
		
		debug.debug("evaluateStrategies: evaluating permission-based strategies");
		List<Set<Strategy>> permStrategies = strategyConfig.getPermissionStrategies(context.getEventType(), context.getPlayer());
		if( permStrategies != null && permStrategies.size() > 0 ) {
			debug.debug("evaluateStrategies: evaluating ",permStrategies.size()," permission strategies");
			LOOP:
			for(Set<Strategy> set : permStrategies) {
				context.resetCurrentModes();
				
				result = evaluateStrategies(context, set);
				if( result != null && result.isSuccess() )
					break LOOP;
			}
		}
		debug.debug("evaluateStrategies: permission-based strategies result = ",result);

		// need to check world strategies if we don't yet have a successful result
		if( result == null || (result != null && !result.isSuccess()) ) {
			// is it possible for player to have a null world when they first login? not sure
			// but lets be sure we don't blow up if it is. Only process world strategy if
			// player is in a world.
			if( context.getPlayer().getWorld() != null ) {
				debug.debug("evaluateStrategies: evaluating world-based strategies");
				Set<Strategy> worldStrategies = strategyConfig.getWorldStrategies(context.getEventType(), context.getPlayer().getWorld().getName());
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
		
		if( result == null )
			log.warning(logPrefix + " Warning: no event strategy defined for event "+context.getEventType()+". If this is intentional, just define the event in config.yml with the single strategy \"default\" to avoid this warning.");
		else {
			if( result.isExplicitDefault() )
				logVerbose("Evaluation chain complete, result = explicit default");
			else
				logVerbose("Evaluation chain complete, result = ", result);
		}

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
		if( isVerbose() ) {
			final StringBuilder sb = new StringBuilder(logPrefix);
			if( !logPrefix.endsWith(" ") )
				sb.append(" ");
			
			for(int i=0; i<args.length;i++) {
				sb.append(args[i]);
			}
			
			log.info(sb.toString());
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
		if( !(strat instanceof HomeModeStrategy) )
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
