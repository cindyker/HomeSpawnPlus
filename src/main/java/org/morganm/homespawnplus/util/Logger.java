/**
 * 
 */
package org.morganm.homespawnplus.util;

/**
 * @author morganm
 *
 */
public interface Logger {
	public void info(String...msg);
	public void warn(String...msg);
	public void severe(String...msg);
	public void debug(Object...msg);
	public void devDebug(Object...msg);
}
