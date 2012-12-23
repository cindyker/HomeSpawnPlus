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
@ConfigOptions(fileName="dynmap.yml", basePath="dynmap")
public class ConfigDynmap extends ConfigBase implements Initializable {
    public boolean isEnabled() {
        return super.getBoolean("enabled");
    }
}
