/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.storage.Storage;


/**
 * @author morganm
 *
 */
public class SetHome extends BaseCommand
{
//	private static final String OTHER_SETHOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + "command.sethome.others";
	
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		debug.debug("sethome invoked. player=",p,"args=",args);
		if( !defaultCommandChecks(p) )
			return true;
		
		if( !applyCost(p) )
			return true;
		
		if( args.length > 0 ) {
			if( !args[0].equals(Storage.HSP_BED_RESERVED_NAME) ) {
				if( util.setNamedHome(p.getName(), p.getLocation(), args[0], p.getName()) )
					util.sendMessage(p, "Home \""+args[0]+"\" set successfully.");
			}
			else
				util.sendMessage(p, "Cannot used reserved name "+Storage.HSP_BED_RESERVED_NAME);
		}
		else {
			if( util.setHome(p.getName(), p.getLocation(), p.getName(), true, false) )
				util.sendMessage(p, "Default home set successfully.");
		}

		return true;
	}

}
