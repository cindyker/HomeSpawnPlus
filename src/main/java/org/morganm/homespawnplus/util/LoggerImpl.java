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
	private final String logPrefix;
	private final Debug debug;
	
	public LoggerImpl(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.log = this.plugin.getJavaLogger();
		
		String prefix = this.plugin.getLogPrefix();
		if( !prefix.endsWith(" ") )
			prefix = prefix + " ";
		this.logPrefix = prefix;
		this.debug = Debug.getInstance();
	}

	private String concatStrings(StringBuilder sb, String...strings) {
		for(String s : strings) {
			sb.append(s);
		}
		return sb.toString();
	}
	
	@Override
	public void info(String... msg) {
		if( log.isLoggable(Level.INFO) ) {
			log.info(concatStrings(new StringBuilder(logPrefix), msg));
		}
	}

	@Override
	public void warn(String... msg) {
		if( log.isLoggable(Level.WARNING) ) {
			log.warning(concatStrings(new StringBuilder(logPrefix), msg));
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
