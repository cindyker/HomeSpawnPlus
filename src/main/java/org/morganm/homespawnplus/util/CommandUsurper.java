/**
 * 
 */
package org.morganm.homespawnplus.util;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/** Class that implements the ability to take over commands from other plugins
 * at the direction of the server admin via the "usurpCommands" config setting.
 * 
 * @author morganm
 *
 */
public class CommandUsurper {
	private final JavaPlugin plugin;
	private final Logger log;
	private final String logPrefix;
	
	public CommandUsurper(JavaPlugin plugin, Logger log, String logPrefix) {
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
	public CommandUsurper(JavaPlugin plugin) {
		this(plugin, null, null);
	}
	
    public void usurpCommands() {
    	UsurpCommandExecutor usurp = new UsurpCommandExecutor(plugin);
    	
    	List<String> commands = plugin.getConfig().getStringList("usurpCommands");
    	if( commands != null ) {
	    	for(String command : commands) {
	        	PluginCommand cmd = plugin.getServer().getPluginCommand(command);
	        	if( cmd != null ) {
	        		log.info(logPrefix + " usurping command "+command+" as specified by usurpCommands config option");
		        	// TODO: "being nice" might be best to keep track of the "old" executor
		        	// and restore that if this plugin is unloaded. At this point, restoring
		        	// the old executor requires turning off the usurp config option and
		        	// restarting the server.
		        	cmd.setExecutor(usurp);
	        	}
	    	}
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
