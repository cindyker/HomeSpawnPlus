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
package com.andune.minecraft.hsp.commands.uber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.reflections.Reflections;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.server.api.Factory;

/**
 * @author andune
 *
 */
public class HSP extends BaseUberCommand {
	@Inject com.andune.minecraft.hsp.commands.HSP hspCommand;
	
	private Map<String, String> hspCommandHelp = null;
	
    @Inject
    public HSP(Factory factory, Reflections reflections) {
        super(factory, reflections);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
    	// all /hsp uber commands are admin functions and are admin-only
        if( !permissions.isAdmin(sender) )
			return false;
    	
        if( args.length > 0 && hspCommand.findMatchingCommand(args[0]) != null ) {
        	return hspCommand.execute(sender, label, args);
        }
        else {
        	return super.execute(sender, label, args);
        }
    }
    
    @Override
    protected Map<String, String> getAdditionalHelp() {
    	if( hspCommandHelp != null )
    		return hspCommandHelp;
    	
    	hspCommandHelp = new HashMap<String, String>();
    	List<String> subCommands = hspCommand.getSubCommandNames();
    	for(String cmdName : subCommands) {
    		String help = server.getLocalizedMessage(HSPMessages.CMD_HSP_UBER_USAGE + "_" + cmdName.toUpperCase());
    		if( help != null )
    			hspCommandHelp.put(cmdName, help);
    		else
    			hspCommandHelp.put(cmdName, "(no additional help available)");
    	}
    	
    	return hspCommandHelp;
    }

    protected Map<String, String> getAdditionalHelpAliases() {
    	return hspCommand.getSubCommandAliases();
    }
}
