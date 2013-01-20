/**
 * 
 */
package com.andune.minecraft.hsp.server.craftbukkit.v1_4_R1;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_4_R1.CraftServer;

/**
 * @author andune
 *
 */
public class CraftServerImpl implements com.andune.minecraft.hsp.server.craftbukkit.CraftServer {
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.craftbukkit.CraftServer#registerCommand(org.bukkit.command.Command)
     */
    @Override
    public void registerCommand(Command command) {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        SimpleCommandMap commandMap = craftServer.getCommandMap();
        commandMap.register("hsp", command);
    }
}
