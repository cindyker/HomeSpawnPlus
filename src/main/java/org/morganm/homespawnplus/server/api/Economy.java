/**
 * 
 */
package org.morganm.homespawnplus.server.api;

/** API to economy. Modeled after Vault API.
 * 
 * @author morganm
 *
 */
public interface Economy {
    /**
     * Determine whether or not Economy is available and enabled.
     * 
     * @return true if economy is enabled
     */
    public boolean isEnabled();
    
    /**
     * Format amount into a human readable String This provides translation into
     * economy specific formatting to improve consistency between plugins.  
     *
     * @param amount
     * @return Human readable string describing amount
     */
    public String format(double amount);

    /**
     * Gets balance of a player
     * @param playerName
     * @return Amount currently held in players account
     */
    public double getBalance(String playerName);

    /**
     * Withdraw an amount from a player - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param playerName Name of player
     * @param amount Amount to withdraw
     * @return null on success or non-null error message on failure
     */
    public String withdrawPlayer(String playerName, double amount);
    
    /**
     * Return the configured cost of a command for a given player.
     * 
     * @param player the player we are checking (if null, default costs will be used)
     * @param command the command whose costs are being checked
     * @return
     */
    public int getCommandCost(Player player, String command);
}
