/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.morganm.homespawnplus.Initializable;
import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.homespawnplus.server.api.YamlFile;
import org.morganm.mBukkitLib.JarUtils;

/**
 * @author morganm
 *
 */
public abstract class AbstractConfigBase implements ConfigInterface, Initializable {
    protected final YamlFile yaml;
    protected final String fileName;
    protected final String basePath;
    
    private Plugin plugin;
    private JarUtils jarUtil;

    protected AbstractConfigBase(String fileName, String basePath, YamlFile yaml) {
        this.fileName = fileName;
        this.basePath = basePath;
        this.yaml = yaml;
    }

    @Override
    public void init() throws Exception {
        load();
    }
    
    @Override
    public int getPriority() {
        return 1;   // default config priority is 1
    }

    /**
     * Load (or reload) the configuration from the backing store.
     * 
     * @throws Exception
     */
    @Override
    public void load() throws IOException, FileNotFoundException, ConfigException {
        installDefaultFile();
        yaml.load(new File(fileName));
    }

    /**
     * Install the configuration default file if it doesn't exist.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void installDefaultFile() throws FileNotFoundException, IOException {
        File pluginDir = plugin.getDataFolder();
        // create the config directory if it doesn't exist
        File configDir = new File(pluginDir, "config");
        if( !configDir.exists() )
            configDir.mkdirs();

        String fileName = getConfigFileName();
        File configFile = new File(configDir, fileName);
        if( !configFile.exists() )
            jarUtil.copyConfigFromJar("config/"+fileName, configFile);
    }

    @Override
    public String getConfigFileName() {
        return fileName;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }
    
    @Inject
    void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }
    @Inject
    void setJarUtil(JarUtils jarUtil) {
        this.jarUtil = jarUtil;
    }

    protected boolean contains(String path) {
        return yaml.contains(basePath+"."+path);
    }
    protected Object get(String path) {
        return yaml.get(basePath+"."+path);
    }
    protected boolean getBoolean(String path) {
        return yaml.getBoolean(basePath+"."+path);
    }
    protected int getInt(String path) {
        return yaml.getInt(basePath+"."+path);
    }
    protected double getDouble(String path) {
        return yaml.getDouble(basePath+"."+path);
    }
    protected String getString(String path) {
        return yaml.getString(basePath+"."+path);
    }
    protected Set<String> getKeys(String path) {
        return yaml.getKeys(basePath+"."+path);
    }
    protected List<String> getStringList(String path) {
        return yaml.getStringList(basePath+"."+path);
    }
}
