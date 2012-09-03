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
