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

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import com.andune.minecraft.hsp.entity.BasicEntity;

/** Abstract basic entity data members so the same code doesn't have to be repeated
 * for each entity.
 * 
 * @author morganm
 *
 */
public abstract class AbstractSerializableBasicEntity<T extends BasicEntity> implements ConfigurationSerializable {
	private final static String ATTR_ID = "id";
	private final static String ATTR_LAST_MODIFIED = "lastModified";
	private final static String ATTR_DATE_CREATED = "dateCreated";
	
	protected T object;
	
	public T getObject() { return object; }
	protected void setObject(T object) { this.object = object; } 
	
	public AbstractSerializableBasicEntity(T object) {
		setObject(object);
	}

	public AbstractSerializableBasicEntity(Map<String, Object> map) {
		setObject(newEntity());
		
		Object o = map.get(ATTR_ID);
		if( o instanceof Integer )
			object.setId((Integer) o);
		o = map.get(ATTR_LAST_MODIFIED);
		if( o instanceof Long )
			object.setLastModified(new Timestamp((Long) o));
		o = map.get(ATTR_DATE_CREATED);
		if( o instanceof Long )
			object.setDateCreated(new Timestamp((Long) o));
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new LinkedHashMap<String, Object>(15);
		map.put(ATTR_ID, getObject().getId());
		
		getObject().setLastModified(new Timestamp(System.currentTimeMillis()));
		map.put(ATTR_LAST_MODIFIED, getObject().getLastModified().getTime());
		
		if( getObject().getDateCreated() == null )
			getObject().setDateCreated(new Timestamp(System.currentTimeMillis()));
		map.put(ATTR_DATE_CREATED, getObject().getDateCreated().getTime());
		
		return map;
	}

	protected abstract T newEntity();
}
