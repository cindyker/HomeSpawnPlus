/**
 * 
 */
package org.morganm.homespawnplus.command;

import java.util.Map;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;


/**
 * @author morganm
 *
 */
public interface Command extends CommandExecutor {
	public boolean execute(Player p, org.bukkit.command.Command command, String[] args);
	public boolean execute(ConsoleCommandSender console, org.bukkit.command.Command command, String[] args);
	public boolean execute(CommandSender sender, String[] args);

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
	public boolean isEnabled();
	
	public Command setPlugin(HomeSpawnPlus plugin);
	
	/** Return the permission node name required to use this command (if any).
	 * 
	 * @return
	 */
	public String getCommandPermissionNode();
	
	/** If this command takes any parameters, it can use this method to receive them.
	 * 
	 * @param params
	 */
	public void setCommandParameters(Map<String, Object> params);
}
