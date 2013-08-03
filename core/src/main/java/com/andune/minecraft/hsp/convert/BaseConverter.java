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
package com.andune.minecraft.hsp.convert;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Factory;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.commonlib.server.api.Server;
import com.andune.minecraft.hsp.storage.Storage;

import javax.inject.Inject;

/**
 * @author andune
 */
public abstract class BaseConverter implements Converter {
    protected Logger log = LoggerFactory.getLogger(BaseConverter.class);
    private CommandSender initiatingSender;

    private Plugin plugin;
    private Server server;
    private Storage storage;
    private Factory factory;

    @Inject
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    @Inject
    public void setServer(Server server) {
        this.server = server;
    }

    @Inject
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    @Inject
    public void setFactory(Factory factory) {
        this.factory = factory;
    }

    @Override
    public void run() {
        try {
            int homesConverted = convert();
            if (initiatingSender != null)
                initiatingSender.sendMessage("Finished converting " + homesConverted + " homes");
        } catch (Exception e) {
            log.warn("error trying to convert homes", e);
            initiatingSender.sendMessage("Error converting homes, check your server.log");
        }
    }

    @Override
    public void setInitiatingSender(CommandSender initiatingSender) {
        this.initiatingSender = initiatingSender;
    }
}
