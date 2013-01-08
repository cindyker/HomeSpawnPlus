/**
 * 
 */
package com.andune.minecraft.hsp.integration.multiverse;

import com.andune.minecraft.hsp.integration.PluginIntegration;

/**
 * @author andune
 *
 */
public interface MultiversePortals extends PluginIntegration {
    public String getSourcePortalName();
    public void setSourcePortalName(String sourcePortalName);
    public String getDestinationPortalName();
    public void setDestinationPortalName(String destinationPortalName);
}
