/**
 * 
 */
package com.andune.minecraft.hsp.integration;

/**
 * @author andune
 *
 */
public interface PluginIntegration {
    /**
     * Whether or not the plugin is enabled. This can be based on configuration
     * and the existence of the actual plugin, but if this returns true it
     * signals admin intent and plugin availability for this plugin to be
     * put to use.
     * 
     * @return
     */
    public boolean isEnabled();
    
    /**
     * Return the version of the integration plugin that is detected.
     * 
     * @return the version string or possibly null
     */
    public String getVersion();
}
