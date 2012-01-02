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
public class HomeOther extends BaseCommand {
//	private static final String OTHER_HOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.home.others";

	@Override
	public String[] getCommandAliases() { return new String[] {"homeo"}; }
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		if( args.length < 1 ) {
			util.sendMessage(p, command.getUsage());
//			util.sendMessage(p, "Usage:");
//			util.sendMessage(p, "  /homeother player : go to \"player\"'s home on current world");
//			util.sendMessage(p, "  /homeother player w:world_name : go to \"player\"'s home on world \"world_name\"");
//			util.sendMessage(p, "  /homeother player home_name : go to \"player\"'s home named \"home_name\"");
			return true;
		}
		
		final String playerName = args[0];
		String worldName = null;
		String homeName = null;
		
		for(int i=1; i < args.length; i++) {
			if( args[i].startsWith("w:") ) {
				worldName = args[i].substring(2);
			}
			else {
				if( homeName != null ) {
					util.sendLocalizedMessage(p, HSPMessages.TOO_MANY_ARGUMENTS);
//					util.sendMessage(p,  "Too many arguments");
					return true;
				}
				homeName = args[i];
			}
		}
		
		if( worldName == null )
			worldName = p.getWorld().getName();
		
		org.morganm.homespawnplus.entity.Home home;
		if( homeName != null ) {
			home = plugin.getStorage().getNamedHome(homeName, playerName);
		}
		else {
			home = util.getDefaultHome(playerName, worldName);
		}
		
		// didn't find an exact match?  try a best guess match
		if( home == null )
			home = util.getBestMatchHome(playerName, worldName);
		
		if( home != null ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEOTHER_TELEPORTING,
					"home", home.getName(), "player", home.getPlayerName(), "world", home.getWorld());
//			util.sendMessage(p, "Teleporting to player home for "+home.getPlayerName()+" on world \""+home.getWorld()+"\"");
			if( applyCost(p) )
				p.teleport(home.getLocation());
		}
		else if( homeName != null )
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_NO_HOME_FOUND,
					"home", homeName, "player", playerName);
//			util.sendMessage(p, "No home found for player "+playerName+" on world "+worldName);
		else
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_NO_DEFAULT_HOME_FOUND,
					"player", playerName, "world", worldName);
		
		return true;
	}

}
