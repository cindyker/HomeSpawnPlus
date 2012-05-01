/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import java.util.logging.Logger;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.config.ConfigOptions;
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
	
	public void setPlugin(final HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.log = plugin.getLogger();
		this.logPrefix = plugin.getLogPrefix();
	}
}