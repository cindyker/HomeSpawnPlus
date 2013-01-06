/**
 * 
 */
package com.andune.minecraft.hsp.config;

import javax.inject.Singleton;


import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.server.api.ConfigurationSection;

/**
 * @author morganm
 *
 */
@Singleton
@ConfigOptions(fileName="dynmap.yml", basePath="dynmap")
public class ConfigDynmap extends ConfigBase implements Initializable {
    public boolean isEnabled() {
        return super.getBoolean("enabled");
    }
    
    public double getUpdatePeriod() {
        return super.getDouble("update.period");
    }
    
    public boolean isHomesEnabled() {
        return super.getBoolean("layer.homes.enable");
    }
    
    public boolean isSpawnsEnabled() {
        return super.getBoolean("layer.spawns.enable");
    }
    
    public ConfigurationSection getHomesConfig() {
        return configSection.getConfigurationSection("layer.homes");
    }

    public ConfigurationSection getSpawnsConfig() {
        return configSection.getConfigurationSection("layer.spawns");
    }
}
