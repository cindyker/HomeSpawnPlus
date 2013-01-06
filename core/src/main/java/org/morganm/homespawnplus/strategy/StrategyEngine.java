package org.morganm.homespawnplus.strategy;

import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;

/**
 * This interface was added primarily to prevent a circular reference in
 * dependency injection, which Guice can't work around unless there is an
 * interface to proxy temporarily. Although conceptually an interface would
 * also allow for different implementation options, I don't see a practical
 * use for that at this time given HSP's scope and the current Engine that
 * fulfills those needs. Thus Guice just binds this interface to the only
 * implementation that exists: StrategyEngineImpl
 * 
 * @author morganm
 *
 */
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