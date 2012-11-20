/**
 * 
 */
package org.morganm.homespawnplus;

import javax.inject.Inject;

import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.guice.InjectorFactory;
import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.homespawnplus.server.api.command.CommandRegister;
import org.morganm.mBukkitLib.PermissionSystem;
import org.morganm.mBukkitLib.i18n.LocaleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/** Main object that controls plugin startup and shutdown.
 * 
 * @author morganm
 *
 */
public class HSPNew {
    private final Logger log = LoggerFactory.getLogger(HSPNew.class);

    private PermissionSystem permSystem;
    private Plugin plugin;
    private ConfigCore config;
    private CommandRegister commands;
    
    private String version = "undef";
    private String buildNumber = "-1";

    public void onEnable() {
        // load localized strings for the configured locale
        LocaleConfig localeConfig = new LocaleConfig(config.getLocale(),
                plugin.getDataFolder(), "homespawnplus", plugin.getJarFile(), null);

        final Injector injector = InjectorFactory.createInjector(this);     // IoC container
        injector.injectMembers(this);   // inject all dependencies for this object

        permSystem.setupPermissions();
        
        commands.registerAllCommands();
        
        log.info("version "+version+", build "+buildNumber+" is enabled");
    }
    
    public void onDisable() {
        log.info("version "+version+", build "+buildNumber+" is disabled");
    }

    @Inject
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    @Inject
    public void setPermissionSystem(PermissionSystem permSystem) {
        this.permSystem = permSystem;
    }
    
    @Inject
    public void setCommandRegister(CommandRegister commandRegister) {
        this.commands = commandRegister;
    }
}
