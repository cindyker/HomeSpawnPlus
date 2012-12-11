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

import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.util.SpawnUtil;


/**
 * @author morganm
 *
 */
public class SetGroupSpawn extends BaseCommand
{
    @Inject private SpawnUtil util;

	@Override
	public String getUsage() {
		return server.getLocalizedMessage(HSPMessages.CMD_SETGROUPSPAWN_USAGE);
	}

	@Override
	public boolean execute(Player p, String[] args) {
		if( args.length < 1 )
			return false;
		
		String group = args[0];
		try {
    		util.setGroupSpawn(group, p.getLocation(), p.getName());
    		p.sendMessage("Group spawn for "+group+" set successfully!");
		}
        catch(StorageException e) {
            log.warn("Caught exception in command /"+getCommandName(), e);
            server.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
        }
		
		return true;
	}
}
