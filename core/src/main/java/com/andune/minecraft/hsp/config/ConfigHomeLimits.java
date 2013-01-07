/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
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
