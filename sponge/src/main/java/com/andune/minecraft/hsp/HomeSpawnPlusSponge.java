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
package com.andune.minecraft.hsp;

import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.guice.SpongeInjectorFactory;
import com.andune.minecraft.hsp.server.sponge.config.SpongeConfigBootstrap;
import com.andune.minecraft.hsp.util.LogUtil;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.service.config.ConfigRoot;
import org.spongepowered.api.service.config.ConfigService;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: version should be set by maven
 *
 * @author andune
 */
@Plugin(id = "HomeSpawnPlus", name = "HomeSpawnPlus", version = "2.0.1-SNAPSHOT")
public class HomeSpawnPlusSponge {
    private HomeSpawnPlus mainClass;
    private PluginContainer pluginContainer;
    private ConfigService configService;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File configRootDir;

    @Inject
    private PluginManager pluginManager;

    @Inject
    private org.slf4j.Logger logger;

    @Subscribe
    public void initialize(ServerStartedEvent event) {
        LoggerFactory.setLoggerPrefix("[HomeSpawnPlus] ");

        // disable reflections spam; it's a bug that prints warnings that look alarming
        Logger.getLogger("org.reflections").setLevel(Level.OFF);

//        TODO: figure how to to handle enabling debug on Sponge
//        File debugFlagFile = new File(getDataFolder(), "devDebug");
//        if (debugFlagFile.exists())
            LogUtil.enableDebug();

        final Game game = event.getGame();
//        final PluginManager pm = game.getPluginManager();
//        Optional<PluginContainer> pcRef = pluginManager.getPlugin("HomeSpawnPlus");
//        Optional<ConfigService> csRef = game.getServiceManager().provide(ConfigService.class);

//        org.slf4j.Logger log = pluginManager.getLogger(pluginContainer);
        try {
            logger.debug("Initializing BukkitInjectorFactory");
            SpongeInjectorFactory factory = new SpongeInjectorFactory(game, pluginContainer,
                    new SpongeConfigBootstrap(getBootstrapConfig()), configRootDir);

            logger.debug("Instantiating HomeSpawnPlus mainClass");
            mainClass = new HomeSpawnPlus(factory);

            logger.debug("invoking mainClass.onEnable()");
            mainClass.onEnable();
        } catch (Exception e) {
            logger.error("Caught exception loading plugin, shutting down", e);
        }

    }

    /**
     * Find and load the bootstrap configuration, this is required prior to
     * handing off control to the core injection routines.
     *
     * @return
     * @throws Exception
     */
    private ConfigurationNode getBootstrapConfig() throws Exception {
        File bootStrapConfigFile = new File(configRootDir, "core.conf");

        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setFile(bootStrapConfigFile).build();
        ConfigurationNode rootNode;
        try {
            rootNode = loader.load();
        } catch(IOException e) {
            rootNode = null;
        }

        return rootNode;
    }
}
