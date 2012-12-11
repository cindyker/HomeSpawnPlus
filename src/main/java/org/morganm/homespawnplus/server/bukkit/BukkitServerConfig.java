/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import javax.inject.Inject;

import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.server.api.ServerConfig;
import org.morganm.mBukkitLib.i18n.Colors;

/**
 * @author morganm
 *
 */
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
