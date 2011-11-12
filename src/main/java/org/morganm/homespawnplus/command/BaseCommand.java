/**
 * 
 */
package org.morganm.homespawnplus.command;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.entity.Player;
import org.morganm.homespawnplus.CooldownManager;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.HomeSpawnUtils;
import org.morganm.homespawnplus.WarmupManager;
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
	protected WarmupManager warmupManager;
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
		this.warmupManager = plugin.getWarmupmanager();
		enabled = !plugin.getHSPConfig().getBoolean(getDisabledConfigFlag(), Boolean.FALSE);
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
	
	/**
	 * 
	 * @param p
	 * @return true on success, false if there was an error that should prevent the action from taking place
	 */
	protected boolean applyCost(Player p) {
		Economy economy = plugin.getEconomy();
		if( economy == null )
			return true;
		
		if( plugin.hasPermission(p, HomeSpawnPlus.BASE_PERMISSION_NODE + ".CostExempt." + getCommandName()) )
			return true;

		int price = plugin.getHSPConfig().getInt(ConfigOptions.COST_BASE + getCommandName(), 0);
		if( price > 0 ) {
			EconomyResponse response = economy.withdrawPlayer(p.getName(), price);
			
			if( response.transactionSuccess() ) {
				if( plugin.getHSPConfig().getBoolean(ConfigOptions.COST_VERBOSE, true) ) {
					util.sendMessage(p, price + " charged for use of the " + getCommandName() + " command.");
				}
				return true;
			}
			else {
				util.sendMessage(p, "Error subtracting "+price+" from your account: "+response.errorMessage);
				return false;
			}
		}
		else
			return true;	// no cost for this command
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

	/**
	 * 
	 * @return true if this command & player has a warmup associated with it
	 */
	protected boolean hasWarmup(Player p) {
		return warmupManager.hasWarmup(p, getCommandName());
	}
	
	/** check if a warmup is already pending for this command
	 * 
	 * @param p
	 * @return true if warmup is already pending, false if not
	 */
	protected boolean isWarmupPending(Player p) {
		return warmupManager.isWarmupPending(p.getName(), getCommandName());
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
