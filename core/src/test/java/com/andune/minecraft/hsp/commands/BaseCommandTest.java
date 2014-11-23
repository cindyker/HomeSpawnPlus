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
package com.andune.minecraft.hsp.commands;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.Permissions;
import com.andune.minecraft.hsp.manager.CooldownManager;
import com.andune.minecraft.hsp.manager.WarmupManager;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.storage.Storage;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

/**
 * @author andune
 */
public class BaseCommandTest {
    protected final String MSG_NO_PERMISSION = "no permission";

    @Mock
    protected Server server;
    @Mock
    protected Plugin plugin;
    @Mock
    protected CooldownManager cooldownManager;
    @Mock
    protected WarmupManager warmupManager;
    @Mock
    protected Permissions permissions;
    @Mock
    protected Storage storage;
    @Mock
    protected CommandSender commandSender;
    @Mock
    protected Player player;

    public void beforeMethod() {
        when(server.getLocalizedMessage(HSPMessages.NO_PERMISSION)).thenReturn(MSG_NO_PERMISSION);
    }
}
