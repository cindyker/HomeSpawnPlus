/**
 * 
 */
package org.morganm.homespawnplus.config;

import javax.inject.Singleton;

import org.morganm.homespawnplus.Initializable;

/**
 * @author morganm
 *
 */
@Singleton
@ConfigOptions(fileName="homeInvites.yml", basePath="homeInvite")
public class ConfigHomeInvites extends ConfigBase implements Initializable {
    /**
     * Return the time (in seconds) for a sent invite to expire.
     * 
     * @return
     */
    public int getTimeout() {
        return super.getInt("timeout");
    }
    
    /**
     * Determine if invites are enabled for bedHomes.
     * 
     * @return
     */
    public boolean allowBedHomeInvites() {
        return super.getBoolean("allowBedHomeInvites");
    }
    
    /**
     * Determine if home invites should use shared home warmups instead of
     * their own warmups.
     * 
     * @return
     */
    public boolean useHomeWarmup() {
        return super.getBoolean("useHomeWarmup");
    }
    
    /**
     * Determine if home invites should use shared home cooldowns instead
     * of their own cooldowns.
     * 
     * @return
     */
    public boolean useHomeCooldown() {
        return super.getBoolean("useHomeCooldown");
    }
}
