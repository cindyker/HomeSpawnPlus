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

import java.sql.Timestamp;
import java.util.Map;

import javax.inject.Inject;

import org.bukkit.configuration.serialization.SerializableAs;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.HomeInvite;
import org.morganm.homespawnplus.storage.dao.HomeDAO;

/**
 * @author morganm
 *
 */
@SerializableAs("HomeInvite")
public class SerializableHomeInvite extends AbstractSerializableBasicEntity<HomeInvite>
implements SerializableYamlObject<HomeInvite>
{
	private final static String ATTR_HOME = "home";
	private final static String ATTR_INVITED_PLAYER = "invitedPlayer";
	private final static String ATTR_EXPIRES = "expires";
	
	// TODO: need to figure out injection or a factory for this object..
    @Inject private HomeDAO homeDAO;
    
	public SerializableHomeInvite(HomeInvite homeInvite) {
		super(homeInvite);
	}
	
	public SerializableHomeInvite(Map<String, Object> map) {
		super(map);
		
		Object o = map.get(ATTR_HOME);
		if( o instanceof Integer ) {
			Home h = homeDAO.findHomeById((Integer) o);
			if( h != null )
				getObject().setHome(h);
		}
		o = map.get(ATTR_INVITED_PLAYER);
		if( o instanceof String )
			getObject().setInvitedPlayer((String) o);
		o = map.get(ATTR_EXPIRES);
		if( o instanceof Long )
			getObject().setExpires(new Timestamp((Long) o));
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put(ATTR_HOME, getObject().getHome().getId());
		map.put(ATTR_INVITED_PLAYER, getObject().getInvitedPlayer());
		Long expiresTime = null;
		if( getObject().getExpires() != null )
			expiresTime = getObject().getExpires().getTime();
		map.put(ATTR_EXPIRES, expiresTime);
		return map;
	}

	@Override
	protected HomeInvite newEntity() {
		return new HomeInvite();
	}

}
