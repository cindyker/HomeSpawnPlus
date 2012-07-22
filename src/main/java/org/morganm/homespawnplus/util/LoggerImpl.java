/**
 * 
 */
package org.morganm.homespawnplus.util;

import java.util.logging.Level;

import org.morganm.homespawnplus.HomeSpawnPlus;

/**
 * @author morganm
 *
 */
public class LoggerImpl implements Logger {
	private final HomeSpawnPlus plugin;
	private final java.util.logging.Logger log;
	private final Debug debug;
	private String logPrefix;
	
	public LoggerImpl(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.log = this.plugin.getLogger();

		setLogPrefix(this.plugin.getLogPrefix());
		this.debug = Debug.getInstance();
	}
	
	public void setLogPrefix(String logPrefix) {
		if( !logPrefix.endsWith(" ") )
			logPrefix = logPrefix + " ";
		this.logPrefix = logPrefix;
	}

	private String concatStrings(StringBuilder sb, Object...msgs) {
		for(Object o : msgs) {
			sb.append(o);
		}
		return sb.toString();
	}
	
	@Override
	public void info(Object... msg) {
		if( log.isLoggable(Level.INFO) ) {
			log.info(concatStrings(new StringBuilder(logPrefix), msg));
		}
	}

	@Override
	public void warn(Object... msg) {
		if( log.isLoggable(Level.WARNING) ) {
			log.warning(concatStrings(new StringBuilder(logPrefix), msg));
		}
	}
	@Override
	public void warn(Throwable t, Object... msg) {
		if( log.isLoggable(Level.WARNING) ) {
			log.log(Level.WARNING, concatStrings(new StringBuilder(logPrefix), msg), t);
		}
	}
	

	@Override
	public void severe(Object... msg) {
		if( log.isLoggable(Level.SEVERE) ) {
			log.severe(concatStrings(new StringBuilder(logPrefix), msg));
		}
	}
	@Override
	public void severe(Throwable t, Object... msg) {
		if( log.isLoggable(Level.SEVERE) ) {
			log.log(Level.SEVERE, concatStrings(new StringBuilder(logPrefix), msg), t);
		}
	}

	@Override
	public void debug(Object... msg) {
		debug.debug(msg);
	}

	@Override
	public void devDebug(Object... msg) {
		debug.devDebug(msg);
	}

}
