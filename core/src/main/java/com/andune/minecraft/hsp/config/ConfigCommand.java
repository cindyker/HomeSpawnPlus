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
package com.andune.minecraft.hsp.config;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.ConfigurationSection;

import javax.inject.Singleton;
import java.util.*;


/**
 * This class is used to store state of commands configuration. This amounts to three
 * key pieces of data: any disabled commands, all custom defined commands and all
 * properties related to any custom defined commands.
 *
 * @author andune
 */
@Singleton
@ConfigOptions(fileName = "commands.yml", basePath = "commands")
public class ConfigCommand extends ConfigBase implements Initializable {
    private final Logger log = LoggerFactory.getLogger(ConfigCommand.class);

    private Set<String> disabledCommands;
    private Map<String, Map<String, Object>> commandParams;

    @Override
    public void init() throws Exception {
        super.init();
        disabledCommands = new HashSet<String>();
        commandParams = new HashMap<String, Map<String, Object>>();
        loadConfig();
    }

    /**
     * Check if a command is disabled.
     *
     * @param command the command name to check
     * @return true if the command is disabled
     */
    public boolean isDisabledCommand(String command) {
        if (disabledCommands.contains("*"))
            return true;
        else
            return disabledCommands.contains(command.toLowerCase());
    }

    /**
     * Return a list of all commands that have been defined and have command
     * parameters.
     *
     * @return
     */
    public Set<String> getDefinedCommands() {
        return commandParams.keySet();
    }

    /**
     * Return command parameters for a specific command.
     *
     * @return guaranteed to not return null
     */
    public Map<String, Object> getCommandParameters(String command) {
        Map<String, Object> ret = commandParams.get(command);

        // If null, create empty map and save it for future use
        if (ret == null) {
            ret = new HashMap<String, Object>();
            commandParams.put(command, ret);
        }

        return ret;
    }

    /**
     * Determine if uber commands are enabled.
     *
     * @return
     */
    public boolean isUberCommandsEnabled() {
        return super.getBoolean("useUberCommands");
    }

    private void loadConfig() {
        disabledCommands = new HashSet<String>();
        List<String> theList = super.getStringList("disabledCommands");
        if (theList != null) {
            for (String s : theList) {
                disabledCommands.add(s.toLowerCase());
            }
        } else

            commandParams = new HashMap<String, Map<String, Object>>();
        Set<String> keys = super.getKeys();
        if (keys != null) {
            for (String key : keys) {
                if (key.equals("disabledCommands") || key.equals("useUberCommands"))
                    continue;

                log.debug("loading config params for command {}", key);
                ConfigurationSection cmdSection = super.getConfigurationSection(key);
                if (cmdSection == null) {
                    log.warn("no parameters defined for command {}, skipping", key);
                    continue;
                }

                Set<String> parameters = cmdSection.getKeys();
                if (parameters == null || parameters.size() == 0) {
                    log.warn("no parameters defined for command {}, skipping", key);
                    continue;
                }

                HashMap<String, Object> paramMap = new HashMap<String, Object>();
                commandParams.put(key, paramMap);
                for (String param : parameters) {
                    final Object val = cmdSection.get(param);
                    log.debug("command {}; key={}, val={}", key, param, val);
                    paramMap.put(param, val);
                }
            }
        }
    }
}
