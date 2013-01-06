/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
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
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package com.andune.minecraft.hsp.storage.yaml.serialize;

import java.util.Map;

import com.andune.minecraft.hsp.entity.EntityWithLocation;

/** Abstract entity location data so the same code doesn't have to be repeated
 * for each entity.
 * 
 * @author morganm
 *
 */
public abstract class AbstractSerializableEntityWithLocation<T extends EntityWithLocation> extends
		AbstractSerializableBasicEntity<T> {
	private final static String ATTR_WORLD = "world";
	private final static String ATTR_X = "x";
	private final static String ATTR_Y = "y";
	private final static String ATTR_Z = "z";
	private final static String ATTR_PITCH = "pitch";
	private final static String ATTR_YAW = "yaw";
	
	public AbstractSerializableEntityWithLocation(T object) {
		super(object);
	}

	public AbstractSerializableEntityWithLocation(Map<String, Object> map) {
		super(map);
		
		Object o = map.get(ATTR_WORLD);
		if( o instanceof String )
			getObject().setWorld((String) o);
		o = map.get(ATTR_X);
		if( o instanceof Double )
			getObject().setX((Double) o);
		o = map.get(ATTR_Y);
		if( o instanceof Double )
			getObject().setY((Double) o);
		o = map.get(ATTR_Z);
		if( o instanceof Double )
			getObject().setZ((Double) o);
		o = map.get(ATTR_PITCH);
		if( o instanceof Double )
			getObject().setPitch(((Double) o).floatValue());
		o = map.get(ATTR_YAW);
		if( o instanceof Double )
			getObject().setYaw(((Double) o).floatValue());
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put(ATTR_WORLD, getObject().getWorld());
		map.put(ATTR_X, getObject().getX());
		map.put(ATTR_Y, getObject().getY());
		map.put(ATTR_Z, getObject().getZ());
		map.put(ATTR_PITCH, getObject().getPitch());
		map.put(ATTR_YAW, getObject().getYaw());
		return map;
	}
}
