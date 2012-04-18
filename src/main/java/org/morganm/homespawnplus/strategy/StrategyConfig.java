/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.util.Debug;

/** Class which processes the config file and loads strategy info into memory
 * for use when evaluating strategies at runtime.
 * 
 * @author morganm
 *
 */
public class StrategyConfig {
	private final HomeSpawnPlus plugin;
	private final Debug debug = Debug.getInstance();
	private final Map<EventType, Set<Strategy>> defaultStrategies;
	private final Map<String, WorldStrategies> worldStrategies;
	private final Map<String, PermissionStrategies> permissionStrategies;
	
	public StrategyConfig(final HomeSpawnPlus plugin) {
		this.plugin = plugin;
		defaultStrategies = new HashMap<EventType, Set<Strategy>>();
		worldStrategies = new HashMap<String, WorldStrategies>();
		permissionStrategies = new LinkedHashMap<String, PermissionStrategies>();
	}
	
	/** Called to load strategies out of the config and into run-time variables.
	 * 
	 */
	public void loadConfig() {
		// clear out any existing data before loading from the config
		defaultStrategies.clear();
		worldStrategies.clear();
		permissionStrategies.clear();
		
		final FileConfiguration config = plugin.getConfig();
		final EventType[] eventTypes = EventType.values();
		
		debug.debug("loadConfig() loading default strategies");
		// load default strategies
		int count=0;
		for(int i=0; i < eventTypes.length; i++) {
			List<String> strategies = config.getStringList(ConfigOptions.SETTING_EVENTS_BASE + "." + eventTypes[i].getConfigOption());
			
			if( strategies != null && strategies.size() > 0) {
				Set<Strategy> set = defaultStrategies.get(eventTypes[i]);
				if( set == null ) {
					set = new LinkedHashSet<Strategy>();
					defaultStrategies.put(eventTypes[i], set);
				}

				for(String item : strategies) {
					try {
						Strategy strategy = StrategyFactory.newStrategy(item);
						set.add(strategy);
						count++;
					} catch (StrategyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		debug.debug("loadConfig() loaded ",count," total default strategy objects");
		count=0;
		
		debug.debug("loadConfig() loading world-specific strategies");
		// now load world-specific strategies
		ConfigurationSection worldSection = config.getConfigurationSection(ConfigOptions.SETTING_EVENTS_BASE + "." + ConfigOptions.SETTING_EVENTS_WORLDBASE);
		Set<String> eventWorlds = null;
		if( worldSection != null )
			eventWorlds = worldSection.getKeys(false);
		if( eventWorlds != null ) {
			for(String world : eventWorlds) {
				for(int i=0; i < eventTypes.length; i++) {
					List<String> strategies = config.getStringList(ConfigOptions.SETTING_EVENTS_BASE
							+ "." + ConfigOptions.SETTING_EVENTS_WORLDBASE
							+ "." + world
							+ "." + eventTypes[i].getConfigOption());
					
					if( strategies != null && strategies.size() > 0 ) {
						WorldStrategies worldStrat = worldStrategies.get(world);
						if( worldStrat == null ) {
							worldStrat = new WorldStrategies();
							worldStrategies.put(world, worldStrat);
						}
						
						Set<Strategy> set = worldStrat.eventStrategies.get(eventTypes[i]);
						if( set == null ) {
							set = new LinkedHashSet<Strategy>();
							worldStrat.eventStrategies.put(eventTypes[i], set);
						}

						for(String item : strategies) {
							try {
								Strategy strategy = StrategyFactory.newStrategy(item);
								set.add(strategy);
								count++;
							} catch (StrategyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		debug.debug("loadConfig() loaded ",count," total world strategy objects");
		count=0;
		
		debug.debug("loadConfig() loading permission-specific strategies");
		// now load permission-specific strategies
		ConfigurationSection permSection = config.getConfigurationSection(ConfigOptions.SETTING_EVENTS_BASE
				+ "." + ConfigOptions.SETTING_EVENTS_PERMBASE);
		Set<String> permEntries = null;
		if( permSection != null )
			permEntries = permSection.getKeys(false);
		if( permEntries != null ) {
			for(String entry : permEntries) {
				
				List<String> perms = config.getStringList(ConfigOptions.SETTING_EVENTS_BASE
						+ "." + ConfigOptions.SETTING_EVENTS_PERMBASE
						+ "." + entry
						+ ".permissions");
				
				// if no permissions are defined, we're done with this entry
				if( perms == null || perms.size() == 0 )
					continue;
				
				PermissionStrategies permStrat = permissionStrategies.get(entry);
				if( permStrat == null ) {
					permStrat = new PermissionStrategies();
					permissionStrategies.put(entry, permStrat);
				}

				// assign defined permissions to this entry
				permStrat.permissions = perms;
				
				// now loop for each event type to load event strategies
				for(int i=0; i < eventTypes.length; i++) {
					List<String> strategies = config.getStringList(ConfigOptions.SETTING_EVENTS_BASE
							+ "." + ConfigOptions.SETTING_EVENTS_PERMBASE
							+ "." + entry
							+ "." + eventTypes[i].getConfigOption());
					if( strategies != null && strategies.size() > 0 ) {
						Set<Strategy> set = permStrat.eventStrategies.get(eventTypes[i]);
						if( set == null ) {
							set = new LinkedHashSet<Strategy>();
							permStrat.eventStrategies.put(eventTypes[i], set);
						}

						for(String item : strategies) {
							try {
								Strategy strategy = StrategyFactory.newStrategy(item);
								set.add(strategy);
								count++;
							} catch (StrategyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
			
		}
		debug.debug("loadConfig() loaded ",count," total permission strategy objects");
		count=0;

		debug.debug("loadConfig() finished loading");
	}
	
	/** Given a specific event type, return the default strategy chain associated with
	 * that event.
	 * The returned set will use a Set implementation that guarantees ordering and whose
	 * order is consistent with that of the underlying config.
	 * 
	 * @param event
	 * @return
	 */
	public Set<Strategy> getDefaultStrategies(final EventType event) {
		return defaultStrategies.get(event);
	}
	
	/** Given a specific event type and player, return all matching permission strategies.
	 * The returned set will use a Set implementation that guarantees ordering and whose
	 * order is consistent with that of the underlying config.
	 * 
	 * @param event
	 * @param player
	 * @return guaranteed to not be null
	 */
	public List<Set<Strategy>> getPermissionStrategies(final EventType event, final Player player) {
		List<Set<Strategy>> strategies = new ArrayList<Set<Strategy>>();
		
		for(PermissionStrategies strat : permissionStrategies.values()) {
			for(String perm : strat.permissions) {
				debug.debug("checking permission ",perm);
				if( plugin.hasPermission(player, perm) ) {
					debug.debug("player ",player," does have perm ",perm,", looking up strategies");
					Set<Strategy> set = strat.eventStrategies.get(event);
					if( set != null )
						strategies.add(set);
				}
			}
		}
		
		return strategies;
	}
	
	/** Given a specific event type and world, return all matching world strategies.
	 * The returned set will use a Set implementation that guarantees ordering and whose
	 * order is consistent with that of the underlying config.
	 * 
	 * @param event
	 * @param player
	 * @return null if no world strategies exist for the world
	 */
	public Set<Strategy> getWorldStrategies(final EventType event, final String world) {
		WorldStrategies worldStrats = worldStrategies.get(world);
		if( worldStrats != null )
			return worldStrats.eventStrategies.get(event);
		else
			return null;
	}
	
	private class WorldStrategies {
//		String worldName;
		Map<EventType, Set<Strategy>> eventStrategies = new HashMap<EventType, Set<Strategy>>();
	}
	
	private class PermissionStrategies {
//		String configNode;
		List<String> permissions;
		Map<EventType, Set<Strategy>> eventStrategies = new HashMap<EventType, Set<Strategy>>();
	}
}
