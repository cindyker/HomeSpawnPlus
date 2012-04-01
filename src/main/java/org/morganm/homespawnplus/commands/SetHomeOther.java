/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;

/**
 * @author morganm
 *
 */
public class SetHomeOther extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"sethomeo", "sho"}; }

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(final Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		if(args.length < 1) {
			return false;
		}
		
		final String setter = p.getName();
		final String homeowner = args[0];
		final Location l = p.getLocation();
		
		if( args.length > 1 ) {
			if( util.setNamedHome(homeowner, l, args[1], setter) )
				util.sendLocalizedMessage(p, HSPMessages.CMD_SETHOMEOTHER_HOME_SET,
						"name", args[1], "player", homeowner);
//				util.sendMessage(p, "Home \""+args[1]+"\" set successfully for player "+homeowner);
		}
		else {
			if( util.setHome(homeowner, l, setter, true, false) )
				util.sendLocalizedMessage(p, HSPMessages.CMD_SETHOMEOTHER_DEFAULT_HOME_SET,
						"player", homeowner);
//				util.sendMessage(p, "Home set successfully for player "+homeowner);
		}
		
		return true;
	}

}
