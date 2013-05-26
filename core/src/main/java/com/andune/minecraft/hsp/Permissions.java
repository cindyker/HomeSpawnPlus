package com.andune.minecraft.hsp;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.server.api.Command;

/**
 * This interface exists so that a proxy object can easily be created for
 * instances where we want to do something special with permission checks,
 * such as make them all true when a command block is executing something
 * on behalf of the player.
 * 
 * @author andune
 *
 */
public interface Permissions {

    /**
     * Determine if a sender has a given permission.
     * 
     * @deprecated you should use one of the specific permission methods
     * or add one if possible, to minimize the spread of permission concerns
     * throughout the plugin code. This method is only set public still
     * to serve the need of config-exposed permissions, where admins can
     * directly choose the permissions and therefore the plugin must test
     * those permissions as-is.
     * 
     * @param sender
     * @param perm
     * @return
     */
    public boolean hasPermission(CommandSender sender, String perm);

    public boolean hasCommandPermission(CommandSender sender, String command);

    /**
     * Check for custom command permission first, if there is none, check the
     * default command permission.
     * 
     * @param sender
     * @param command
     * @return true if the sender has permission, false if not
     */
    public boolean hasCommandPermission(CommandSender sender, Command command);

    public boolean hasSetHomeNamed(Player player);

    /**
     * Determine if the player should have HSP admin privileges.
     * 
     * @param player
     * @return
     */
    public boolean isAdmin(CommandSender sender);

    /**
     * Determine if the player should be exempt from a given warmup.
     * 
     * @param player
     * @param warmup
     * @return
     */
    public boolean isWarmupExempt(Player player, String warmup);

    /**
     * Determine if the player should be exempt from a given cooldown.
     * 
     * @param player
     * @param cooldown
     * @return
     */
    public boolean isCooldownExempt(Player player, String cooldown);

    /**
     * Determine if the player should be exempt from a given cost.
     * 
     * @param player
     * @param cooldown
     * @return
     */
    public boolean isCostExempt(Player player, String cost);

    /**
     * Determine if a player has permission to specify an argument to 
     * groupspawn commands, such as "/groupspawn somegroup" 
     * 
     * @param player
     * @return
     */
    public boolean hasOtherGroupSpawnPermission(Player player);

    /**
     * Determine if the player has permission to go to named spawns, such
     * as "/spawn spawn3".
     * 
     * @param player
     * @param name optional arg, if set, is appended to the permission check
     * @return
     */
    public boolean hasSpawnNamed(Player player, String name);

    /**
     * Determine if the player has permission to send out permament
     * home invites.
     * 
     * @param player
     * @return
     */
    public boolean hasPermanentHomeInvite(Player player);

    public boolean hasHomeInviteOtherWorld(Player player);

    public boolean hasHomeOtherWorld(Player player);

    public boolean hasHomeNamed(Player player);

    /**
     * Determine if player has permission to set bed homes.
     * 
     * @param player
     * @return
     */
    public boolean hasBedSetHome(Player player);
}