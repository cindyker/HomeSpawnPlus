/**
 * 
 */
package com.andune.minecraft.hsp.server.craftbukkit;

import org.bukkit.command.Command;

/** This interface represents any needs we have on the CraftServer
 * implementation. This can be implemented by modules specific to each
 * CraftServer version to abstract the CraftServer package versioning
 * away from the plugin.
 * 
 * @author andune
 *
 */
public interface CraftServer {
    /**
     * Dynamically register a command with the craftserver Command
     * implementation. Someday this simple capability will be a Bukkit
     * API feature, but for now, CraftServer is the only way to do it.
     * 
     * @param command
     */
    public void registerCommand(Command command);
}
