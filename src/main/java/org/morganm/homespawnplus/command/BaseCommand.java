/**
 * 
 */
package org.morganm.homespawnplus.command;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.HomeSpawnUtils;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.manager.CooldownManager;
import org.morganm.homespawnplus.manager.WarmupManager;
import org.morganm.homespawnplus.manager.WarmupRunner;
import org.morganm.homespawnplus.util.Debug;


/** Abstract class that takes care of some routine tasks for commands, to keep those
 * objects as light as possible and so as to not violate the DRY principle.
 * 
 * @author morganm
 *
 */
public abstract class BaseCommand implements Command {
	protected Debug debug;
	protected HomeSpawnPlus plugin;
	protected HomeSpawnUtils util;
	protected CooldownManager cooldownManager;
	protected WarmupManager warmupManager;
	protected Logger log;
	protected String logPrefix;
	private boolean enabled;
	private String permissionNode;
	private String commandName;

	/** By default, commands do not respond to console input. They can override this if they wish
	 * to do so.
	 * 
	 */
	public boolean execute(ConsoleCommandSender console, org.bukkit.command.Command command, String[] args)
	{
		return false;
	}
	
	/** Returns this object for easy initialization in a command hash.
	 * 
	 * @param plugin
	 * @return
	 */
	public Command setPlugin(HomeSpawnPlus plugin) {
		this.debug = Debug.getInstance();
		this.plugin = plugin;
		this.util = plugin.getUtil();
		this.cooldownManager = plugin.getCooldownManager();
		this.warmupManager = plugin.getWarmupmanager();
		this.log = HomeSpawnPlus.log;
		this.logPrefix = HomeSpawnPlus.logPrefix;
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
	
	/** Check to see if player has sufficient money to pay for this command.
	 * 
	 * @param p
	 * @return
	 */
	protected boolean costCheck(Player p) {
		boolean returnValue = false;
		
		Economy economy = plugin.getEconomy();
		if( economy == null )
			returnValue = true;
		
		if( !returnValue && plugin.hasPermission(p, HomeSpawnPlus.BASE_PERMISSION_NODE + ".CostExempt." + getCommandName()) )
			returnValue = true;

		if( !returnValue ) {
			int price = getPrice(p);
			if( price > 0 ) {
				double balance = economy.getBalance(p.getName());
				if( balance >= price )
					returnValue = true;
			}
			else
				returnValue = true;	// no cost for this command
		}
		
		return returnValue;
	}
	
	protected int getPrice(Player p) {
		return util.getCommandCost(p, getCommandName());
	}
	
	protected void printInsufficientFundsMessage(Player p) {
		Economy economy = plugin.getEconomy();
		if( economy != null )
			util.sendLocalizedMessage(p, HSPMessages.COST_INSUFFICIENT_FUNDS,
					"price", economy.format(getPrice(p)),
					"balance", economy.format(economy.getBalance(p.getName())));
//			util.sendMessage(p, "Insufficient funds, you need at least "+economy.format(getPrice(p))
//					+ " (you only have "+economy.format(economy.getBalance(p.getName()))+")"
//				);
	}
	
	/**
	 * 
	 * @param p
	 * @return true on success, false if there was an error that should prevent the action from taking place
	 */
	protected boolean applyCost(Player p, boolean applyCooldown, String cooldownName) {
		boolean returnValue = false;
		
		Economy economy = plugin.getEconomy();
		if( economy == null )
			returnValue = true;
		
		if( !returnValue && plugin.hasPermission(p, HomeSpawnPlus.BASE_PERMISSION_NODE + ".CostExempt." + getCommandName()) )
			returnValue = true;

		if( !costCheck(p) ) {
			printInsufficientFundsMessage(p);
			returnValue = false;
		}
		else if( !returnValue ) {
			int price = getPrice(p);
			if( price > 0 ) {
				EconomyResponse response = economy.withdrawPlayer(p.getName(), price);
				
				if( response.transactionSuccess() ) {
					if( plugin.getHSPConfig().getBoolean(ConfigOptions.COST_VERBOSE, true) ) {
						// had an error report that might have been related to a null value
						// being returned from economy.format(price), so let's check for that
						// and protect against any error.
						String priceString = economy.format(price);
						if( priceString == null )
							priceString = ""+price;
						
						util.sendLocalizedMessage(p, HSPMessages.COST_CHARGED,
								"price", priceString,
								"command", getCommandName());
//						util.sendMessage(p, economy.format(price) + " charged for use of the " + getCommandName() + " command.");
					}
					
					returnValue = true;
				}
				else {
					util.sendLocalizedMessage(p, HSPMessages.COST_ERROR,
							"price", economy.format(price),
							"errorMessage", response.errorMessage);
//					util.sendMessage(p, "Error subtracting "+price+" from your account: "+response.errorMessage);
					returnValue = false;
				}
			}
			else
				returnValue = true;	// no cost for this command
		}
		
		// if applyCooldown flag is true and the returnValue is true, then apply the Cooldown now
		if( applyCooldown && returnValue == true )
			applyCooldown(p, cooldownName);
		
		return returnValue;
	}
	protected boolean applyCost(Player p, boolean applyCooldown) {
		return applyCost(p, applyCooldown, null);
	}
	protected boolean applyCost(Player p) {
		return applyCost(p, false);
	}
	
	protected void doWarmup(Player p, WarmupRunner wr) {
		if ( !isWarmupPending(p, wr.getWarmupName()) ) {
			warmupManager.startWarmup(p.getName(), wr);
			
			util.sendLocalizedMessage(p, HSPMessages.WARMUP_STARTED,
					"name", wr.getWarmupName(),
					"seconds", warmupManager.getWarmupTime(p, wr.getWarmupName()).warmupTime);
//			util.sendMessage(p, "Warmup "+wr.getWarmupName()+" started, you must wait "+
//					warmupManager.getWarmupTime(p, wr.getWarmupName()).warmupTime+" seconds.");
		}
		else
			util.sendLocalizedMessage(p, HSPMessages.WARMUP_ALREADY_PENDING, "name", wr.getWarmupName());
//			util.sendMessage(p, "Warmup already pending for "+wr.getWarmupName());
		
	}
	
	/** Most commands for this plugin check 3 things:
	 * 
	 *   1 - is the command enabled in the config?
	 *   2 - does the player have access to run the command?
	 *   3 - is the command on cooldown for the player?
	 *   
	 * This method just implements all 3 checks.
	 * 
	 * @param p the player object that is running the command
	 * 
	 * @return returns false if the checks fail and Command processing should stop, true if the command is allowed to continue
	 */
	protected boolean defaultCommandChecks(Player p) {
		debug.devDebug("enabled =",enabled);
		if( !enabled )
			return false;

		final boolean hasP = hasPermission(p);
		debug.devDebug("hasPermission =",hasP);
		if( !hasP )
			return false;

		final boolean cd = cooldownCheck(p);
		debug.devDebug("cooldownCheck = ",cd);
		if( !cd )
			return false;
		
		debug.devDebug("all defaultCommandChecks return true");
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
	protected boolean cooldownCheck(Player p, String cooldownName) {
		if( cooldownName == null )
			cooldownName = getCommandName();
		return cooldownManager.cooldownCheck(p, cooldownName);
	}
	protected boolean cooldownCheck(Player p) {
		return cooldownCheck(p, getCommandName());
	}
	
	protected void applyCooldown(Player p, String cooldownName) {
		if( cooldownName == null )
			cooldownName = getCommandName();
		cooldownManager.setCooldown(p,  cooldownName);
	}
	protected void applyCooldown(Player p) {
		applyCooldown(p,  getCommandName());
	}

	/**
	 * 
	 * @return true if this command & player has a warmup associated with it
	 */
	protected boolean hasWarmup(Player p, String warmupName) {
		return warmupManager.hasWarmup(p, warmupName);
	}
	protected boolean hasWarmup(Player p) {
		return hasWarmup(p, getCommandName());
	}
	
	/** check if a warmup is already pending for this command
	 * 
	 * @param p
	 * @return true if warmup is already pending, false if not
	 */
	protected boolean isWarmupPending(Player p, String warmupName) {
		return warmupManager.isWarmupPending(p.getName(), warmupName);
	}
	protected boolean isWarmupPending(Player p) {
		return isWarmupPending(p, getCommandName());
	}
	
	protected String getCooldownName(String baseName, String homeName) {
		if( baseName == null )
			baseName = getCommandName();
		
		if( homeName != null && cooldownManager.isCooldownSeparationEnabled(baseName) )
			return baseName + "." + homeName;
		else
			return baseName;
	}

	public String getCommandPermissionNode() {
		if( permissionNode == null )
			permissionNode = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command." + getCommandName();
		
		return permissionNode;
	}
	
	/** Return true if the player has permission to run this command.  If they
	 * don't have permission, print them a message saying so and return false.
	 * 
	 * @param p
	 * @return
	 */
	protected boolean hasPermission(Player p) {
		if( !plugin.hasPermission(p, getCommandPermissionNode()) ) {
			util.sendLocalizedMessage(p, HSPMessages.NO_PERMISSION);
//			p.sendMessage("You don't have permission to do that.");
			return false;
		}
		else
			return true;
	}
}
