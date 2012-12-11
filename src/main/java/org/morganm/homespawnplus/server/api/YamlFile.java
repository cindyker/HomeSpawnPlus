/**
 * 
 */
package org.morganm.homespawnplus.server.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.morganm.homespawnplus.config.ConfigException;

/** Interface to YAML configuration. HSP config files are written in
 * YAML, however through this interface they are not bound to any
 * specific implementation. So an implementation could be written to
 * bind to Bukkit's YAML, or SnakeYAML, or some other YAML API.
 * 
 * @author morganm
 *
 */
public interface YamlFile {
    public void save(File file) throws IOException;
    public void load(File file) throws FileNotFoundException, IOException, ConfigException;
    
    public boolean contains(String path);
    public Object get(String path);
    public boolean getBoolean(String path);
    public int getInt(String path);
    public double getDouble(String path);
    public String getString(String path);
    public Set<String> getKeys(String path);
    public List<String> getStringList(String path);
}
