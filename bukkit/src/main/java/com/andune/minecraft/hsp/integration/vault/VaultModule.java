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
package com.andune.minecraft.hsp.integration.vault;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.integration.worldborder.WorldBorder;
import com.andune.minecraft.hsp.integration.worldborder.WorldBorderIntegration;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;

/**
 * Wrapper for Vault integration - this prevents IoC breaking if economy
 * isn't installed since this class has no references to Vault classes.
 *
 * @author andune
 */
public class VaultModule implements Vault, Initializable {
    private static final Logger log = LoggerFactory.getLogger(VaultModule.class);
    private VaultIntegration vault;

    @Inject
    public VaultModule(Plugin plugin) {
        this.vault = new VaultIntegration(plugin);
    }

    @Override
    public void init() throws Exception {
        vault.init();
    }

    @Override
    public void shutdown() throws Exception {
        vault = null;
    }

    @Override
    public int getInitPriority() {
        return 9;
    }

    @Override
    public boolean isEnabled() {
        return vault.isEnabled();
    }

    @Override
    public boolean isEconomyEnabled() { return vault.isEconomyEnabled(); }

    @Override
    public String getVersion() {
        return vault.getVersion();
    }

    @Override
    public String format(double amount) {
        return vault.format(amount);
    }

    @Override
    public double getBalance(String playerName) {
        return vault.getBalance(playerName);
    }

    @Override
    public String withdrawPlayer(String playerName, double amount) {
        return vault.withdrawPlayer(playerName, amount);
    }
}
