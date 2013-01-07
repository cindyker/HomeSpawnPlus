/**
 * 
 */
package com.andune.minecraft.hsp.server.craftbukkit;

import org.bukkit.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author andune
 *
 */
public class CraftServerNotAvailable implements CraftServer {
    private static final Logger log = LoggerFactory.getLogger(CraftServerNotAvailable.class);
    
    // keep track of if we've already printed a warning, so we don't spam the log file
    private boolean hasWarned = false;

    @Override
    public void registerCommand(Command command) {
        if( !hasWarned ) {
            log.error("CraftBukkit server implementation not found, commands will not be enabled");
            hasWarned = true;
        }
    }

}
