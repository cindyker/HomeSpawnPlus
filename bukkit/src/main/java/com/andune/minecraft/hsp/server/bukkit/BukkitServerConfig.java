/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit;

import javax.inject.Inject;
import javax.inject.Singleton;


import com.andune.minecraft.commonlib.i18n.Colors;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.server.api.ServerConfig;

/**
 * @author morganm
 *
 */
@Singleton
public class BukkitServerConfig implements ServerConfig {
    private final ConfigCore configCore;
    private final Colors colors;
    
    @Inject
    public BukkitServerConfig(ConfigCore configCore, Colors colors) {
        this.configCore = configCore;
        this.colors = colors;
    }
    
    @Override
    public String getDefaultColor() {
        String configuredColor = configCore.getDefaultColor();
        return colors.getColorString(configuredColor);
    }

}
