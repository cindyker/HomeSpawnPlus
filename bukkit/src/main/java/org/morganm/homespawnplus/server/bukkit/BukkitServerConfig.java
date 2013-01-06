/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.server.api.ServerConfig;
nimport com.andune.minecraft.commonlib.i18n.Colors;
.Colors;

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
