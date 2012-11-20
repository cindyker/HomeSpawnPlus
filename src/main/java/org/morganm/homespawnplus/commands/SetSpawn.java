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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.config.old.ConfigOptions;
import org.morganm.homespawnplus.i18n.HSPMessages;


/**
 * @author morganm
 *
 */
public class SetSpawn extends BaseCommand
{
	@Override
	public String[] getCommandAliases() { return new String[] {"setglobalspawn"}; }

	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_SETSPAWN_USAGE);
	}

	@Override
	public boolean execute(Player p, org.bukkit.command.Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

		if( args.length > 0 ) {
			util.setSpawn(args[0], p.getLocation(), p.getName());
			util.sendLocalizedMessage(p, HSPMessages.CMD_SETSPAWN_SET_NAMED_SUCCESS, "name", args[0]);
		}
		else {
			util.setSpawn(p.getLocation(), p.getName());
			util.sendLocalizedMessage(p, HSPMessages.CMD_SETSPAWN_SET_SUCCESS);
			
			// also set map spawn if configured to do so
	    	if( plugin.getConfig().getBoolean(ConfigOptions.SETTING_WORLD_OVERRIDE, false) ) {
	    		final Location l = p.getLocation();
	    		l.getWorld().setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
				util.sendLocalizedMessage(p, HSPMessages.CMD_SETSPAWN_SET_SUCCESS);
				util.sendLocalizedMessage(p, HSPMessages.CMD_SETMAPSPAWN_SET_SUCCESS,
						"world", l.getWorld().getName(), "location", util.shortLocationString(l));
	    	}
		}
		
		return true;
	}
}
