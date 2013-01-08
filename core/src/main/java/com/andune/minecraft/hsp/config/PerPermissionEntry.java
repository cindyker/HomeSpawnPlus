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
 * @author andune
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
