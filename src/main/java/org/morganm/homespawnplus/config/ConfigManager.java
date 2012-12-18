/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.mBukkitLib.JarUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * @author morganm
 * @deprecated
 */
@Singleton
public class ConfigManager {
    @SuppressWarnings("unused")
    private final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    
    private final Reflections reflections;
    private final Injector injector;
    private final JarUtils jarUtil;
    private final Plugin plugin;
    
    @Inject
    public ConfigManager(Injector injector, JarUtils jarUtil, Plugin plugin)
    {
        this.injector = injector;
        this.jarUtil = jarUtil;
        this.plugin = plugin;
        
        reflections = Reflections.collect();
    }
    
    public void installDefaults() throws FileNotFoundException, IOException {
        File pluginDir = plugin.getDataFolder();
        // create the config directory if it doesn't exist
        File configDir = new File(pluginDir, "config");
        if( !configDir.exists() )
            configDir.mkdirs();

        // now copy default files into the config directory if they aren't already there
        for(Class<? extends ConfigInterface> configClass : getConfigClasses()) {
            ConfigInterface config = injector.getInstance(configClass);
            
            String fileName = config.getConfigFileName();
            File configFile = new File(configDir, fileName);
            if( !configFile.exists() )
                jarUtil.copyConfigFromJar("config/"+fileName, configFile);
        }
    }

    /** Can be used to load or reload all configs.
     * @throws ConfigException 
     * @throws IOException 
     * @throws FileNotFoundException 
     * 
     */
    public void loadAll() throws FileNotFoundException, IOException, ConfigException {
        for(Class<? extends ConfigInterface> configClass : getConfigClasses()) {
            ConfigInterface config = injector.getInstance(configClass);
            config.load();
        }
    }
    
    private Set<Class<? extends ConfigInterface>> getConfigClasses() {
        Set<Class<? extends ConfigInterface>> configClasses = reflections.getSubTypesOf(ConfigInterface.class);
        for(Iterator<Class<? extends ConfigInterface>> i = configClasses.iterator(); i.hasNext();) {
            Class<? extends ConfigInterface> configClass = i.next();
            // skip any abstract classes
            if( Modifier.isAbstract(configClass.getModifiers()) )
                i.remove();
        }
        return configClasses;
    }
}
