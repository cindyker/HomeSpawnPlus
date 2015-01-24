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
package com.andune.minecraft.hsp.integration.vault;

import com.andune.minecraft.hsp.integration.PluginIntegration;

/**
 * @author andune
 */
public interface Vault extends PluginIntegration {
    /**
     * Format the amount in the appropriate way based on the economy plugin
     * settings. ie. an amount of "40" might be returned as "40 quid"
     *
     * @param amount
     * @return
     */
    public String format(double amount);

    /**
     * Return the account balance of a player
     *
     * @param playerName the player whose account balance we are returning
     * @return
     */
    public double getBalance(String playerName);

    /**
     * Withdraw an amount from a players account
     *
     * @param playerName the player to withdraw from
     * @param amount the amount. Use negative values to add to the account
     * @return
     */
    public String withdrawPlayer(String playerName, double amount);

    /**
     * Return true if Vault is enabled and has detected a valid economy
     * plugin to integrate with
     *
     * @return
     */
    public boolean isEconomyEnabled();
}
