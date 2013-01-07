/**
 * 
 */
package com.andune.minecraft.hsp.server.craftbukkit.pre;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

import com.andune.minecraft.hsp.server.craftbukkit.CraftServer;

/**
 * @author andune
 *
 */
public class CraftServerImpl implements CraftServer {

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.craftbukkit.CraftServer#registerCommand(org.bukkit.command.Command)
     */
    @Override
    public void registerCommand(Command command) {
        org.bukkit.craftbukkit.CraftServer craftServer = (org.bukkit.craftbukkit.CraftServer) Bukkit.getServer();
        SimpleCommandMap commandMap = craftServer.getCommandMap();
        commandMap.register("hsp", command);
    }

}
