/**
 * 
 */
package org.morganm.homespawnplus.server.api;

import java.io.File;
import java.io.InputStream;

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

    /**
     * Gets an embedded resource in this plugin
     *
     * @param filename Filename of the resource
     * @return File if found, otherwise null
     */
    public InputStream getResource(String filename);
}
