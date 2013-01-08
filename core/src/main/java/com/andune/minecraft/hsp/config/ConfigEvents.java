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
package com.andune.minecraft.hsp.config;

import javax.inject.Singleton;


import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.server.api.ConfigurationSection;

/** Config file for event-related configuration.
 * 
 * The pattern used by this config object is different from the others because the
 * StrategyConfig object already has all the necessary logic to process the event
 * configs and it is sufficiently different from the other config patterns to
 * remain that way.
 * 
 * At some point it might make sense to merge the majority of code from StrategyConfig
 * class into this config object, but there wouldn't be a huge benefit except for
 * alignment with purpose in this package. For now it's better to leave the complex and
 * well-tested StrategyConfig class alone.
 * 
 * @author andune
 *
 */
@Singleton
@ConfigOptions(fileName="events.yml", basePath="events")
public class ConfigEvents extends ConfigBase implements Initializable {
    public static final String SETTING_EVENTS_WORLDBASE = "world";
    public static final String SETTING_EVENTS_PERMBASE = "permission";

    /**
     * Return the base configuration section.
     * 
     * @return
     */
    public ConfigurationSection getBaseSection() {
        return configSection;
    }

    /**
     * Return a ConfigurationSection relative to the base section.
     * 
     * @param sectionName
     * @return
     */
    public ConfigurationSection getSection(String sectionName) {
        return configSection.getConfigurationSection(sectionName);
    }

    public ConfigurationSection getWorldSection() {
        return configSection.getConfigurationSection(SETTING_EVENTS_WORLDBASE);
    }

    public ConfigurationSection getPermissionSection() {
        return configSection.getConfigurationSection(SETTING_EVENTS_PERMBASE);
    }
}
