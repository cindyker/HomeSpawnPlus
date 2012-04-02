/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;

/**
 * @author morganm
 *
 */
public class HomeInviteAccept extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"hiaccept", "hia"}; }
	
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		org.morganm.homespawnplus.entity.Home h = plugin.getHomeInviteManager().getInvitedHome(p);
		if( h == null ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_HIACCEPT_NO_INVITE);
		}
		else {
			p.teleport(h.getLocation());
			util.sendLocalizedMessage(p, HSPMessages.CMD_HIACCEPT_TELEPORTED,
					"home", h.getName(), "player", h.getPlayerName());
			plugin.getHomeInviteManager().removeInvite(p);
		}
		
		return true;
	}

}
