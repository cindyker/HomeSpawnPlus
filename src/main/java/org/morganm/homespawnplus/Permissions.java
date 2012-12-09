/**
 * 
 */
package org.morganm.homespawnplus;

import javax.inject.Inject;

import org.morganm.homespawnplus.server.api.Player;
import org.morganm.mBukkitLib.PermissionSystem;


/** All HSP Permissions are defined here.
 * 
 * @author morganm
 *
 */
public class Permissions {
    /**
     * The base permission prefix - all HSP permissions start with this prefix. 
     */
    private static final String PERM_PREFIX = "hsp.";
    
    private final PermissionSystem permSystem;
    
    @Inject
    public Permissions(PermissionSystem permSystem) {
        this.permSystem = permSystem;
    }
    
    /**
     * Prepend the PREFIX and check if a player has a given permission node. 
     * 
     * @param player the player to check
     * @param perm the permission to check (PREFIX is automatically prepended)
     * 
     * @return true if the player has the permission
     */
    private boolean permCheck(Player player, String perm) {
        return permSystem.has(player.getName(), PERM_PREFIX+ perm);
    }

    public boolean hasCommandPermission(Player player, String command) {
        return permCheck(player,  "command." + command);
    }
    
    public boolean hasSetHomeNamed(Player player) {
        return permCheck(player, "command.sethome.named");
    }
    
    public boolean isWarmupExempt(Player player, String warmup) {
        return permCheck(player, "WarmupExempt."+warmup);
    }
    
    /**
     * Determine if a player has permission to specify an argument to 
     * groupspawn commands, such as "/groupspawn somegroup" 
     * 
     * @param player
     * @return
     */
    public boolean hasOtherGroupSpawnPermission(Player player) {
        return hasCommandPermission(player, ".groupspawn.named");
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
            return hasCommandPermission(player, ".spawn.named."+name);
        else
            return hasCommandPermission(player, ".spawn.named");
    }
    
    /**
     * Determine if the player has permission to send out permament
     * home invites.
     * 
     * @param player
     * @return
     */
    public boolean hasPermanentHomeInvite(Player player) {
        return hasCommandPermission(player, ".homeinvite.permanent");
    }
}
