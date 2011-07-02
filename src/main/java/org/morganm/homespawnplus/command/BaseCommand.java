/**
 * 
 */
package org.morganm.homespawnplus.command;

import org.bukkit.entity.Player;
import org.morganm.homespawnplus.CooldownManager;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.HomeSpawnUtils;
import org.morganm.homespawnplus.config.ConfigOptions;


/** Abstract class that takes care of some routine tasks for commands, to keep those
 * objects as light as possible and so as to not violate the DRY principle.
 * 
 * @author morganm
 *
 */
public abstract class BaseCommand implements Command {

	protected HomeSpawnPlus plugin;
	protected HomeSpawnUtils util;
	protected CooldownManager cooldownManager;
	private boolean enabled;
	private String permissionNode;
	private String commandName;

	/** Returns this object for easy initialization in a command hash.
	 * 
	 * @param plugin
	 * @return
	 */
	public Command setPlugin(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.util = plugin.getUtil();
		this.cooldownManager = plugin.getCooldownManager();
		enabled = !plugin.getConfig().getBoolean(getDisabledConfigFlag(), Boolean.FALSE);
		return this;
	}
	
	/** Return true if the command is enabled, false if it is not.
	 * 
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	protected String getDisabledConfigFlag() {
		return ConfigOptions.COMMAND_TOGGLE_BASE + getCommandName();
	}
	
	/** Most commands for this plugin check 3 things:
	 * 
	 *   1 - is the command enabled in the config?
	 *   2 - does the player have access to run the command?
	 *   3 - is the command on cooldown for the player?
	 *   
	 * This method just implements all 3 checks.
	 * 
	 * @param enabledConfigParam the config param to check to see if this command is enabled. Can be null to skip this check.
	 * @param p the player object that is running the command
	 * 
	 * @return returns false if the checks fail and Command processing should stop, true if the command is allowed to continue
	 */
	protected boolean defaultCommandChecks(Player p) {
		if( !enabled )
			return false;
		
		if(!hasPermission(p))
			return false;

		if( !cooldownCheck(p) )
			return false;
		
		return true;
	}
	
	/** Can be overridden, but default implementation just applies the command name
	 * as the lower case version of the class name of the implemented command.
	 */
	@Override
	public String getCommandName() {
		if( commandName == null ) {
			String className = this.getClass().getName();
			int index = className.lastIndexOf('.');
			commandName = className.substring(index+1).toLowerCase();
		}
		
		return commandName;
	}
	
	/** Here for convenience if the command has no aliases.  Otherwise, this should be overridden.
	 * 
	 */
	@Override
	public String[] getCommandAliases() {
		return null;
	}
	
	/** check the default command cooldown for the player
	 * 
	 * @param p
	 * @return true if cooldown is available, false if currently in cooldown period
	 */
	protected boolean cooldownCheck(Player p) {
		return cooldownManager.cooldownCheck(p, getCommandName());
	}

	/** Return true if the player has permission to run this command.  If they
	 * don't have permission, print them a message saying so and return false.
	 * 
	 * @param p
	 * @return
	 */
	protected boolean hasPermission(Player p) {
		if( permissionNode == null )
			permissionNode = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command." + getCommandName() + ".use";
		
		if( !plugin.hasPermission(p, permissionNode) ) {
			p.sendMessage("You don't have permission to do that.");
			return false;
		}
		else
			return true;
	}
}
