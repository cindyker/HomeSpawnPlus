/**
 * 
 */
package com.andune.minecraft.hsp.server.api;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.hsp.HSPMessages;

/**
 * @author andune
 *
 */
public interface Server extends com.andune.minecraft.commonlib.server.api.Server {
    public String getLocalizedMessage(HSPMessages key, Object... args);
    public void sendLocalizedMessage(CommandSender sender, HSPMessages key, Object... args);
}
