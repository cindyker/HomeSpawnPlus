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
package com.andune.minecraft.hsp.server.api;

import java.util.Map;

import com.andune.minecraft.commonlib.server.api.CommandSender;


/**
 * @author andune
 *
 */
public interface Command {

    /**
     * Execute this command.
     * 
     * @param sender the command sender (can be Console or Player)
     * @param cmd the command that caused this execute to be invoked. If this
     * command has aliases, this might be different than the command name.
     * @param args the arguments that were passed to the command
     * 
     * @return true if the command executed successfully, false if command execution should
     * continue looking for another command.
     */
	public boolean execute(CommandSender sender, String cmd, String[] args);

	/** If the command allows it's name to be set by external configuration, it
	 * should implement this call to allow it's name to be changed.
	 * 
	 * @param name
	 */
	public void setCommandName(String name);
	
	/** Return the name of the command.  Used in cooldown and permission checks as well as
	 * for matching the command the player types in.
	 * 
	 * @return
	 */
	public String getCommandName();
	
	/** Return any aliases for this command.  Can be null.
	 * 
	 * @return
	 */
	public String[] getCommandAliases();

	/** Commands can be disabled by configuration, this method allows them to declare
	 * themselves enabled or disabled.
	 * 
	 * @return
	 */
//	public boolean isEnabled();
	
//	public void setPlugin(Plugin plugin);
	
	/** If there is a custom permission node for this command, it should be
	 * returned here. If this returns null, the default command permission
	 * will be used.
	 * 
	 * @return
	 */
	public String getCommandPermissionNode();
	
	/** If this command takes any parameters, it can use this method to receive them.
	 * 
	 * @param params
	 */
	public void setCommandParameters(Map<String, Object> params);
	
	/** The usage string, which will be passed through to Bukkit PluginCommand so
	 * can use the Bukkit convention of <command> and newlines.
	 * 
	 * @return
	 */
	public String getUsage();
}
