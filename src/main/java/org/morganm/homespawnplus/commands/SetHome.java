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

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.Storage;


/**
 * @author morganm
 *
 */
public class SetHome extends BaseCommand
{
	private static final String SETHOME_NAMED_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.sethome.named";
	
	@Override
	public String[] getCommandAliases() { return new String[] {"homeset"}; }
	
	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_SETHOME_USAGE);
	}

	@Override
	public boolean execute(final Player p, final Command command, final String[] args) {
		debug.debug("sethome invoked. player=",p,"args=",args);
		if( !isEnabled() || !hasPermission(p) )
			return true;
//		if( !defaultCommandChecks(p) )
//			return true;
		
		String cooldownName = null;
		String homeName = null;

		if( args.length > 0 ) {
			if( plugin.hasPermission(p, SETHOME_NAMED_PERMISSION) ) {
				if( !args[0].equals(Storage.HSP_BED_RESERVED_NAME) && !args[0].endsWith("_" + Storage.HSP_BED_RESERVED_NAME )) {
					homeName = args[0];
					cooldownName = getCooldownName("sethome-named", homeName);
				}
				else {
					util.sendLocalizedMessage(p, HSPMessages.CMD_SETHOME_NO_USE_RESERVED_NAME, "name", args[0]);
//					util.sendMessage(p, "Cannot used reserved name "+args[0]);
					return true;
				}
			}
			else {
				util.sendLocalizedMessage(p, HSPMessages.CMD_SETHOME_NO_NAMED_HOME_PERMISSION);
//				util.sendMessage(p, "You do not have permission to set named homes");
				return true;
			}
		}
		
		if( !cooldownCheck(p, cooldownName) )
			return true;
		
		if( !costCheck(p) ) {
			printInsufficientFundsMessage(p);
			return true;
		}

		if( homeName != null ) {
			if( util.setNamedHome(p.getName(), p.getLocation(), homeName, p.getName()) ) {
				if( applyCost(p, true, cooldownName) )
					util.sendLocalizedMessage(p, HSPMessages.CMD_SETHOME_HOME_SET, "name", homeName);
//					util.sendMessage(p, "Home \""+homeName+"\" set successfully.");
			}
		}
		else {
			if( util.setHome(p.getName(), p.getLocation(), p.getName(), true, false) ) {
				if( applyCost(p, true, cooldownName) )
					util.sendLocalizedMessage(p, HSPMessages.CMD_SETHOME_DEFAULT_HOME_SET);
//					util.sendMessage(p, "Default home set successfully.");
			}
		}

		return true;
	}
}
