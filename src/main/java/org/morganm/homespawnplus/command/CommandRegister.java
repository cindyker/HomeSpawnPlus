/**
 * 
 */
package org.morganm.homespawnplus.command;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.Logger;

/** Class whose job is to register all of HSP commands with Bukkit dynamically,
 * as opposed to through static plugin.yml configurations. This is used by HSP
 * to give admins maximum flexibility in naming their commands however they like.
 * 
 * @author morganm
 *
 */
public class CommandRegister {
	private static final String COMMANDS_PACKAGE = "org.morganm.homespawnplus.commands";
	private static final Map<String, Class<?>> customClassMap = new HashMap<String, Class<?>>();
	
	private final HomeSpawnPlus plugin;
	private final Logger log;
	private final Set<String> loadedCommands = new HashSet<String>(25);
	private CommandConfig commandConfig;
	
	static {
		customClassMap.put("customeventcommand", CustomEventCommand.class);
	}

	public CommandRegister(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.log = plugin.getLog();
		this.commandConfig = new CommandConfig(plugin.getLog());
	}
	
	public void setCommandConfig(CommandConfig commandConfig) {
		this.commandConfig = commandConfig;
	}
	
	@SuppressWarnings("unchecked")
	public void register(Command command, Map<String, Object> cmdParams) {
		String cmdName = command.getCommandName();
		
		log.devDebug("register() command=",command,",cmdParams=",cmdParams);
		command.setPlugin(plugin);
		command.setCommandParameters(cmdParams);
		
		if( cmdParams.containsKey("name") )
			cmdName = (String) cmdParams.get("name");
		
		// do nothing if the command is disabled
		if( commandConfig.isDisabledCommand(cmdName) ) {
			log.debug("register() skipping ",cmdName," because it is flagged as disabled");
			return;
		}
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
			pc.setExecutor(command);
			pc.setLabel(cmdName);
			pc.setPermission(command.getCommandPermissionNode());
			
			// check for aliases defined in the configuration
			Object o = cmdParams.get("aliases");
			if( o != null ) {
				if( o instanceof List ) {
					pc.setAliases((List<String>) o);
				}
				else if( o instanceof String ) {
					List<String> list = new ArrayList<String>(1);
					list.add((String) o);
					pc.setAliases(list);
				}
				else
					log.warn("invalid aliases defined for command ",cmdName,": ",o);
			}
			// otherwise set whatever the command has defined, if anything
			else if( command.getCommandAliases() != null )
				pc.setAliases(Arrays.asList(command.getCommandAliases()));
			
			// register it
			commandMap.register("hsp", pc);
			loadedCommands.add(cmdName);
			
			Debug.getInstance().devDebug("register() command ",command," registered");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void register(Command command) {
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
	private Class<?> findCommandClass(Class<?>[] classes, String cmd) {
		cmd = cmd.toLowerCase();
		
		Class<?> clazz = null;
		if( (clazz = customClassMap.get(cmd)) != null )
			return clazz;
					
		for(int i=0; i < classes.length; i++) {
			String shortName = classes[i].getSimpleName();
			if( shortName.equalsIgnoreCase(cmd) ) {
				return classes[i];
			}
		}
		
		return null;
	}
	
	/** Given a specific commandName, find the matching command and
	 * register it. This is useful in command usurping.
	 * 
	 * @param commandName
	 */
	public boolean registerCommand(String commandName) {
		Class<?>[] classes = plugin.getJarUtils().getClasses(COMMANDS_PACKAGE);

		if( isDefinedConfigCommand(commandName) ) {
			registerConfigCommand(commandName, classes);
			return true;
		}
		else {
			Class<?> clazz = findDefaultCommand(commandName);
			if( clazz != null ) {
				registerDefaultCommand(clazz);
				return true;
			}
		}
		
		return false;
	}
	
	/** Return true if the given command is defined as a custom command
	 * by the admin in the config file.
	 * 
	 * @param cmd
	 * @return
	 */
	private boolean isDefinedConfigCommand(String cmd) {
		return commandConfig.getDefinedCommands().contains(cmd);
	}
	
	/** Given a command name, look for and register that command from
	 * the admin-configured command definitions.
	 * 
	 * @param cmd
	 * @param classes
	 */
	private void registerConfigCommand(String cmd, Class<?>[] classes) {
		log.devDebug("processing config defined command ",cmd);
		Map<String, Object> cmdParams = commandConfig.getCommandParameters(cmd);
		
		Class<?> cmdClass = null;
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
				cmdClass = findCommandClass(classes, className);
			}
		}
		
		// if we have no commandClass yet, but we do have a className, then
		// try to find that className.
		if( cmdClass == null && className != null ) {
			try {
				cmdClass = Class.forName(className);
			}
			catch(ClassNotFoundException e) {
				log.warn(e, "class ",className," not found");
				return;
			}
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
			log.warn(e, "error loading class "+cmdClass);
		}
	}
	
	/** Given a class file (which must be of our Command interface),
	 * register that Command with Bukkit.
	 * 
	 * @param clazz
	 */
	private void registerDefaultCommand(Class<?> clazz) {
		try {
			Class<?> superClass = clazz.getSuperclass();
			
			if( BaseCommand.class.equals(superClass) ) {
				Debug.getInstance().devDebug("registering command class ",clazz);
				Command cmd = (Command) clazz.newInstance();
				register(cmd);
			}
			// implements our Command interface?
			else {
				Class<?>[] interfaces = clazz.getInterfaces();
				for(Class<?> iface : interfaces) {
					if( iface.equals(Command.class) ) {
						Debug.getInstance().devDebug("registering command interface ",clazz);
						Command cmd = (Command) clazz.newInstance();
						register(cmd);
					}
				}
			}
		}
		catch(Exception e) {
			log.severe(e, "error trying to load command class "+clazz);
		}
	}
	
	/** Given a commandName, find the default command which matches.
	 * 
	 * @param cmdName
	 * @return
	 */
	private Class<?> findDefaultCommand(String cmdName) {
		Class<?>[] classes = plugin.getJarUtils().getClasses(COMMANDS_PACKAGE);
		for(int i=0; i < classes.length; i++) {
			Class<?> clazz = classes[i];
			
			try {
				Class<?> superClass = clazz.getSuperclass();
				
				if( BaseCommand.class.equals(superClass) ) {
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
				// implements our Command interface?
				else {
					Class<?>[] interfaces = clazz.getInterfaces();
					for(Class<?> iface : interfaces) {
						if( iface.equals(Command.class) ) {
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
					}
				}
			}
			catch(Exception e) {
				log.severe(e, "Caught exception in findDefaultCommand for command "+cmdName);
			}
		}
		
		return null;
	}
	
	/** Register all known HSP commands. This includes those defined by
	 * the admin in the config file as well as all commands found
	 * automatically on the command path.
	 * 
	 */
	public void registerAllCommands() {
		Class<?>[] classes = plugin.getJarUtils().getClasses(COMMANDS_PACKAGE);
		
		// loop through all config-defined command and load them up
		Set<String> commands = commandConfig.getDefinedCommands();
		for(String cmd : commands) {
			registerConfigCommand(cmd, classes);
		}
		
		// now loop through all normal commands in the class path
		for(int i=0; i < classes.length; i++) {
			log.devDebug("checking found class ",classes[i]);
			registerDefaultCommand(classes[i]);
		}
	}
}
