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
package org.morganm.homespawnplus.server.bukkit.command;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_4_5.CraftServer;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.Initializable;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.command.Command;
import org.morganm.homespawnplus.command.CustomEventCommand;
import org.morganm.homespawnplus.server.api.command.CommandConfig;
import org.morganm.homespawnplus.server.bukkit.BukkitFactory;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/** Class whose job is to register all of HSP commands with Bukkit dynamically,
 * as opposed to through static plugin.yml configurations. This is used by HSP
 * to give admins maximum flexibility in naming their commands however they like.
 * 
 * @author morganm
 *
 */
public class BukkitCommandRegister implements Initializable {
    private final Logger log = LoggerFactory.getLogger(BukkitCommandRegister.class);

//	private static final String COMMANDS_PACKAGE = "org.morganm.homespawnplus.commands";
	private final Map<String, Class<? extends Command>> customClassMap = new HashMap<String, Class<? extends Command>>();
	
	private final Plugin plugin;
	private final Set<String> loadedCommands = new HashSet<String>(25);
	private final Reflections reflections;
	private final CommandConfig commandConfig;
	private final BukkitFactory factory;
	private final Injector injector;
	
	@Inject
	public BukkitCommandRegister(Plugin plugin, CommandConfig commandConfig, BukkitFactory factory, Injector injector) {
		this.plugin = plugin;
		this.commandConfig = commandConfig;
		this.factory = factory;
		this.injector = injector;
		
        customClassMap.put("customeventcommand", CustomEventCommand.class);

//    	reflections = new Reflections(COMMANDS_PACKAGE);
    	reflections = Reflections.collect();
	}
	
    @Override
    public void init() throws Exception {
        registerAllCommands();
    }

    @Override
    public int getPriority() {
        return 5;
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
		if( loadedCommands.contains(cmdName) )
			return;
		
		CraftServer cs = (CraftServer) Bukkit.getServer();
		SimpleCommandMap commandMap = cs.getCommandMap();
		
		try {
			Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			constructor.setAccessible(true);
			
			// construct a new PluginCommand object
			PluginCommand pc = constructor.newInstance(cmdName, plugin);
			pc.setExecutor(new CommandExecutor() {
                public boolean onCommand(CommandSender sender,
                        org.bukkit.command.Command bukkitCommand, String label, String[] args) {
                    org.morganm.homespawnplus.server.api.CommandSender apiSender = factory.getCommandSender(sender);
                    return command.execute(apiSender, args);
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
			commandMap.register("hsp", pc);
			loadedCommands.add(cmdName);
			
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
	
	/** Given an unqualified name, find a matching command class. This ignores case,
	 * so for example if you pass in "homedeleteother", it will find and return the
	 * class "org.morganm.homespawnplus.commands.HomeDeleteOther". If it can't find
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
    	
    	return commandClasses;
	}

    /** Given a specific commandName, find the matching command and
     * register it. This is useful in command usurping.
     * 
     * No longer used since command usurping is no longer used. -11/16/12 morganm
     * 
     * @param commandName
     */
    /*
    private boolean registerCommand(String commandName) {
        if( isDefinedConfigCommand(commandName) ) {
            registerConfigCommand(commandName);
            return true;
        }
        else {
            Class<? extends Command> clazz = findDefaultCommand(commandName);
            if( clazz != null ) {
                registerDefaultCommand(clazz);
                return true;
            }
        }
        
        return false;
    }
    */

    /** Given a commandName, find the default command which matches.
     * 
     * @param cmdName
     * @return
     */
    /*
    private Class<? extends Command> findDefaultCommand(String cmdName) {
        Set<Class<? extends Command>> classes = getCommandClasses();
        
        for(Class<? extends Command> clazz : classes) {
            try {
                Command cmd = (Command) clazz.newInstance();
                if( cmd.getCommandName().equals(cmdName) )
                    return clazz;
                
                String[] aliases = cmd.getCommandAliases();
                if( aliases != null && aliases.length > 0 ) {
                    for(String alias : aliases) {
                        if( alias.equals(cmdName) )
                            return clazz;
                    }
                }
            }
            catch(Exception e) {
                log.severe(e, "Caught exception in findDefaultCommand for command "+cmdName);
            }
        }
        
        return null;
    }
    */
    
    /** Return true if the given command is defined as a custom command
     * by the admin in the config file.
     * 
     * @param cmd
     * @return
     */
    /*
    private boolean isDefinedConfigCommand(String cmd) {
        return commandConfig.getDefinedCommands().contains(cmd);
    }
    */
}
