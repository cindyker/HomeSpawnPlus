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
package org.morganm.homespawnplus.commands;

import javax.inject.Inject;

import org.morganm.homespawnplus.HSPMessages;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.util.SpawnUtil;

/**
 * @author morganm
 *
 */
public class SetDefaultSpawn extends BaseCommand {
    @Inject private SpawnUtil util;

	@Override
	public String getUsage() {
		return server.getLocalizedMessage(HSPMessages.CMD_SETDEFAULTSPAWN_USAGE);
	}

	@Override
	public boolean execute(Player p, String[] args) throws StorageException {
		if( args.length < 1 ) {
			server.sendLocalizedMessage(p, HSPMessages.CMD_SETDEFAULTSPAWN_SPECIFY_NAME);
			return true;
		}
		
		Spawn spawn = storage.getSpawnDAO().findSpawnByName(args[0]);
		if( spawn != null ) {
		    Location l = spawn.getLocation();
		    util.setDefaultWorldSpawn(l, p.getName());
		    server.sendLocalizedMessage(p, HSPMessages.CMD_SETDEFAULTSPAWN_SPAWN_CHANGED,
		            "name", spawn.getName(), "location", l.shortLocationString());
		}

		return true;
	}
}
