package com.andune.minecraft.hsp.strategy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.andune.minecraft.hsp.server.api.Player;
import com.andune.minecraft.hsp.strategy.Strategy;

/**
 * Interface class that exists so Guice can create a temporary proxy to avoid
 * circular dependencies. Only one implementation exists: StrategyConfigImpl
 * 
 * @author morganm
 *
 */
public interface StrategyConfig {

    /** Given a specific event type, return the default strategy chain associated with
     * that event.
     * The returned set will use a Set implementation that guarantees ordering and whose
     * order is consistent with that of the underlying config.
     * 
     * @param event
     * @return
     */
    public Set<Strategy> getDefaultStrategies(String event);

    /** Given a specific event type and player, return all matching permission strategies.
     * The returned set will use a Set implementation that guarantees ordering and whose
     * order is consistent with that of the underlying config.
     * 
     * @param event
     * @param player
     * @return guaranteed to not be null
     */
    public List<Set<Strategy>> getPermissionStrategies(String event,
            Player player);

    /** Given a specific event type and world, return all matching world strategies.
     * The returned set will use a Set implementation that guarantees ordering and whose
     * order is consistent with that of the underlying config.
     * 
     * @param event
     * @param player
     * @return null if no world strategies exist for the world
     */
    public Set<Strategy> getWorldStrategies(String event, String world);

    /** Metrics: return total number of Permission-related strategies.
     * 
     * @return
     */
    public int getPermissionStrategyCount();

    /** Metrics: return total number of World-related strategies.
     * 
     * @return
     */
    public int getWorldStrategyCount();

    /** Metrics: return total number of default strategies.
     * 
     * @return
     */
    public int getDefaultStrategyCount();

    /** For metrics, return the count of each strategy that is in use.
     * 
     * @return
     */
    public Map<String, Integer> getStrategyCountMap();

}