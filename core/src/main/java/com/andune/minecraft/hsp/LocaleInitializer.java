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
package com.andune.minecraft.hsp;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.i18n.Colors;
import com.andune.minecraft.commonlib.i18n.Locale;
import com.andune.minecraft.commonlib.i18n.LocaleConfig;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.hsp.config.ConfigCore;

/** Class responsible for initializing our Locale object. Priority
 * guarantees it runs after the config files have been loaded, which
 * is important so that we know what locale to use.
 *  
 * @author andune
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
