package org.morganm.homespawnplus.strategy;

import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;

public interface StrategyEngine {

    /** Convenience method for routines only interested in an output location (as
     * opposed to other result details).
     * 
     * @param event
     * @param player
     * @return
     */
    public StrategyResult getStrategyResult(StrategyContext context, String... args);

    public Location getStrategyLocation(EventType event, Player player, String... args);

    public StrategyResult getStrategyResult(String event, Player player, String... args);

    public StrategyResult getStrategyResult(EventType event, Player player, String... args);

    /** Given a StrategyContext, evaluate the strategies for that context.
     * 
     * @param context
     */
    public StrategyResult evaluateStrategies(StrategyContext context);
}