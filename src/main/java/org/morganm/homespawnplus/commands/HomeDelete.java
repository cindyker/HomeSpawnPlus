/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;

/**
 * @author morganm
 *
 */
public class HomeDelete extends BaseCommand {

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !isEnabled() )
			return false;

		if( args.length < 1 ) {
			util.sendMessage(p, "Usage:");
			util.sendMessage(p, "  /homedelete player : delete \"player\"'s home on current world");
			util.sendMessage(p, "  /homedelete player <name> : delete \"player\"'s home named \"name\" on current world");
			util.sendMessage(p, "  /homedelete player w:world_name : delete \"player\"'s home on world \"world_name\"");
			util.sendMessage(p, "  /homedelete player w:world_name <name>: delete \"player\"'s home named \"name\" on world \"world_name\"");
			return true;
		}
		
		String worldName;
		if( args.length > 1 ) {
			worldName = args[1];
		}
		else {
			worldName = p.getWorld().getName();
		}
		
		final String playerName = args[0];
		org.morganm.homespawnplus.entity.Home home = util.getDefaultHome(playerName, worldName);
		
		// didn't find an exact match?  try a best guess match
		if( home == null )
			home = util.getBestMatchHome(playerName, worldName);
		
		if( home != null ) {
			util.sendMessage(p, "Teleporting to player home for "+home.getPlayerName()+" on world \""+home.getWorld()+"\"");
			if( applyCost(p) )
				p.teleport(home.getLocation());
		}
		else
			p.sendMessage("No home found for player "+playerName+" on world "+worldName);
		
		return true;
	}

}
