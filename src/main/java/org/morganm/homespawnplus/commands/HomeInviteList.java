/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.entity.HomeInvite;
import org.morganm.homespawnplus.storage.dao.HomeInviteDAO;

/**
 * @author morganm
 *
 */
public class HomeInviteList extends BaseCommand {

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		HomeInviteDAO dao = plugin.getStorage().getHomeInviteDAO();
		
		Set<HomeInvite> invites = dao.findAllOpenInvites(p.getName());
		if( invites != null && invites.size() > 0 ) {
			util.sendMessage(p, "Open invites for your homes:");
			for(HomeInvite invite : invites) {
				String homeName = invite.getHome().getName();
				util.sendMessage(p, "-> Your home "+homeName+" has an open invite to player "+invite.getInvitedPlayer());
			}
		}
		else
			util.sendMessage(p, "You have no open invites to your homes");
		
		invites = dao.findAllAvailableInvites(p.getName());
		if( invites != null && invites.size() > 0 ) {
			util.sendMessage(p, "Open invites extended to you:");
			for(HomeInvite invite : invites) {
				String homeName = invite.getHome().getName();
				String playerName = invite.getHome().getPlayerName();
				util.sendMessage(p, "-> Player "+playerName+" has an open invite to you for their home named "+homeName);
			}
		}
		else
			util.sendMessage(p, "You have no invites to other players homes");

		return true;
	}

}
