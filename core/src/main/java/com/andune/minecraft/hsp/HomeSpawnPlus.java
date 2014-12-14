/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
/**
 *
 */
package com.andune.minecraft.hsp;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.commonlib.server.api.event.EventDispatcher;
import com.andune.minecraft.hsp.guice.InjectorFactory;
import com.andune.minecraft.hsp.util.SpawnUtil;
import com.google.inject.Injector;

import javax.inject.Inject;

/**
 * Main object that controls plugin startup and shutdown.
 *
 * @author andune
 */
public class HomeSpawnPlus {
    private final Logger log = LoggerFactory.getLogger(HomeSpawnPlus.class);

    // members that are passed in to us when instantiated
    private final InjectorFactory injectorFactory;

    // members that are injected by the IoC container
    @Inject
    private Initializer initializer;
    @Inject
    private EventDispatcher eventDispatcher;
    @Inject
    private Plugin plugin;
    @Inject
    private SpawnUtil spawnUtil;
    private boolean initialized = false;

    public HomeSpawnPlus(InjectorFactory injectorFactory) {
        this.injectorFactory = injectorFactory;
    }

    public void onEnable() throws Exception {
//        GuiceDebug.enable();
        final Injector injector = injectorFactory.createInjector(); // IoC container
        injector.injectMembers(this);   // inject all dependencies for this object

        initializer.initAll();
        eventDispatcher.registerEvents();

        log.info("{} version {} is enabled", plugin.getName(), plugin.getVersion(), plugin.getBuild());
        initialized = true;
    }

    public void onDisable() {
        if (initialized) {
            // unhook multiverse (if needed)
            //        multiverse.onDisable();

            if (spawnUtil != null)
                spawnUtil.updateAllPlayerLocations();
            if (initializer != null)
                initializer.shutdownAll();
        }

        if (plugin != null)
            log.info("{} version {} is disabled", plugin.getName(), plugin.getVersion(), plugin.getBuild());

        initialized = false;
    }

    /** Routine to detect other plugins that use the same commands as HSP and
     * often cause conflicts and create confusion.
     *
     */
    /* only method left from old plugin class, probably should put this into the integration
     * classes when I build them.
     * 
    private void detectAndWarn() {
        // do nothing if warning is disabled
        if( !getConfig().getBoolean(ConfigOptions.WARN_CONFLICTS, true) )
            return;
        
        if( getServer().getPluginManager().getPlugin("Essentials") != null ) {
            log.warning(logPrefix+" Essentials found. It is likely your HSP /home and /spawn commands will"
                    + " end up going to Essentials instead.");
            log.warning(logPrefix+" Also note that HSP can convert your homes from Essentials for you. Just"
                    + " run the command \"/hspconvert essentials\" (must have hsp.command.admin permission)");
            log.warning(logPrefix+" Set \"core.warnConflicts\" to false in your HSP config.yml to disable"
                    + " this warning.");
        }
        
        if( getServer().getPluginManager().getPlugin("CommandBook") != null ) {
            log.warning(logPrefix+" CommandBook found. It is likely your HSP /home and /spawn commands will"
                    + " end up going to CommandBook instead. Please add \"homes\" and"
                    + " \"spawn-locations\" to your CommandBook config.yml \"components.disabled\" section.");
            log.warning(logPrefix+" Set \"core.warnConflicts\" to false in your HSP config.yml to disable"
                    + " this warning.");
        }
    }
    */

}
