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
import org.morganm.homespawnplus.strategies.ModeDefault;
import org.morganm.homespawnplus.strategies.ModeExcludeNewPlayerSpawn;
import org.morganm.homespawnplus.strategies.ModeHomeAny;
import org.morganm.homespawnplus.strategies.ModeHomeBedOnly;
import org.morganm.homespawnplus.strategies.ModeHomeDefaultOnly;
import org.morganm.homespawnplus.strategies.ModeHomeNoBed;
import org.morganm.homespawnplus.strategies.ModeHomeNormal;
import org.morganm.homespawnplus.strategies.ModeHomeRequiresBed;
import org.morganm.homespawnplus.strategies.ModeInRegion;
import org.morganm.homespawnplus.strategies.ModeMultiverseDestinationPortal;
import org.morganm.homespawnplus.strategies.ModeMultiverseSourcePortal;
import org.morganm.homespawnplus.strategies.ModeNoIce;
import org.morganm.homespawnplus.strategies.ModeNoLeaves;
import org.morganm.homespawnplus.strategies.ModeNoLilyPad;
import org.morganm.homespawnplus.strategies.ModeNoWater;
import org.morganm.homespawnplus.strategies.ModeRememberLocation;
import org.morganm.homespawnplus.strategies.ModeRememberSpawn;
import org.morganm.homespawnplus.strategies.ModeSourceWorld;
import org.morganm.homespawnplus.strategies.ModeYBounds;
import org.morganm.homespawnplus.strategies.NearestHomeOrSpawn;
import org.morganm.homespawnplus.strategies.SpawnDefaultWorld;
import org.morganm.homespawnplus.strategies.SpawnGroup;
import org.morganm.homespawnplus.strategies.SpawnGroupSpecificWorld;
import org.morganm.homespawnplus.strategies.SpawnLastLocation;
import org.morganm.homespawnplus.strategies.SpawnLocalPlayerSpawn;
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
		noArgStrategies.put(new HomeAnyWorld().getStrategyConfigName().toLowerCase(), HomeAnyWorld.class);
		noArgStrategies.put(new HomeDefaultWorld().getStrategyConfigName().toLowerCase(), HomeDefaultWorld.class);
		noArgStrategies.put(new HomeLocalWorld().getStrategyConfigName().toLowerCase(), HomeLocalWorld.class);
		noArgStrategies.put(new HomeNearestHome().getStrategyConfigName().toLowerCase(), HomeNearestHome.class);
		
		noArgStrategies.put(new ModeHomeAny().getStrategyConfigName().toLowerCase(), ModeHomeAny.class);
		noArgStrategies.put(new ModeHomeBedOnly().getStrategyConfigName().toLowerCase(), ModeHomeBedOnly.class);
		noArgStrategies.put(new ModeHomeDefaultOnly().getStrategyConfigName().toLowerCase(), ModeHomeDefaultOnly.class);
		noArgStrategies.put(new ModeHomeNoBed().getStrategyConfigName().toLowerCase(), ModeHomeNoBed.class);
		noArgStrategies.put(new ModeHomeNormal().getStrategyConfigName().toLowerCase(), ModeHomeNormal.class);
		noArgStrategies.put(new ModeHomeRequiresBed().getStrategyConfigName().toLowerCase(), ModeHomeRequiresBed.class);
		noArgStrategies.put(new ModeNoWater().getStrategyConfigName().toLowerCase(), ModeNoWater.class);
		noArgStrategies.put(new ModeNoLilyPad().getStrategyConfigName().toLowerCase(), ModeNoLilyPad.class);
		noArgStrategies.put(new ModeNoLeaves().getStrategyConfigName().toLowerCase(), ModeNoLeaves.class);
		noArgStrategies.put(new ModeNoIce().getStrategyConfigName().toLowerCase(), ModeNoIce.class);
		noArgStrategies.put(new ModeDefault().getStrategyConfigName().toLowerCase(), ModeDefault.class);
		noArgStrategies.put(new ModeRememberSpawn().getStrategyConfigName().toLowerCase(), ModeRememberSpawn.class);
		noArgStrategies.put(new ModeRememberLocation().getStrategyConfigName().toLowerCase(), ModeRememberLocation.class);
		noArgStrategies.put(new ModeExcludeNewPlayerSpawn().getStrategyConfigName().toLowerCase(), ModeExcludeNewPlayerSpawn.class);
		
		noArgStrategies.put(new SpawnDefaultWorld().getStrategyConfigName().toLowerCase(), SpawnDefaultWorld.class);
		noArgStrategies.put(new SpawnLocalRandom().getStrategyConfigName().toLowerCase(), SpawnLocalRandom.class);
		noArgStrategies.put(new SpawnLocalWorld().getStrategyConfigName().toLowerCase(), SpawnLocalWorld.class);
		noArgStrategies.put(new SpawnNearestSpawn().getStrategyConfigName().toLowerCase(), SpawnNearestSpawn.class);
		noArgStrategies.put(new SpawnNewPlayer().getStrategyConfigName().toLowerCase(), SpawnNewPlayer.class);
		noArgStrategies.put(new SpawnWorldGuardRegion().getStrategyConfigName().toLowerCase(), SpawnWorldGuardRegion.class);
		noArgStrategies.put(new SpawnLocalPlayerSpawn().getStrategyConfigName().toLowerCase(), SpawnLocalPlayerSpawn.class);
		noArgStrategies.put(new Default().getStrategyConfigName().toLowerCase(), Default.class);
		try {
			noArgStrategies.put(new HomeMultiWorld().getStrategyConfigName().toLowerCase(), HomeMultiWorld.class);
			noArgStrategies.put(new SpawnGroup().getStrategyConfigName().toLowerCase(), SpawnGroup.class);
			noArgStrategies.put(new NearestHomeOrSpawn().getStrategyConfigName().toLowerCase(), NearestHomeOrSpawn.class);
		}
		catch(StrategyException e) {
			e.printStackTrace();
		}
		
		oneArgStrategies = new HashMap<String, Class<? extends Strategy>>(10);
		
		// Special strategies that can be both a noArg and a 1-arg strategy
		noArgStrategies.put(new HomeNamedHome().getStrategyConfigName().toLowerCase(), HomeNamedHome.class);
		oneArgStrategies.put(new HomeNamedHome().getStrategyConfigName().toLowerCase(), HomeNamedHome.class);
		noArgStrategies.put(new SpawnNamedSpawn().getStrategyConfigName().toLowerCase(), SpawnNamedSpawn.class);
		oneArgStrategies.put(new SpawnNamedSpawn().getStrategyConfigName().toLowerCase(), SpawnNamedSpawn.class);
		noArgStrategies.put(new SpawnWorldRandom().getStrategyConfigName().toLowerCase(), SpawnWorldRandom.class);
		oneArgStrategies.put(new SpawnWorldRandom().getStrategyConfigName().toLowerCase(), SpawnWorldRandom.class);
		noArgStrategies.put(new SpawnLastLocation().getStrategyConfigName().toLowerCase(), SpawnLastLocation.class);
		oneArgStrategies.put(new SpawnLastLocation().getStrategyConfigName().toLowerCase(), SpawnLastLocation.class);

		// 1-arg Strategies
		oneArgStrategies.put(new HomeSpecificWorld(null).getStrategyConfigName().toLowerCase(), HomeSpecificWorld.class);
		oneArgStrategies.put(new SpawnSpecificWorld(null).getStrategyConfigName().toLowerCase(), SpawnSpecificWorld.class);
		oneArgStrategies.put(new SpawnGroupSpecificWorld(null).getStrategyConfigName().toLowerCase(), SpawnGroupSpecificWorld.class);
		oneArgStrategies.put(new SpawnRandomNamed(null).getStrategyConfigName().toLowerCase(), SpawnRandomNamed.class);
		oneArgStrategies.put(new SpawnRegionRandom(null).getStrategyConfigName().toLowerCase(), SpawnRegionRandom.class);
		
		oneArgStrategies.put(new ModeYBounds().getStrategyConfigName().toLowerCase(), ModeYBounds.class);
		oneArgStrategies.put(new ModeMultiverseSourcePortal(null).getStrategyConfigName().toLowerCase(), ModeMultiverseSourcePortal.class);
		oneArgStrategies.put(new ModeMultiverseDestinationPortal(null).getStrategyConfigName().toLowerCase(), ModeMultiverseDestinationPortal.class);
		oneArgStrategies.put(new ModeInRegion(null).getStrategyConfigName().toLowerCase(), ModeInRegion.class);
		oneArgStrategies.put(new ModeSourceWorld(null).getStrategyConfigName().toLowerCase(), ModeSourceWorld.class);
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
