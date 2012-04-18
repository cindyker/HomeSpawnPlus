/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import org.morganm.homespawnplus.HomeSpawnPlus;

/** Strategy interface for all strategies.
 * 
 * @author morganm
 *
 */
public interface Strategy {
	/** This method is responsible for evaluating this strategy, implementing
	 * whatever behavior is appropriate for the strategy.
	 * 
	 * @param context
	 * @return a strategy result or null if no result was found for this strategy
	 */
	public StrategyResult evaluate(StrategyContext context);
	
	/** This is the configuration name for the strategy to be used via the
	 * config file.
	 * 
	 * @return
	 */
	public String getStrategyConfigName();
	
	/** The StrategyFactory framework guarantees all strategies will have a plugin
	 * reference to work with.
	 * 
	 * @param plugin
	 */
	public void setPlugin(HomeSpawnPlus plugin);
	
	/** If this is a "default strategy", that means it is not associated
	 * to a specific world or specific permission nodes.
	 * 
	 * @return true if this is a default strategy node
	 */
//	public boolean isDefaultStrategy();
	
	/** If this strategy instance is specific to permission nodes, this
	 * method will return them. If it is not, this will return null.
	 * 
	 * @return
	 */
//	public Set<String> getPermissions();
	
	/** If this strategy instance is specific to a world, this method
	 * will return that world name. If it is not, this will return null.
	 * 
	 * @return
	 */
//	public String getWorld();
}
