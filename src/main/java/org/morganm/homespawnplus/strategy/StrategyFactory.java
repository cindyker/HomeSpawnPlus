/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.strategies.Default;
import org.morganm.homespawnplus.strategies.HomeAnyWorld;
import org.morganm.homespawnplus.strategies.HomeDefaultWorld;
import org.morganm.homespawnplus.strategies.HomeLocalWorld;
import org.morganm.homespawnplus.strategies.HomeMultiWorld;
import org.morganm.homespawnplus.strategies.HomeNamedHome;
import org.morganm.homespawnplus.strategies.HomeNearestHome;
import org.morganm.homespawnplus.strategies.HomeSpecificWorld;
import org.morganm.homespawnplus.strategies.ModeHomeAny;
import org.morganm.homespawnplus.strategies.ModeHomeBedOnly;
import org.morganm.homespawnplus.strategies.ModeHomeDefaultOnly;
import org.morganm.homespawnplus.strategies.ModeHomeNoBed;
import org.morganm.homespawnplus.strategies.ModeHomeNormal;
import org.morganm.homespawnplus.strategies.ModeHomeRequiresBed;
import org.morganm.homespawnplus.strategies.SpawnDefaultWorld;
import org.morganm.homespawnplus.strategies.SpawnGroup;
import org.morganm.homespawnplus.strategies.SpawnGroupSpecificWorld;
import org.morganm.homespawnplus.strategies.SpawnLocalRandom;
import org.morganm.homespawnplus.strategies.SpawnLocalWorld;
import org.morganm.homespawnplus.strategies.SpawnNamedSpawn;
import org.morganm.homespawnplus.strategies.SpawnNearestSpawn;
import org.morganm.homespawnplus.strategies.SpawnNewPlayer;
import org.morganm.homespawnplus.strategies.SpawnRandomNamed;
import org.morganm.homespawnplus.strategies.SpawnRegionRandom;
import org.morganm.homespawnplus.strategies.SpawnSpecificWorld;
import org.morganm.homespawnplus.strategies.SpawnWorldGuardRegion;
import org.morganm.homespawnplus.strategies.SpawnWorldRandom;

/**
 * @author morganm
 *
 */
public class StrategyFactory {
	private static final Map<String, Class<? extends Strategy>> noArgStrategies;
	private static final Map<String, Class<? extends Strategy>> oneArgStrategies;
	
	/* Replace these static definitions with annotations at some point.
	 */
	static {
		noArgStrategies = new HashMap<String, Class<? extends Strategy>>(20);
		noArgStrategies.put(new HomeAnyWorld().getStrategyConfigName(), HomeAnyWorld.class);
		noArgStrategies.put(new HomeDefaultWorld().getStrategyConfigName(), HomeDefaultWorld.class);
		noArgStrategies.put(new HomeLocalWorld().getStrategyConfigName(), HomeLocalWorld.class);
		noArgStrategies.put(new HomeNearestHome().getStrategyConfigName(), HomeNearestHome.class);
		noArgStrategies.put(new ModeHomeAny().getStrategyConfigName(), ModeHomeAny.class);
		noArgStrategies.put(new ModeHomeBedOnly().getStrategyConfigName(), ModeHomeBedOnly.class);
		noArgStrategies.put(new ModeHomeDefaultOnly().getStrategyConfigName(), ModeHomeDefaultOnly.class);
		noArgStrategies.put(new ModeHomeNoBed().getStrategyConfigName(), ModeHomeNoBed.class);
		noArgStrategies.put(new ModeHomeNormal().getStrategyConfigName(), ModeHomeNormal.class);
		noArgStrategies.put(new ModeHomeRequiresBed().getStrategyConfigName(), ModeHomeRequiresBed.class);
		noArgStrategies.put(new SpawnDefaultWorld().getStrategyConfigName(), SpawnDefaultWorld.class);
		noArgStrategies.put(new SpawnLocalRandom().getStrategyConfigName(), SpawnLocalRandom.class);
		noArgStrategies.put(new SpawnLocalWorld().getStrategyConfigName(), SpawnLocalWorld.class);
		noArgStrategies.put(new SpawnNearestSpawn().getStrategyConfigName(), SpawnNearestSpawn.class);
		noArgStrategies.put(new SpawnNewPlayer().getStrategyConfigName(), SpawnNewPlayer.class);
		noArgStrategies.put(new SpawnWorldGuardRegion().getStrategyConfigName(), SpawnWorldGuardRegion.class);
		noArgStrategies.put(new Default().getStrategyConfigName(), Default.class);
		try {
			noArgStrategies.put(new HomeMultiWorld().getStrategyConfigName(), HomeMultiWorld.class);
			noArgStrategies.put(new SpawnGroup().getStrategyConfigName(), SpawnGroup.class);
		}
		catch(StrategyException e) {
			e.printStackTrace();
		}
		
		oneArgStrategies = new HashMap<String, Class<? extends Strategy>>(10);
		
		// Special strategies that can be both a noArg and a 1-arg strategy
		noArgStrategies.put(new HomeNamedHome().getStrategyConfigName(), HomeNamedHome.class);
		oneArgStrategies.put(new HomeNamedHome(null).getStrategyConfigName(), HomeNamedHome.class);
		noArgStrategies.put(new SpawnWorldRandom(null).getStrategyConfigName(), SpawnWorldRandom.class);
		oneArgStrategies.put(new SpawnWorldRandom(null).getStrategyConfigName(), SpawnWorldRandom.class);

		// 1-arg Strategies
		oneArgStrategies.put(new HomeSpecificWorld(null).getStrategyConfigName(), HomeSpecificWorld.class);
		oneArgStrategies.put(new SpawnNamedSpawn(null).getStrategyConfigName(), SpawnNamedSpawn.class);
		oneArgStrategies.put(new SpawnSpecificWorld(null).getStrategyConfigName(), SpawnSpecificWorld.class);
		oneArgStrategies.put(new SpawnGroupSpecificWorld(null).getStrategyConfigName(), SpawnGroupSpecificWorld.class);
		oneArgStrategies.put(new SpawnRandomNamed(null).getStrategyConfigName(), SpawnRandomNamed.class);
		oneArgStrategies.put(new SpawnRegionRandom(null).getStrategyConfigName(), SpawnRegionRandom.class);
	}
	
	
	/** Given a Strategy class, return an instantiated instance of that class.
	 * 
	 * @param clazz
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static Strategy newStrategy(Class<? extends Strategy> clazz)
			throws StrategyException
	{
		try {
			Strategy strategy = clazz.newInstance();
			strategy.setPlugin(HomeSpawnPlus.getInstance());
			strategy.validate();
			return strategy;
		}
		catch(StrategyException e) {
			throw e;	// just re-throw
		}
		catch(Exception e) {
			throw new StrategyException("Error instantiating new 0-arg strategy instance", e);
		}
	}
	
	public static Strategy newStrategy(Class<? extends Strategy> clazz, String arg)
			throws StrategyException
	{
		try {
			Constructor<? extends Strategy> constructor = clazz.getConstructor(String.class);
			Strategy strategy = constructor.newInstance(arg);
			
			strategy.setPlugin(HomeSpawnPlus.getInstance());
			strategy.validate();
			
			return strategy;
		}
		catch(StrategyException e) {
			throw e;	// just re-throw
		}
		catch(Exception e) {
			throw new StrategyException("Error instantiating new 1-arg strategy instance", e);
		}
	}

	/** Given a strategy name, match it to a Strategy object and return the instance.
	 * This is capable of parsing an argument, so for example:
	 * 
	 *   spawnNamedSpawn:spawn1
	 * 
	 * Will create a namedSpawn strategy object with the argument "spawn1".
	 * 
	 * @param strategyName
	 * @return
	 */
	public static Strategy newStrategy(final String strategyName) throws StrategyException {
		for(Entry<String, Class<? extends Strategy>> entry : oneArgStrategies.entrySet()) {
			if( strategyName.toLowerCase().startsWith(entry.getKey().toLowerCase()) ) {
				String[] strings = strategyName.split(":");
				if( strings.length < 2 ) {
					// check to see if this config also has a noArg version. If so, we don't
					// throw an error, we let the code fall through to the noArg checks below
					if( !noArgStrategies.containsKey(entry.getKey().toLowerCase()) )
						throw new StrategyException("Invalid strategy: "+strategyName+" (strategy requires argument)");
					else
						break;
				}
				
				return newStrategy(entry.getValue(), strings[1]);
			}
		}
		
		for(Entry<String, Class<? extends Strategy>> entry : noArgStrategies.entrySet()) {
			if( strategyName.toLowerCase().equals(entry.getKey().toLowerCase()) ) {
				return newStrategy(entry.getValue());
			}
		}
		
		throw new StrategyException("invalid strategy: "+strategyName);
	}
}
