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
package com.andune.minecraft.hsp.server.core;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Economy;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.config.ConfigEconomy;
import com.andune.minecraft.hsp.config.ConfigEconomy.PerPermissionEconomyEntry;
import com.andune.minecraft.hsp.integration.vault.Vault;
import com.andune.minecraft.hsp.manager.HomeLimitsManager;

import java.util.List;
import java.util.Map;

/**
 * @author andune
 */
public class EconomyImpl implements Economy {
    private static final Logger log = LoggerFactory.getLogger(EconomyImpl.class);

    private final ConfigEconomy config;
    private final HomeLimitsManager homeLimitsManager;
    private final Vault vault;

    public EconomyImpl(ConfigEconomy config, HomeLimitsManager hlm, Vault vault) {
        this.config = config;
        this.homeLimitsManager = hlm;
        this.vault = vault;
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

    @Override
    public boolean isEnabled() {
        return vault.isEconomyEnabled();
    }

    public int getCommandCost(Player player, String command) {
        Integer cost = null;

        Map<String, PerPermissionEconomyEntry> map = config.getPerPermissionEntries();

        PER_PERMISSION:
        for (Map.Entry<String, PerPermissionEconomyEntry> e : map.entrySet()) {
            PerPermissionEconomyEntry entry = e.getValue();

            Map<String, Integer> costs = entry.getCosts();
            Integer entryCost = costs.get(command);
            // if this command is not listed in this entry, skip it
            if (entryCost == null)
                continue;

            // command is listed, see if player has a matching permission
            List<String> permissions = entry.getPermissions();
            for (String perm : permissions) {
                if (player.hasPermission(perm)) {
                    cost = entryCost;
                    break PER_PERMISSION;
                }
            }
        }

        // if there was no permission-specific cost, so look for
        // a world-specific cost instead
        if (cost == null)
            cost = config.getWorldSpecificCost(player.getWorld().getName(), command);

        // if we get here and no cost yet, then get the global cost (possibly 0)
        if (cost == null)
            cost = config.getGlobalCost(command);

        // apply sethome-multiplier, if any
        if (cost > 0 && command.equalsIgnoreCase("sethome")) {
            double multiplier = config.getSethomeMultiplier();
            if (multiplier > 0) {
                // by the time this method is called, the new home has already been created,
                // so it is already part of our globalHomeCount
                int globalHomeCount = homeLimitsManager.getHomeCount(player.getName(), null);
                if (globalHomeCount > 1) {
                    double totalCost = cost;
                    for (int i = 1; i < globalHomeCount; i++)
                        totalCost *= multiplier;
                    double additionalCost = totalCost - cost;
                    log.debug("applying sethome-multplier {} for player {}"
                            + ", total global home count={}, original cost={}, additionalCost={}",
                            multiplier, player, globalHomeCount, cost, additionalCost);
                    // should always be true, but check just in case
                    if (additionalCost > 0)
                        cost += (int) additionalCost;
                }
            }
        }

        return cost;
    }
}
