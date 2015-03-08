/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
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
 */
/**
 *
 */
package com.andune.minecraft.hsp.strategy;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.ConfigurationSection;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Server;
import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.hsp.config.ConfigEvents;
import com.andune.minecraft.hsp.integration.worldguard.WorldGuard;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * Class which processes the config file and loads strategy info into memory
 * for use when evaluating strategies at runtime.
 *
 * @author andune
 */
@Singleton
public class StrategyConfigImpl implements StrategyConfig {
    private static final Logger log = LoggerFactory.getLogger(StrategyConfigImpl.class);

    private final StrategyMap defaultStrategies;
    private final Map<String, WorldStrategies> worldStrategies;
    private final Map<String, PermissionStrategies> permissionStrategies;

    private final Server server;
    private final ConfigEvents configEvents;
    private final StrategyFactory strategyFactory;
    private final WorldGuard worldGuard;

    @Inject
    public StrategyConfigImpl(final Server server, ConfigEvents configEvents, StrategyFactory strategyFactory,
                              WorldGuard worldGuard) {
        this.server = server;
        this.configEvents = configEvents;
        this.strategyFactory = strategyFactory;
        this.worldGuard = worldGuard;

        defaultStrategies    = new StrategyMap();
        worldStrategies      = new HashMap<String, WorldStrategies>();
        permissionStrategies = new LinkedHashMap<String, PermissionStrategies>();
    }


    @Override
    public void init() throws Exception {
        loadConfig();
    }

    @Override
    public void shutdown() throws Exception {
        defaultStrategies.clear();
        worldStrategies.clear();
        permissionStrategies.clear();
    }

    @Override
    public int getInitPriority() {
        return 12;
    }

    /**
     * Given an eventType, check to see if it's related to a region
     * and if so, register interest in that region.
     *
     * @param eventType
     * @param worldContext the world context, if any (can be null)
     */
    private void checkTypeForRegion(final String eventType, final String worldContext) {
        log.debug("checkTypeForRegion() eventType={}", eventType);
        int index = eventType.indexOf(';');
        if (index == -1)
            return;

        final String lcEventType = eventType.toLowerCase();
        if (lcEventType.startsWith(EventType.ENTER_REGION.toString().toLowerCase())
                || lcEventType.startsWith(EventType.EXIT_REGION.toString().toLowerCase())) {
            String region = eventType.substring(index + 1);
            World world = null;
            int commaIndex = region.indexOf(',');
            if (commaIndex != -1) {
                String worldName = region.substring(commaIndex + 1);
                world = server.getWorld(worldName);
                if (world == null)
                    log.warn("eventType {} references non-existant world {}", eventType, worldName);
                region = region.substring(0, commaIndex);
            } else if (worldContext != null) {
                world = server.getWorld(worldContext);
            }

            if (worldGuard.isEnabled()) {
                worldGuard.registerRegion(world, region);
            } else {
                log.warn("eventType {} depends on WorldGuard which is not present or enabled. Skipping.", eventType);
            }
        }
    }

    /**
     * Called to load strategies out of the config and into run-time variables.
     */
    @Override
    public void loadConfig() {
        log.debug("loadConfig() enter");

        // clear out any existing data before loading from the config
        defaultStrategies.clear();
        worldStrategies.clear();
        permissionStrategies.clear();

        try {
            configEvents.init();
        } catch(Exception e) {
            log.warn("Error when attempting to reload configEvents", e);
        }

        ConfigurationSection section = configEvents.getBaseSection();
        loadDefaultStrategies(section);

        section = configEvents.getWorldSection();
        loadWorldStrategies(section);

        section = configEvents.getPermissionSection();
        loadPermissionStrategies(section);

        log.debug("loadConfig() finished loading");
    }

    private void loadDefaultStrategies(ConfigurationSection section) {
        if (section == null) {
            log.debug("loadDefaultStrategies() default section is null, skipping");
            return;
        }

        int count = 0;
        log.debug("loadDefaultStrategies() loading default strategies");

        Set<String> eventTypes = section.getKeys();
        for (String eventType : eventTypes) {
            // skip special sections
            // this is an ugly leakage of concerns from the config object, and a good
            // reason to eventually move the majority of this functionality into
            // the config object instead. 12/22/12 -andune
            if (eventType.equalsIgnoreCase(ConfigEvents.SETTING_EVENTS_PERMBASE)
                    || eventType.equalsIgnoreCase(ConfigEvents.SETTING_EVENTS_WORLDBASE))
                continue;

            // get config children, then use lowercase to make configs case-insensitive
            List<String> strategies = section.getStringList(eventType);
            
            count += loadStrategiesIntoMap(strategies, eventType, defaultStrategies, null);
        }

        log.debug("loadDefaultStrategies() loaded {} total default strategy objects", count);
    }

    private void loadWorldStrategies(ConfigurationSection section) {
        if (section == null) {
            log.debug("loadWorldStrategies() world section is null, skipping");
            return;
        }

        int count = 0;
        log.debug("loadWorldStrategies() loading world-specific strategies");

        Set<String> eventWorlds = section.getKeys();
        if (eventWorlds == null) {
            log.debug("loadWorldStrategies() world section keys are null, skipping");
            return;
        }

        for (String world : eventWorlds) {
            WorldStrategies worldStrat = worldStrategies.get(world);
            if (worldStrat == null) {
                worldStrat = new WorldStrategies();
                worldStrategies.put(world, worldStrat);
            }

            ConfigurationSection worldSection = section.getConfigurationSection(world);
            Set<String> types = worldSection.getKeys();
            if (types != null && types.size() > 0) {
                for (String eventType : types) {
                    // get config children, then use lowercase to make configs case-insensitive
                    List<String> strategies = section.getStringList(world + "." + eventType);
                    
                    count += loadStrategiesIntoMap(strategies, eventType, worldStrat, world);
                }
            }
        }

        log.debug("loadWorldStrategies() loaded {} total world strategy objects", count);
    }

    /**
     * Load permission strategies.
     *
     * @return
     */
    private void loadPermissionStrategies(ConfigurationSection section) {
        if (section == null) {
            log.debug("loadPermissionStrategies() permission section is null, skipping");
            return;
        }

        int count = 0;
        log.debug("loadPermissionStrategies() loading permission-specific strategies");

        Set<String> permEntries = section.getKeys();
        if (permEntries == null) {
            log.debug("loadPermissionStrategies() permission section keys are null, skipping");
            return;
        }

        for (String entry : permEntries) {
            List<String> perms = section.getStringList(entry + ".permissions");

            // if no explicit permissions were defined, then setup the default ones
            if (perms == null || perms.isEmpty()) {
                perms = new ArrayList<String>(3);

                perms.add("hsp.events." + entry);               // add basePath permission
                perms.add("hsp.entry." + entry);                // add default entry permission
                perms.add("group." + entry);                    // add convenience group entry
            }

            PermissionStrategies permStrat = permissionStrategies.get(entry);
            if (permStrat == null) {
                permStrat = new PermissionStrategies();
                permissionStrategies.put(entry, permStrat);
            }

            // assign defined permissions to this entry
            permStrat.permissions = perms;

            ConfigurationSection entrySection = section.getConfigurationSection(entry);
            Set<String> entryKeys = entrySection.getKeys();

            for (String eventType : entryKeys) {
                // get config children, then use lowercase to make configs case-insensitive
                List<String> strategies = entrySection.getStringList(eventType);

                // skip the "permissions" entry
                if (eventType.equalsIgnoreCase("permissions"))
                    continue;

                count += loadStrategiesIntoMap(strategies, eventType, permStrat, null);
            }
        }

        log.debug("loadPermissionStrategies() loaded {} total permission strategy objects", count);
    }

    private int loadStrategiesIntoMap(List<String> strategies, String eventType, StrategyMap map, String world)
    {
       int count = 0;
       if (strategies != null && !strategies.isEmpty()) {
          checkTypeForRegion(eventType, world);

          String lcEventType = eventType.toLowerCase();
          StrategySet set = map.get(lcEventType);
          if (set == null) {
             set = new StrategySet();
             map.put(lcEventType, set);
          }

          for (String item : strategies) {
             try {
               log.debug("loadStrategiesIntoMap() loading strategy {} for event {} (class {})",
                       item, eventType, map.getClass().getSimpleName());
                Strategy strategy = strategyFactory.newStrategy(item);
                set.add(strategy);
                count++;
             } catch (StrategyException e) {
                log.warn("Error loading strategy " + item + ": " + e.getMessage(), e);
             }
          }
       }
       return count;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.strategy.StrategyConfig#getDefaultStrategies(java.lang.String)
     */
    @Override
    public Set<Strategy> getDefaultStrategies(final String event) {
        return defaultStrategies.get(event);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.strategy.StrategyConfig#getPermissionStrategies(java.lang.String, com.andune.minecraft.hsp.server.api.Player)
     */
    @Override
    public List<Set<Strategy>> getPermissionStrategies(final String event, final Player player) {
        List<Set<Strategy>> strategies = new ArrayList<Set<Strategy>>();

        for (PermissionStrategies strat : permissionStrategies.values()) {
            for (String perm : strat.permissions) {
                log.debug("checking permission {}", perm);
                if (player.hasPermission(perm)) {
                    log.debug("player {} does have perm {}, looking up strategies", player, perm);
                    Set<Strategy> set = strat.get(event);
                    if (set != null)
                        strategies.add(set);
                    break;
                }
            }
        }

        return strategies;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.strategy.StrategyConfig#getWorldStrategies(java.lang.String, java.lang.String)
     */
    @Override
    public Set<Strategy> getWorldStrategies(final String event, final String world) {
        WorldStrategies worldStrats = worldStrategies.get(world);
        if (worldStrats != null)
            return worldStrats.get(event);
        else
            return null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.strategy.StrategyConfig#getPermissionStrategyCount()
     */
    @Override
    public int getPermissionStrategyCount() {
        int count = 0;
        for (EventType type : EventType.values()) {
            for (PermissionStrategies strat : permissionStrategies.values()) {
                Set<Strategy> set = strat.get(type.toString());
                if (set != null) {
                    for (@SuppressWarnings("unused") Strategy s : set) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.strategy.StrategyConfig#getWorldStrategyCount()
     */
    @Override
    public int getWorldStrategyCount() {
        int count = 0;
        for (EventType type : EventType.values()) {
            for (WorldStrategies strat : worldStrategies.values()) {
                Set<Strategy> set = strat.get(type.toString());
                if (set != null) {
                    for (@SuppressWarnings("unused") Strategy s : set) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.strategy.StrategyConfig#getDefaultStrategyCount()
     */
    @Override
    public int getDefaultStrategyCount() {
        int count = 0;
        for (Set<Strategy> set : defaultStrategies.values()) {
            for (@SuppressWarnings("unused") Strategy s : set) {
                count++;
            }
        }
        return count;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.strategy.StrategyConfig#getStrategyCountMap()
     */
    @Override
    public Map<String, Integer> getStrategyCountMap() {
        Map<String, Integer> map = new TreeMap<String, Integer>();

        for (EventType type : EventType.values()) {
            // for each permission strategy, increment strategy counter
            for (PermissionStrategies strat : permissionStrategies.values()) {
                Set<Strategy> set = strat.get(type.toString());
                incrementStrategyCounters(map, set);
            }

            // for each world strategy, increment strategy counter
            for (WorldStrategies strat : worldStrategies.values()) {
                Set<Strategy> set = strat.get(type.toString());
                incrementStrategyCounters(map, set);
            }
        }

        // for each default strategy, increment strategy counter
        for (Set<Strategy> set : defaultStrategies.values()) {
            incrementStrategyCounters(map, set);
        }

        return map;
    }

    private void incrementStrategyCounters(Map<String, Integer> map, Set<Strategy> set) {
        if (set == null)
            return;

        for (Strategy s : set) {
            Integer i = map.get(s.getStrategyConfigName());
            i = (i != null) ? i++ : 1;
            map.put(s.getStrategyConfigName(), i);
        }
    }
    
    private static class StrategySet extends LinkedHashSet<Strategy>
    {
    }
    
    private static class StrategyMap extends LinkedHashMap<String, StrategySet>
    {
    }
    
    private static class WorldStrategies extends StrategyMap
    {
    }

    private static class PermissionStrategies extends StrategyMap
    {
        List<String> permissions;
    }
}
