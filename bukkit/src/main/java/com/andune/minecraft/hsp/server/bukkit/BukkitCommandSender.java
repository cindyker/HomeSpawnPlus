/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit;

import com.andune.minecraft.hsp.server.api.Server;

/**
 * @author andune
 *
 */
public class BukkitCommandSender extends com.andune.minecraft.commonlib.server.bukkit.BukkitCommandSender {
    private final Server server;
    
    public BukkitCommandSender(org.bukkit.command.CommandSender bukkitSender, Server server) {
        super(bukkitSender);
        this.server = server;
    }

    @Override
    public void sendMessage(String message) {
        super.sendMessage(server.getDefaultColor() + message);
    }

    @Override
    public void sendMessage(String[] messages) {
        super.sendMessage(server.getDefaultColor() + messages);
    }
}
