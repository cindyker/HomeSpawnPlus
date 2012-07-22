/**
 * 
 */
package org.morganm.homespawnplus.util;

/**
 * @author morganm
 *
 */
public interface Logger {
	public void info(Object...msg);
	public void warn(Object...msg);
	public void warn(Throwable t, Object... msg);
	public void severe(Object...msg);
	public void severe(Throwable t, Object... msg);
	
	public void debug(Object...msg);
	public void devDebug(Object...msg);
}
