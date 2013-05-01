/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
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
package com.andune.minecraft.hsp.integration.multiverse;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;

import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.config.ConfigCore;

/**
 * @author andune
 *
 */
@Singleton
public class MultiverseCoreModule implements MultiverseCore, Initializable {
    private static final Logger log = LoggerFactory.getLogger(MultiverseCoreModule.class);

    private final Plugin plugin;
    private final ConfigCore configCore;
    private MultiverseListener multiverseListener;
    private MultiverseSafeTeleporter teleporter;
    private String currentTeleporter;

    @Inject
    public MultiverseCoreModule(ConfigCore configCore, Plugin plugin) {
        this.configCore = configCore;
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        if( configCore.isMultiverseEnabled() ) {    // enabled in config?
            Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
            if( p != null )                         // plugin exists?
                return p.isEnabled();               // plugin is enabled?
        }

        // if we get here, this module is not enabled
        return false;
    }

    @Override
    public String getVersion() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        if( p != null )
            return p.getDescription().getVersion();
        else
            return null;
    }
    
    /**
     * Package use only.
     * 
     * @return
     */
    protected MultiverseListener getMultiverseListener() {
        return multiverseListener;
    }

    @Override
    public void init() throws Exception {
        if( !isEnabled() )
            return;
        
        Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        if( p != null ) {
            if( p.getDescription().getVersion().startsWith("2.4") ) {
                com.onarandombox.MultiverseCore.MultiverseCore multiverse = (com.onarandombox.MultiverseCore.MultiverseCore) p;
            
                if( multiverse != null ) {
                    log.debug("Hooking Multiverse");
                    teleporter = new MultiverseSafeTeleporter(multiverse, this);
                    teleporter.install();
                    this.multiverseListener = new MultiverseListener();

                    registerListener();
                }
            }
        }
    }

    @Override
    public void shutdown() throws Exception {
        if( teleporter != null ) {
            teleporter.uninstall();
            teleporter = null;
        }
    }

    @Override
    public int getInitPriority() {
        return 9;
    }

    @Override
    public String getCurrentTeleporter() {
        return currentTeleporter;
    }

    @Override
    public void setCurrentTeleporter(String name) {
        currentTeleporter = name;
    }

    private void registerListener() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        if( p != null ) {
            plugin.getServer().getPluginManager().registerEvent(com.onarandombox.MultiverseCore.event.MVTeleportEvent.class,
                    multiverseListener,
                    EventPriority.NORMAL,
                    new EventExecutor() {
                        public void execute(Listener listener, Event event) throws EventException {
                            try {
                                multiverseListener.onMultiverseTeleport((com.onarandombox.MultiverseCore.event.MVTeleportEvent) event);
                            } catch (Throwable t) {
                                throw new EventException(t);
                            }
                        }
                    },
                    plugin);
        }
    }
}
