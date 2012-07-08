/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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
		final Location l = p.getLocation();
		
		String homeowner = null;
		// try player name best match
		final OfflinePlayer otherPlayer = util.getBestMatchPlayer(args[0]);
		if( otherPlayer != null ) {
			homeowner = otherPlayer.getName();
		}
		// no match, no point in proceeding, no online or offline player by
		// that name exists
		else {
			util.sendLocalizedMessage(p, HSPMessages.PLAYER_NOT_FOUND,
					"player", args[0]);
			return true;
		}
		
		if( args.length > 1 ) {
			if( util.setNamedHome(homeowner, l, args[1], setter) )
				util.sendLocalizedMessage(p, HSPMessages.CMD_SETHOMEOTHER_HOME_SET,
						"name", args[1], "player", homeowner);
		}
		else {
			if( util.setHome(homeowner, l, setter, true, false) )
				util.sendLocalizedMessage(p, HSPMessages.CMD_SETHOMEOTHER_DEFAULT_HOME_SET,
						"player", homeowner);
		}
		
		return true;
	}

}
