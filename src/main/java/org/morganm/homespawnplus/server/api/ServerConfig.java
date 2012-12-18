/**
 * 
 */
package org.morganm.homespawnplus.server.api;


/** Interface for configuration items that might return different
 * values depending on the implementation.
 * 
 * @author morganm
 *
 */
public interface ServerConfig {
    /**
     * Return a string that represents the default color in a format
     * that will be displayed properly when added to a string sent to
     * a user. For example, in Bukkit these strings are something like
     * "\u00A7f" (representing White in the Bukkit example).
     * 
     * @return
     */
    public String getDefaultColor();
}
