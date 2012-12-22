/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.Initializable;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * @author morganm
 *
 */
@Singleton
public class StrategyFactory implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(StrategyFactory.class);

	/*
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
        oneArgStrategies.put(new ModeDistanceLimits(null).getStrategyConfigName().toLowerCase(), ModeDistanceLimits.class);
	}
	*/
	
	private final Injector injector;
	private final Reflections reflections;
    private final Map<String, Class<? extends Strategy>> noArgStrategies;
    private final Map<String, Class<? extends Strategy>> oneArgStrategies;
    
	
	@Inject
	public StrategyFactory(Injector injector, Reflections reflections) {
	    this.injector = injector;
	    this.reflections = reflections;

        noArgStrategies = new HashMap<String, Class<? extends Strategy>>(30);
        oneArgStrategies = new HashMap<String, Class<? extends Strategy>>(15);
	}

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void init() throws Exception {
        initStrategies(NoArgStrategy.class, noArgStrategies);
        initStrategies(OneArgStrategy.class, oneArgStrategies);
    }
	
    @SuppressWarnings("unchecked")
    private void initStrategies(Class<? extends Annotation> annotationClass,
            Map<String, Class<? extends Strategy>> targetMap)
            throws InstantiationException
    {
        log.debug("initStrategies(): invoked");

        Set<Class<?>> oneArgStrats = reflections.getTypesAnnotatedWith(annotationClass);
        for(Class<?> clazz : oneArgStrats) {
            if( Strategy.class.isAssignableFrom(clazz) ) {
                Class<? extends Strategy> strategyClass = (Class<? extends Strategy>) clazz;
                log.debug("initStrategies(): adding {} to strategy map", strategyClass);

                Strategy strategyObject = null;

                // we are only instantiating the strategy to grab it's configName,
                // so we don't IoC Inject it here since we don't care about dependencies,
                // the object will be discarded immediately.
                // Just loop through available constructors to find one that will
                // load the current object; we don't care about anything except that
                // the loaded object can respond to getStrategyConfigName()
                Constructor<?>[] constructors = strategyClass.getConstructors(); 
                for(int i=0; i < constructors.length; i++) {
                    try {
                        if( constructors[i].getParameterTypes().length == 0 ) {
                            strategyObject = (Strategy) constructors[i].newInstance();
                        }
                        else {
                            Object[] args = new Object[constructors[i].getParameterTypes().length];
                            strategyObject = (Strategy) constructors[i].newInstance(args);
                        }
                    }
                    catch(Exception e) {
                        // silently catch and drop any exception, we just try
                        // each constructor
                    }
                }
                
                if( strategyObject == null )
                    throw new InstantiationException("Could not instantiate class "+strategyClass);
                
                targetMap.put(strategyObject.getStrategyConfigName().toLowerCase(), strategyClass);
            }
        }
    }

	/** Given a Strategy class, return an instantiated instance of that class.
	 * 
	 * @param clazz
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Strategy newStrategy(Class<? extends Strategy> clazz)
			throws StrategyException
	{
		try {
            Strategy strategy = injector.getInstance(clazz);
//			Strategy strategy = clazz.newInstance();
//            injector.injectMembers(strategy);
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
	
	public Strategy newStrategy(Class<? extends Strategy> clazz, String arg)
			throws StrategyException
	{
		try {
		    Strategy strategy = injector.getInstance(clazz);
//			Constructor<? extends Strategy> constructor = clazz.getConstructor(String.class);
//			Strategy strategy = constructor.newInstance(arg);
//			injector.injectMembers(strategy);
			
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
	public Strategy newStrategy(final String strategyName) throws StrategyException {
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
