/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.storage.Storage;


/**
 * @author morganm
 *
 */
public class SetHome extends BaseCommand
{
	private static final String SETHOME_NAMED_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.sethome.named";
	
	@Override
	public String[] getCommandAliases() { return new String[] {"homeset"}; }
	
	@Override
	public boolean execute(final Player p, final Command command, final String[] args) {
		debug.debug("sethome invoked. player=",p,"args=",args);
		if( !isEnabled() || !hasPermission(p) )
			return true;
//		if( !defaultCommandChecks(p) )
//			return true;
		
		if( !costCheck(p) ) {
			printInsufficientFundsMessage(p);
			return true;
		}

		String cooldownName = null;
		String homeName = null;

		if( args.length > 0 ) {
			if( plugin.hasPermission(p, SETHOME_NAMED_PERMISSION) ) {
				if( !args[0].equals(Storage.HSP_BED_RESERVED_NAME) && !args[0].endsWith("_" + Storage.HSP_BED_RESERVED_NAME )) {
					if( !cooldownCheck(p, cooldownName) )
						return true;
					
					homeName = args[0];
				}
				else
					util.sendMessage(p, "Cannot used reserved name "+args[0]);
			}
			else
				util.sendMessage(p, "You do not have permission to set named homes");
		}
		
		if( homeName != null ) {
			if( util.setNamedHome(p.getName(), p.getLocation(), homeName, p.getName()) ) {
				if( applyCost(p, true, getCooldownName(homeName)) )
					util.sendMessage(p, "Home \""+args[0]+"\" set successfully.");
			}
		}
		else {
			if( util.setHome(p.getName(), p.getLocation(), p.getName(), true, false) ) {
				if( applyCost(p, true, getCooldownName(null)) )
					util.sendMessage(p, "Default home set successfully.");
			}
		}

		return true;
	}

	private String getCooldownName(String homeName) {
		if( homeName != null && plugin.getHSPConfig().getBoolean(ConfigOptions.COOLDOWN_PER_HOME, false) )
			return getCommandName() + "." + homeName;
		else
			return getCommandName();
	}

}
