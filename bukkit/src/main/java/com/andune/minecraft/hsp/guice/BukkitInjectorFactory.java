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
package com.andune.minecraft.hsp.guice;


import com.andune.minecraft.hsp.config.ConfigStorage;
import com.andune.minecraft.hsp.guice.HSPModule;
import com.andune.minecraft.hsp.guice.InjectorFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

/** Factory class to create an injector specific to the Bukkit module.
 *  
 * @author andune
 *
 */
public class BukkitInjectorFactory implements InjectorFactory {
    private final Object originalPluginObject;
    private final ConfigStorage configStorage;
    
    public BukkitInjectorFactory(Object originalPluginObject, ConfigStorage configStorage) {
        this.originalPluginObject = originalPluginObject;
        this.configStorage = configStorage;
    }

    /**
     * Factory to create Guice Injector.
     * 
     * @return
     */
    public Injector createInjector() {
        // in the future this will choose different injectors based on the
        // environment. For now the only environment we support is Bukkit.
        Injector injector = Guice.createInjector(new HSPModule(configStorage), new BukkitModule(originalPluginObject));
        return injector;
    }
}
