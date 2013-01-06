/**
 * 
 */
package com.andune.minecraft.hsp.config;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.config.ConfigException;
import com.andune.minecraft.hsp.server.api.ConfigurationSection;

/** Abstract base class that implements some common functionality
 * for config classes.
 * 
 * @author morganm
 *
 */
public abstract class ConfigBase implements Initializable {
    protected static final Logger log = LoggerFactory.getLogger(ConfigBase.class);
    
    @Inject private ConfigLoader configLoader; 
    protected ConfigurationSection configSection;

    @Override
    public void init() throws Exception {
        ConfigOptions configOptions = getClass().getAnnotation(ConfigOptions.class);
        if( configOptions == null )
            throw new ConfigException("Annotation @ConfigOptions missing from class "+getClass());

        configSection = configLoader.load(configOptions.fileName(), configOptions.basePath());
    }
    
    protected String getBasePath() {
        return getClass().getAnnotation(ConfigOptions.class).basePath();
    }
    
    @Override
    public void shutdown() throws Exception {};
    
    @Override
    public int getInitPriority() {
        return 3;   // default config initialization priority is 3
    }

    protected boolean contains(String path) {
        return configSection.contains(path);
    }
    protected Object get(String path) {
        return configSection.get(path);
    }
    protected boolean getBoolean(String path) {
        return configSection.getBoolean(path);
    }
    protected int getInt(String path) {
        return configSection.getInt(path);
    }
    protected Integer getInteger(String path) {
        return configSection.getInteger(path);
    }
    protected double getDouble(String path) {
        return configSection.getDouble(path);
    }
    protected String getString(String path) {
        return configSection.getString(path);
    }
    protected Set<String> getKeys(String path) {
        return configSection.getKeys(path);
    }
    protected List<String> getStringList(String path) {
        return configSection.getStringList(path);
    }
}
