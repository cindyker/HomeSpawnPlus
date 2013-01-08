/**
 * 
 */
package com.andune.minecraft.hsp.integration.dynmap;

import com.andune.minecraft.hsp.integration.PluginIntegration;

/**
 * @author andune
 *
 */
public interface DynmapModule extends PluginIntegration {
    /*
     * Dynmap module file is entirely contained within Bukkit at this time,
     * with all of the support classes contained here in core. Dynmap has
     * no services provided to the rest of HSP, it is simply "fire and
     * forget", setting up it's own listeners and responding to events
     * as needed.
     */
}
