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

import java.util.logging.Logger;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.config.old.ConfigOptions;
import org.morganm.homespawnplus.util.Debug;

/** Basic routines common/useful to most all strategies.
 * 
 * @author morganm
 *
 */
public abstract class BaseStrategy implements Strategy {
	protected HomeSpawnPlus plugin;
	protected Logger log;
	protected String logPrefix;
	protected final Debug debug = Debug.getInstance();

	@Override
	public String getStrategyConfigName() {
		return this.getClass().getSimpleName();
	}
	
	protected boolean isVerbose() {
		return plugin.getConfig().getBoolean(ConfigOptions.STRATEGY_VERBOSE_LOGGING, false);
	}
	
	protected void logVerbose(final Object...args) {
		if( isVerbose() ) {
			final StringBuilder sb = new StringBuilder(logPrefix);
			if( !logPrefix.endsWith(" ") )
				sb.append(" ");
			
			sb.append("(strategy ");
			sb.append(this.getStrategyConfigName());
			sb.append(") ");
			
			for(int i=0; i<args.length;i++) {
				sb.append(args[i]);
			}
			
			log.info(sb.toString());
		}
	}
	
	protected void logInfo(String msg) {
		log.info(logPrefix + " " + msg);
	}
	
	public void setPlugin(final HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.log = plugin.getLogger();
		this.logPrefix = plugin.getLogPrefix();
	}
	
	/** By default, strategy is assumed valid. Subclass can override to do
	 * it's own checks.
	 */
	public void validate() throws StrategyException {}
}
