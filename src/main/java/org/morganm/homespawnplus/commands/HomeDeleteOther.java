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
public class HomeDeleteOther extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"hdo"}; }
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return false;

		if( args.length < 1 ) {
			util.sendMessage(p, command.getUsage());
//			util.sendMessage(p, "Usage:");
//			util.sendMessage(p, "  /homedelete player : delete \"player\"'s home on current world");
//			util.sendMessage(p, "  /homedelete player <name> : delete \"player\"'s home named \"name\" on current world");
//			util.sendMessage(p, "  /homedelete player w:world_name : delete \"player\"'s default home on world \"world_name\"");
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
		
		if( home != null ) {
			plugin.getStorage().deleteHome(home);
			if( homeName != null )
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_HOME_DELETED,
						"home", homeName, "player", playerName);
			else
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_DEFAULT_HOME_DELETED,
						"player", playerName, "world", worldName);
			
//			String msg = null;
//			if( homeName != null )
//				msg = "Home named "+homeName+" for player "+playerName+" deleted.";
//			else
//				msg = "Default home for player "+playerName+" on world "+worldName+" deleted";
//			util.sendMessage(p, msg);
		}
		else if( homeName != null ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_NO_HOME_FOUND,
					"home", homeName, "player", playerName);
//			p.sendMessage("No home named "+homeName+" found for player "+playerName);
		}
		else
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_NO_DEFAULT_HOME_FOUND,
					"player", playerName, "world", worldName);
//			p.sendMessage("No home found for player "+playerName+" on world "+worldName);
		
		return true;
	}

}
