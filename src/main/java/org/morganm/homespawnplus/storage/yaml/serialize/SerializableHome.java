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
package org.morganm.homespawnplus.storage.yaml.serialize;

import java.util.Map;

import org.bukkit.configuration.serialization.SerializableAs;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.storage.yaml.StorageYaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Class which maps Home entity to YAML using Bukkit's ConfigurationSerializable
 * interface.
 * 
 * @author morganm
 *
 */
@SerializableAs("Home")
public class SerializableHome extends AbstractSerializableEntityWithLocation<Home>
implements SerializableYamlObject<Home>
{
    private final static Logger log = LoggerFactory.getLogger(SerializableHome.class);
    
	private final static String ATTR_NAME = "name";
	private final static String ATTR_PLAYER_NAME = "player_name";
	private final static String ATTR_UPDATED_BY = "updatedBy";
	private final static String ATTR_DEFAULT_HOME = "defaultHome";
	private final static String ATTR_BED_HOME = "bedHome";
	
	public SerializableHome(Home home) {
		super(home);
	}
	
	public SerializableHome(Map<String, Object> map) {
		super(map);
		
		log.debug("SerializeHome constructor, map={}",map);
		
		Object o = map.get(ATTR_NAME);
		if( o instanceof String )
			getObject().setName((String) o);
		o = map.get(ATTR_PLAYER_NAME);
		if( o instanceof String )
			getObject().setPlayerName((String) o);
		o = map.get(ATTR_UPDATED_BY);
		if( o instanceof String )
			getObject().setUpdatedBy((String) o);
		o = map.get(ATTR_BED_HOME);
		if( o instanceof Boolean )
			getObject().setBedHome((Boolean) o);
		o = map.get(ATTR_DEFAULT_HOME);
		if( o instanceof Boolean )
			getObject().setDefaultHome((Boolean) o);
		
		// tell the currently loading StorageYaml that this object has been
		// loaded. @see org.morganm.homespawnplus.storage.yaml.HomeDAYYaml.temporaryAllObjects
		if( StorageYaml.getCurrentlyInitializingInstance() != null ) {
			StorageYaml.getCurrentlyInitializingInstance().homeLoaded(getObject());
		}
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put(ATTR_NAME, getObject().getName());
		map.put(ATTR_PLAYER_NAME, getObject().getPlayerName());
		map.put(ATTR_UPDATED_BY, getObject().getUpdatedBy());
		map.put(ATTR_BED_HOME, getObject().isBedHome());
		map.put(ATTR_DEFAULT_HOME, getObject().isDefaultHome());
		return map;
	}

	@Override
	protected Home newEntity() {
		return new Home();
	}
}
