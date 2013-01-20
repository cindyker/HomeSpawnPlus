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
package com.andune.minecraft.hsp.server.bukkit;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;

import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.config.ConfigEconomy;
import com.andune.minecraft.hsp.manager.HomeLimitsManager;
import com.andune.minecraft.hsp.server.api.impl.EconomyAbstractImpl;

/**
 * @author andune
 *
 */
public class BukkitEconomy extends EconomyAbstractImpl implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(BukkitEconomy.class);

    private net.milkbowl.vault.economy.Economy vaultEconomy;
    
    public BukkitEconomy(ConfigEconomy config, HomeLimitsManager hlm) {
        super(config, hlm);
    }
    
    @Override
    public void init() throws Exception {
        if( vaultEconomy == null ) {
            Plugin vault = Bukkit.getServer().getPluginManager().getPlugin("Vault");
            if( vault != null ) {
                RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                if (economyProvider != null) {
                    vaultEconomy = economyProvider.getProvider();
                    log.info("Vault interface found and will be used for economy-related functions");
                }
            }
            else
                log.info("Vault not found, HSP economy features are disabled");
        }
    }

    @Override
    public int getInitPriority() {
        return 6;
    }

    @Override
    public boolean isEnabled() {
        return vaultEconomy != null;
    }

    @Override
    public String format(double amount) {
        return vaultEconomy.format(amount);
    }

    @Override
    public double getBalance(String playerName) {
        return vaultEconomy.getBalance(playerName);
    }

    @Override
    public String withdrawPlayer(String playerName, double amount) {
        EconomyResponse response = vaultEconomy.withdrawPlayer(playerName, amount);
        if( !response.transactionSuccess() )
            return response.errorMessage;
        else
            return null;
    }

    @Override
    public void shutdown() throws Exception {
    }
}
