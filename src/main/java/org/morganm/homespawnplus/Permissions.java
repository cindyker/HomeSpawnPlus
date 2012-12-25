/**
 * 
 */
package org.morganm.homespawnplus;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.command.Command;
import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.server.api.CommandSender;
import org.morganm.homespawnplus.server.api.PermissionSystem;
import org.morganm.homespawnplus.server.api.Player;


/** All HSP Permissions are defined here.
 * 
 * @author morganm
 *
 */
@Singleton
public class Permissions {
    /**
     * The base permission prefix - all HSP permissions start with this prefix. 
     */
    private static final String PERM_PREFIX = "hsp.";
    
    private final PermissionSystem permSystem;
    private final ConfigCore configCore;
    
    @Inject
    public Permissions(PermissionSystem permSystem, ConfigCore configCore) {
        this.permSystem = permSystem;
        this.configCore = configCore;
    }
    
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
    public boolean hasPermission(CommandSender sender, String perm) {
        boolean result = permSystem.has(sender, perm);

        // support legacy HSP "defaultPermissions" setting
        if( !result ) {
            List<String> defaultPerms = configCore.getDefaultPermissions();
            if( defaultPerms != null && defaultPerms.contains(perm) )
                result = true;
        }

        return result;
    }

    /**
     * Prepend the PREFIX and check if a player has a given permission node. 
     * 
     * @param sender the player to check
     * @param perm the permission to check (PREFIX is automatically prepended)
     * 
     * @return true if the player has the permission
     */
    private boolean permCheck(CommandSender sender, String perm) {
        return hasPermission(sender, PERM_PREFIX+ perm);
    }

    public boolean hasCommandPermission(CommandSender sender, String command) {
        return permCheck(sender,  "command." + command);
    }
    
    /**
     * Check for custom command permission first, if there is none, check the
     * default command permission.
     * 
     * @param sender
     * @param command
     * @return true if the sender has permission, false if not
     */
    public boolean hasCommandPermission(CommandSender sender, Command command) {
        String customPerm = command.getCommandPermissionNode();
        if( customPerm != null )
            return permCheck(sender, customPerm);
        else 
            return hasCommandPermission(sender,  command.getCommandName());
    }
    
    public boolean hasSetHomeNamed(Player player) {
        return permCheck(player, "command.sethome.named");
    }
    
    /**
     * Determine if the player should have HSP admin privileges.
     * 
     * @param player
     * @return
     */
    public boolean isAdmin(CommandSender sender) {
        return permCheck(sender, "admin");
    }
    
    /**
     * Determine if the player should be exempt from a given warmup.
     * 
     * @param player
     * @param warmup
     * @return
     */
    public boolean isWarmupExempt(Player player, String warmup) {
        return permCheck(player, "WarmupExempt."+warmup);
    }
    
    /**
     * Determine if the player should be exempt from a given cooldown.
     * 
     * @param player
     * @param cooldown
     * @return
     */
    public boolean isCooldownExempt(Player player, String cooldown) {
        return permCheck(player, "CooldownExempt."+cooldown);
    }

    /**
     * Determine if the player should be exempt from a given cost.
     * 
     * @param player
     * @param cooldown
     * @return
     */
    public boolean isCostExempt(Player player, String cost) {
        return permCheck(player, "CostExempt."+cost);
    }

    /**
     * Determine if a player has permission to specify an argument to 
     * groupspawn commands, such as "/groupspawn somegroup" 
     * 
     * @param player
     * @return
     */
    public boolean hasOtherGroupSpawnPermission(Player player) {
        return hasCommandPermission(player, "groupspawn.named");
    }
    
    /**
     * Determine if the player has permission to go to named spawns, such
     * as "/spawn spawn3".
     * 
     * @param player
     * @param name optional arg, if set, is appended to the permission check
     * @return
     */
    public boolean hasSpawnNamed(Player player, String name) {
        if( name != null )
            return hasCommandPermission(player, "spawn.named."+name);
        else
            return hasCommandPermission(player, "spawn.named");
    }
    
    /**
     * Determine if the player has permission to send out permament
     * home invites.
     * 
     * @param player
     * @return
     */
    public boolean hasPermanentHomeInvite(Player player) {
        return hasCommandPermission(player, "homeinvite.permanent");
    }
    
    public boolean hasHomeInviteOtherWorld(Player player) {
        return hasCommandPermission(player, "homeinvitetp.otherworld");
    }
    
    public boolean hasHomeOtherWorld(Player player) {
        return hasCommandPermission(player, "home.otherworld");
    }
    
    public boolean hasHomeNamed(Player player) {
        return hasCommandPermission(player, "home.named");
    }
    
    /**
     * Determine if player has permission to set bed homes.
     * 
     * @param player
     * @return
     */
    public boolean hasBedSetHome(Player player) {
        return permCheck(player, "home.bedsethome");
    }
}
