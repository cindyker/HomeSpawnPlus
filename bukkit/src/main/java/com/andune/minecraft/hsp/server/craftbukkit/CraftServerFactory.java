/**
 * 
 */
package com.andune.minecraft.hsp.server.craftbukkit;

import org.bukkit.Bukkit;

/** Class to determine which version of CraftServer we need at
 * runtime.
 * 
 * @author andune
 *
 */
public class CraftServerFactory {
    /**
     * Code heavily borrowed from mbaxter's AbstractionExamplePlugin.
     * 
     * @return
     */
    public static CraftServer getCraftServer() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        
        // Get full package string of CraftServer.
        // org.bukkit.craftbukkit.versionstring (or for pre-refactor, just org.bukkit.craftbukkit
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);

        // If the last element of the package was "craftbukkit" we are pre-refactor
        if (version.equals("craftbukkit")) {
            version = "pre";
        }
        try {
            final Class<?> clazz = Class.forName("com.andune.minecraft.hsp.server.craftbukkit." + version + ".CraftServerImpl");
            // Check if we have an implementation class at that location.
            if (CraftServer.class.isAssignableFrom(clazz)) { // Make sure it actually implements our interface
                return (CraftServer) clazz.getConstructor().newInstance();
            }
        }
        catch (Exception e) {
        }

        return new CraftServerNotAvailable();
    }
}
