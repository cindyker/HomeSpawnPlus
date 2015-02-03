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
package com.andune.minecraft.hsp.server.bukkit;

import com.andune.minecraft.commonlib.i18n.Colors;
import com.andune.minecraft.commonlib.i18n.Locale;
import com.andune.minecraft.commonlib.server.api.BukkitFactoryInterface;
import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Teleport;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.server.api.Server;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;

/**
 * @author andune
 */
public class BukkitServer extends com.andune.minecraft.commonlib.server.bukkit.BukkitServer
        implements Server {
    private final Colors colors;

    @Inject
    public BukkitServer(Plugin plugin, Teleport teleport, Locale locale,
                        BukkitFactoryInterface bukkitFactory, Colors colors) {
        super(plugin, teleport, locale, bukkitFactory);
        this.colors = colors;
    }

    @Override
    public String getLocalizedMessage(HSPMessages key, Object... args) {
        return getLocalizedMessage(key.toString(), args);
    }

    @Override
    public void sendLocalizedMessage(CommandSender sender, HSPMessages key, Object... args) {
        sendLocalizedMessage(sender, key.toString(), args);
    }

    @Override
    public String getDefaultColor() {
        return colors.getDefaultColorString();
    }
}
