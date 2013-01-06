/**
 * 
 */
package com.andune.minecraft.hsp;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.andune.minecraft.commonlib.i18n.Colors;
import com.andune.minecraft.commonlib.i18n.Locale;
import com.andune.minecraft.commonlib.i18n.LocaleConfig;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.server.api.Plugin;

/** Class responsible for initializing our Locale object. Priority
 * guarantees it runs after the config files have been loaded, which
 * is important so that we know what locale to use.
 *  
 * @author morganm
 *
 */
@Singleton
public class LocaleInitializer implements Initializable {
    private final ConfigCore configCore;
    private final Plugin plugin;
    private final Locale locale;
    
    @Inject
    public LocaleInitializer(ConfigCore configCore, Plugin plugin, Locale locale) {
        this.configCore = configCore;
        this.plugin = plugin;
        this.locale = locale;
    }

    @Override
    public void init() throws Exception {
        Colors colors = new Colors();
        colors.setDefaultColor(configCore.getDefaultColor());
        LocaleConfig localeConfig = new LocaleConfig(configCore.getLocale(),
                plugin.getDataFolder(), "hsp", plugin.getJarFile(), colors);        
        locale.load(localeConfig);
    }

    @Override
    public int getInitPriority() {
        return 5;
    }

    @Override
    public void shutdown() throws Exception {
    }
}
