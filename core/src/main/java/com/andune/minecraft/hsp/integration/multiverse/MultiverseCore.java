/**
 * 
 */
package com.andune.minecraft.hsp.integration.multiverse;

import com.andune.minecraft.hsp.integration.PluginIntegration;

/**
 * @author andune
 *
 */
public interface MultiverseCore extends PluginIntegration {
    public String getCurrentTeleporter();    
    public void setCurrentTeleporter(String name);
}
