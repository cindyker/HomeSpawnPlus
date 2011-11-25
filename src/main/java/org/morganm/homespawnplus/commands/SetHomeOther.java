/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;

/**
 * @author morganm
 *
 */
public class SetHomeOther extends BaseCommand {

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(final Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		if(args.length < 1) {
			util.sendMessage(p, "Usage:");
			util.sendMessage(p, "  /sethomeother <player> : set <player>'s default home to the current location");
			util.sendMessage(p, "  /sethomeother <player> <name> : set <player>'s home named <name> to the current location");
			
			return true;
		}
		
		final String setter = p.getName();
		final String homeowner = args[0];
		final Location l = p.getLocation();
		
		if( args.length > 1 ) {
			util.setNamedHome(homeowner, l, args[1], setter);
			util.sendMessage(p, "Home \""+args[1]+"\" set successfully for player "+homeowner);
		}
		else {
			util.setHome(homeowner, l, setter, true, false);
			util.sendMessage(p, "Home set successfully for player "+homeowner);
		}
		
		return true;
	}

}
