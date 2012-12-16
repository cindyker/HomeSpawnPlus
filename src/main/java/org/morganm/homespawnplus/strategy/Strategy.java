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
//	public void setPlugin(OldHSP plugin);
	
	/** This method should be called after Strategy instantiation so the strategy
	 * can do validation on it's inputs. This should be called AFTER setPlugin() so
	 * the strategy can count on a valid plugin reference to do it's work.
	 * 
	 * If the strategy is invalid and should not be used, it should throw a
	 * StrategyException explaining why so the admin can fix any issue.
	 */
	public void validate() throws StrategyException;
	
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
