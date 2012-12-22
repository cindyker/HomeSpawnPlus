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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Abstract base class that implements some common functionality
 * for config classes.
 * 
 * @author morganm
 *
 */
public abstract class ConfigBase implements ConfigInterface, Initializable {
    protected static final Logger log = LoggerFactory.getLogger(ConfigBase.class);
    
    protected final YamlFile yaml;
    protected final String fileName;
    protected final String basePath;
    
    Plugin plugin;
    JarUtils jarUtil;

    protected ConfigBase(String fileName, String basePath, YamlFile yaml) {
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
        return 3;   // default config initialization priority is 3
    }

    /**
     * Load (or reload) the configuration from the backing store.
     * 
     * @throws Exception
     */
    @Override
    public void load() throws IOException, FileNotFoundException, ConfigException {
        log.debug("config "+getClass()+" loading");
        installDefaultFile();
        yaml.load(new File(fileName));
    }

    /**
     * Install the configuration default file if it doesn't exist.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    void installDefaultFile() throws FileNotFoundException, IOException {
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
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }
    @Inject
    public void setJarUtil(JarUtils jarUtil) {
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
    protected Integer getInteger(String path) {
        return yaml.getInteger(basePath+"."+path);
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
