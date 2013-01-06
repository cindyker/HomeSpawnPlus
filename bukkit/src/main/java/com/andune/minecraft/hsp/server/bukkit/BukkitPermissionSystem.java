/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.plugin.Plugin;

import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.server.api.CommandSender;
import com.andune.minecraft.hsp.server.api.PermissionSystem;

/**
 * @author morganm
 *
 */
@Singleton
public class BukkitPermissionSystem implements PermissionSystem, Initializable {
    private final Plugin plugin;

    private com.andune.minecraft.commonlib.PermissionSystem permSystem;

    @Inject
    public BukkitPermissionSystem(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getSystemInUse() {
        return permSystem.getSystemInUseString();
    }

    @Override
    public boolean has(String worldName, String playerName, String permission) {
        return permSystem.has(worldName, playerName, permission);
    }

    @Override
    public boolean has(CommandSender sender, String permission) {
        return permSystem.has(((BukkitCommandSender) sender).getBukkitSender(), permission);
    }

    @Override
    public void init() throws Exception {
        permSystem = new com.andune.minecraft.commonlib.PermissionSystem(plugin, plugin.getLogger());
        permSystem.setupPermissions();
    }

    @Override
    public void shutdown() throws Exception {
        permSystem = null;
    }

    @Override
    public int getInitPriority() {
        return 6;
    }

    @Override
    public String getPlayerGroup(String playerWorld, String playerName) {
        return permSystem.getPlayerGroup(playerWorld, playerName);
    }
}
