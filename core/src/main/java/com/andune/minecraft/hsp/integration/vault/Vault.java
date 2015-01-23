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
