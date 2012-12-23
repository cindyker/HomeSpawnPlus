/**
 * 
 */
package org.morganm.homespawnplus.server.api;

import java.io.File;

/**
 * @author morganm
 *
 */
public interface Plugin {
    public File getDataFolder();
    public File getJarFile();
    public String getName();
    public ClassLoader getClassLoader();
    public String getVersion();
    public int getBuildNumber();
}
