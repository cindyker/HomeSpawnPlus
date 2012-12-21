/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.util.List;

/** Per-permission configs are precompiled into memory so they are fast to
 * use when they are needed, which is important since, by nature, they have
 * to iterate through every permission listed every time a player uses
 * functionality dependent upon per-permission configs.
 * 
 * This interface generically defines the interface for the per-permission
 * data entry so that it can be manipulated generically in base classes but
 * leave room for specific details that change from one config setup to the
 * next.
 * 
 * @author morganm
 *
 */
public abstract class PerPermissionEntry extends PerXEntry {
    protected List<String> permissions;

    /**
     * Get the permissions that are defined for this entry.
     * 
     * @return
     */
    public List<String> getPermissions() {
        return permissions;
    }
    
    /**
     * Set the permissions for this entry.
     * 
     * @param permissions
     */
    void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
