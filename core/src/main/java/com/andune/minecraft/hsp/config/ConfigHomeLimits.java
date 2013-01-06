/**
 * 
 */
package com.andune.minecraft.hsp.config;

import javax.inject.Singleton;

import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.config.ConfigHomeLimits.LimitsPerPermission;
import com.andune.minecraft.hsp.config.ConfigHomeLimits.LimitsPerWorld;

/**
 * @author morganm
 *
 */
@Singleton
@ConfigOptions(fileName="homeLimits.yml", basePath="homeLimits")
public class ConfigHomeLimits extends ConfigPerXBase<LimitsPerPermission, LimitsPerWorld> implements Initializable {
    /**
     * Determine if single global home setting is enabled.
     * 
     * @return
     */
    public boolean isSingleGlobalHome() {
        return super.getBoolean("singleGlobalHome");
    }
    
    public Integer getDefaultGlobalLimit() {
        return super.getInteger("default.global");
    }

    public Integer getDefaultPerWorldLimit() {
        return super.getInteger("default.perWorld");
    }

    @Override
    protected LimitsPerPermission newPermissionEntry() {
        return new LimitsPerPermission();
    }
    @Override
    protected LimitsPerWorld newWorldEntry() {
        return new LimitsPerWorld();
    }
    
    private class Entry {
        Integer perWorld = null;
        Integer global = null;
        
        public void setValue(String key, Object o) {
            if( key.equals("perWorld") ) {
                perWorld = (Integer) o;
            }
            else if( key.equals("global") ) {
                global = (Integer) o;
            }
        }
    }

    public class LimitsPerPermission extends PerPermissionEntry {
        private Entry entry = new Entry();
        
        public Integer getPerWorld() { return entry.perWorld; }
        public Integer getGlobal() { return entry.global; }
        
        @Override
        public void setValue(String key, Object o) {
            entry.setValue(key,  o);
        }
    }

    public class LimitsPerWorld extends PerWorldEntry {
        private Entry entry = new Entry();
        
        public Integer getPerWorld() { return entry.perWorld; }
        public Integer getGlobal() { return entry.global; }
        
        @Override
        public void setValue(String key, Object o) {
            entry.setValue(key,  o);
        }
    }
}
