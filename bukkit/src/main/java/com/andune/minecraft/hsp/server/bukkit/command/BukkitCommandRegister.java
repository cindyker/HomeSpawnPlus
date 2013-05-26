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
package com.andune.minecraft.hsp.server.bukkit.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;

import com.andune.minecraft.commonlib.FeatureNotImplemented;
import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.command.CommandConfig;
import com.andune.minecraft.commonlib.server.bukkit.BukkitFactory;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.command.Command;
import com.andune.minecraft.hsp.command.CustomEventCommand;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.server.craftbukkit.CraftServer;
import com.andune.minecraft.hsp.server.craftbukkit.CraftServerFactory;
import com.google.inject.Injector;

/** Class whose job is to register all of HSP commands with the server dynamically,
 * as opposed to through static plugin.yml configurations. This is used by HSP
 * to give admins maximum flexibility in naming their commands however they like
 * or creating their own custom commands.
 * 
 * @author andune
 *
 */
@Singleton
public class BukkitCommandRegister implements Initializable {
    private final Logger log = LoggerFactory.getLogger(BukkitCommandRegister.class);

	private final Map<String, Class<? extends Command>> customClassMap = new HashMap<String, Class<? extends Command>>();
	
	private final Plugin plugin;
	private final Map<String, PluginCommand> loadedCommands = new HashMap<String, PluginCommand>(25);
	private final Reflections reflections;
	private final CommandConfig commandConfig;
	private final BukkitFactory factory;
	private final Injector injector;
	private final CraftServerFactory craftServerFactory;
	private final Server server;
	
	@Inject
	public BukkitCommandRegister(Plugin plugin, CommandConfig commandConfig,
	        BukkitFactory factory, Injector injector, Reflections reflections,
	        Server server) {
		this.plugin = plugin;
		this.commandConfig = commandConfig;
		this.factory = factory;
		this.injector = injector;
		this.reflections = reflections;
		this.server = server;
		
		this.craftServerFactory = new CraftServerFactory(plugin);
        customClassMap.put("customeventcommand", CustomEventCommand.class);
	}
	
    @Override
    public void init() throws Exception {
        registerAllCommands();
    }

    @Override
    public void shutdown() throws Exception {}

    @Override
    public int getInitPriority() {
        return 6;
    }

	@SuppressWarnings("unchecked")
	private void register(final Command command, Map<String, Object> cmdParams) {
		String cmdName = command.getCommandName();
		
		log.debug("register() command={},cmdParams={}", command, cmdParams);
//		command.setPlugin(plugin);
		command.setCommandParameters(cmdParams);
		
		if( cmdParams.containsKey("name") )
			cmdName = (String) cmdParams.get("name");
		
		// we never load the same command twice
		if( loadedCommands.containsKey(cmdName) )
			return;

		CraftServer craftServer = craftServerFactory.getCraftServer();
		try {
			Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			constructor.setAccessible(true);
			
			// construct a new PluginCommand object
			PluginCommand pc = constructor.newInstance(cmdName, plugin);
			pc.setExecutor(new CommandExecutor() {
                public boolean onCommand(CommandSender sender,
                        org.bukkit.command.Command bukkitCommand, String label, String[] args) {
                    try {
                        com.andune.minecraft.commonlib.server.api.CommandSender apiSender = factory.getCommandSender(sender);
                        return command.execute(apiSender, label, args);
                    }
                    catch(FeatureNotImplemented fne) {
                        String msg = server.getLocalizedMessage(HSPMessages.FEATURE_NOT_IMPLEMENTED);
                        sender.sendMessage(msg);
                        log.info("Caught feature not implemented exception for command "+label+": "+fne.getMessage(), fne);
                        return true;
                    }
                }
            });
			pc.setLabel(cmdName);
			if( command.getUsage() != null )
				pc.setUsage(command.getUsage());

			// don't set Permission node, not all permission plugins behave
			// with superperms command permissions nicely.
//			pc.setPermission(command.getCommandPermissionNode());
			
			// check for aliases defined in the configuration
			Object o = cmdParams.get("aliases");
			if( o != null ) {
				List<String> aliases = null;
				if( o instanceof List ) {
					aliases = (List<String>) o;
				}
				else if( o instanceof String ) {
					aliases = new ArrayList<String>(2);
					aliases.add((String) o);
				}
				else
					log.warn("invalid aliases defined for command ",cmdName,": ",o);
				
				if( aliases == null )
					aliases = new ArrayList<String>(1);
				
				aliases.add("hsp"+command.getCommandName());	// all commands have "hsp" prefix alias
				pc.setAliases(aliases);
			}
			// otherwise set whatever the command has defined
			else {
				List<String> aliases = new ArrayList<String>(5);
				String[] strAliases = command.getCommandAliases();
				if( strAliases != null ) {
					for(String alias : strAliases) {
						aliases.add(alias);
					}
				}
				aliases.add("hsp"+command.getCommandName());	// all commands have "hsp" prefix alias
				pc.setAliases(aliases);
			}
			
			// register it
			craftServer.registerCommand(pc);
			loadedCommands.put(cmdName, pc);
			
			log.debug("register() command {} registered", command);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void register(Command command) {
		Map<String, Object> cmdParams = commandConfig.getCommandParameters(command.getCommandName());
		register(command, cmdParams);
	}
	
	public Map<String, PluginCommand> getLoadedCommands() {
	    return Collections.unmodifiableMap(loadedCommands);
	}
	
	/** Given an unqualified name, find a matching command class. This ignores case,
	 * so for example if you pass in "homedeleteother", it will find and return the
	 * class "com.andune.minecraft.hsp.commands.HomeDeleteOther". If it can't find
	 * any matching class, it will return null.
	 * 
	 * @param cmd
	 * @return
	 */
	private Class<? extends Command> findCommandClass(String cmd) {
		cmd = cmd.toLowerCase();
		
		Class<? extends Command> customClass = null;
		if( (customClass = customClassMap.get(cmd)) != null )
			return customClass;

		// now loop through all normal commands in the class path
		for(Class<? extends Command> clazz : getCommandClasses()) {
			String shortName = clazz.getSimpleName();
			if( shortName.equalsIgnoreCase(cmd) ) {
				return clazz;
			}
		}
		
		return null;
	}
	
	/** Given a command name, look for and register that command from
	 * the admin-configured command definitions.
	 * 
	 * @param cmd
	 * @param classes
	 */
	private void registerConfigCommand(String cmd) {
		log.debug("processing config defined command {}",cmd);
		Map<String, Object> cmdParams = commandConfig.getCommandParameters(cmd);
		
		Class<? extends Command> cmdClass = null;
		String className = null;
		Object clazz = cmdParams.get("class");
		
		// if no class given, just assume the name of the commmand
		if( clazz == null )
			clazz = cmd;
		
		if( clazz != null && clazz instanceof String ) {
			className = (String) clazz;
			
			// if class given, but no package given, assume default package
			if( className.indexOf('.') == -1 ) {
				String firstChar = className.substring(0, 1);
				String theRest = className.substring(1);
				className = firstChar.toUpperCase() + theRest;
				cmdClass = findCommandClass(className);
			}
		}
		
		// if we have no commandClass yet, but we do have a className, then
		// try to find that className.
		if( cmdClass == null && className != null ) {
			cmdClass = findCommandClass(className);
		}
		
		if( cmdClass == null ) {
			log.warn("No class defined or found for command ",cmd);
			return;
		}
		
		try {
			Command command = (Command) cmdClass.newInstance();
			command.setCommandName(cmd.toLowerCase());	// default to name of instance key
			register(command, cmdParams);
		}
		catch(ClassCastException e) {
			log.warn("class "+cmdClass+" does not implement Command interface");
		}
		catch(Exception e) {
			log.warn("error loading class {}", cmdClass, e);
		}
	}
	
	/** Given a class file (which must be of our Command interface),
	 * register that Command with Bukkit.
	 * 
	 * @param clazz
	 */
	private void registerDefaultCommand(Class<? extends Command> clazz) {
		try {
			log.debug("registering command class {}",clazz);
	        Command cmd = injector.getInstance(clazz);
//			Command cmd = (Command) clazz.newInstance();
			
			String cmdName = cmd.getCommandName();
			// do nothing if the command is disabled
			if( commandConfig.isDisabledCommand(cmdName) ) {
				log.debug("registerDefaultCommand() skipping {} because it is flagged as disabled", cmdName);
				return;
			}
			
			register(cmd);
		}
		catch(Exception e) {
		    log.error("error trying to load command class {}", clazz, e);
		}
	}
	
    private void registerAllCommands() {
		// loop through all config-defined command and load them up
		Set<String> commands = commandConfig.getDefinedCommands();
		for(String cmd : commands) {
			registerConfigCommand(cmd);
		}
		
		Set<Class<? extends Command>> commandClasses = getCommandClasses();
		// now loop through all normal commands in the class path
		for(Class<? extends Command> clazz : commandClasses) {
			log.debug("checking found class {}",clazz);
			registerDefaultCommand(clazz);
		}
	}
	
	/* cache object only, always use getCommandClasses() 
	 */
	private Set<Class<? extends Command>> commandClasses;
	/** Return all classes which extend our Command interface.
	 * 
	 * @return
	 */
	private Set<Class<? extends Command>> getCommandClasses() {
		if( commandClasses != null )
			return commandClasses;
		
		commandClasses = reflections.getSubTypesOf(Command.class);
		Set<Class<? extends BaseCommand>> baseCommandClasses = reflections.getSubTypesOf(BaseCommand.class);
		for(Class<? extends BaseCommand> bc : baseCommandClasses) {
			commandClasses.add((Class<? extends Command>) bc);
		}

    	if( commandClasses == null || commandClasses.size() == 0 ) {
    		log.error("No command classes found, HSP will not be able to register commands!");
    	}
    	
        // filter out any abstract classes
        for(Iterator<Class<? extends Command>> i = commandClasses.iterator(); i.hasNext();) {
            Class<? extends Command> commandClass = i.next();
            if( Modifier.isAbstract(commandClass.getModifiers()) )
                i.remove();
        }

    	return commandClasses;
	}
}
