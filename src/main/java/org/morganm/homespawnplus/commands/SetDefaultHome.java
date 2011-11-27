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
public class SetDefaultHome extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"sdh"}; }
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		if( args.length < 1 ) {
			util.sendMessage(p, "You must specify your home name to set as default");
		}
		else {
			String homeName = args[0];
			
			org.morganm.homespawnplus.entity.Home home = util.getHomeByName(p.getName(), homeName);
			
			if( home != null ) {
				home.setDefaultHome(true);
				plugin.getStorage().writeHome(home);
				util.sendMessage(p, "Your default home on world "+home.getWorld()+" has been changed to your home named "+home.getName());
			}
		}
		
		return true;
	}

}
