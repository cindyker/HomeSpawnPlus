/**
 * 
 */
package org.morganm.homespawnplus.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.CommandRegister;

/** Class that implements the ability to take over commands from other plugins
 * at the direction of the server admin via the "usurpCommands" config setting.
 * 
 * @author morganm
 *
 */
public class CommandUsurper {
	private final HomeSpawnPlus plugin;
	private final Logger log;
	private final String logPrefix;
	
	public CommandUsurper(HomeSpawnPlus plugin, Logger log, String logPrefix) {
		this.plugin = plugin;
		if( log != null )
			this.log = log;
		else
			this.log = Logger.getLogger(PermissionSystem.class.toString());
		
		if( logPrefix != null )
			this.logPrefix = logPrefix;
		else
			this.logPrefix = "["+plugin.getDescription().getName()+"] ";
	}
	public CommandUsurper(HomeSpawnPlus plugin) {
		this(plugin, null, null);
	}
	
    public void usurpCommands() {
    	UsurpCommandExecutor usurp = new UsurpCommandExecutor(plugin);
    	
    	List<String> commands = plugin.getConfig().getStringList("usurpCommands");
    	if( commands != null ) {
	    	for(String command : commands) {
	        	PluginCommand cmd = plugin.getServer().getPluginCommand(command);
	        	if( cmd != null ) {
		        	cmd.setExecutor(usurp);
	        		log.info(logPrefix + " command "+command+" usurped as specified by usurpCommands config option");
	        	}
	        	else {
	        		removeCommand(command);
	            	CommandRegister register = new CommandRegister(plugin);
	            	register.registerCommand(command);
	        		log.info(logPrefix + " command "+command+" usurped as specified by usurpCommands config option");
	        	}
	    	}
    	}
    }

    /** Use reflection voodoo to reach into CraftBukkit command implementation
     * and remove a command from the active command map.
     * 
     * @param command
     */
	@SuppressWarnings("unchecked")
    private void removeCommand(String command) {
		CraftServer cs = (CraftServer) Bukkit.getServer();
		SimpleCommandMap commandMap = cs.getCommandMap();
		try {
			Field field = commandMap.getClass().getDeclaredField("knownCommands");
			field.setAccessible(true);
			Map<String, Command> knownCommands = (Map<String, Command>) field.get(commandMap);
			knownCommands.remove(command);
		}
		catch(Exception e) {
			log.log(Level.WARNING, "Could not remove command (usurp): "+command, e);
		}
    }
    
    /** Private class which is used to re-route commands being processed by other plugins
     * to our plugin instead (if the admin enabled config flag to do so).
     * 
     * @author morganm
     *
     */
    public class UsurpCommandExecutor implements CommandExecutor {
    	private JavaPlugin plugin;
    	
    	public UsurpCommandExecutor(JavaPlugin plugin) {
    		this.plugin = plugin;
		}
    	
		@Override
		public boolean onCommand(CommandSender sender, Command command, String commandLabel,
				String[] args) {
			return plugin.onCommand(sender, command, commandLabel, args);
		}
    	
    }
}
