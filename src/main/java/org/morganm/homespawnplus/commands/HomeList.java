/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;

/**
 * @author morganm
 *
 */
public class HomeList extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"homel", "listhomes", "hl"}; }
	
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		String world = "all";
		if( args.length > 0 )
			world = args[0];
		
		return executeCommand(p, p.getName(), world);
	}

	/** Package visibility, code is reused by HomeListOther.
	 * 
	 * @param p
	 * @param command
	 * @param args
	 * @return
	 */
	boolean executeCommand(CommandSender p, String player, String world) {
		Set<org.morganm.homespawnplus.entity.Home> homes;
		
		homes = plugin.getStorage().getHomeDAO().findHomesByWorldAndPlayer(world, player);
		
		if( homes != null && homes.size() > 0 ) {
			/*
			 *  MC uses variable-width font, so tabular sprintf-style formatted strings don't
			 *  line up properly.  Boo.
			util.sendMessage(p, String.format("%-16s %12s/%6s/%6s/%6s %-8s",
					"name",
					"world",
					"x","y","z",
					"default"));
					*/

			if( world.equals("all") || world.equals("*") )
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOMELIST_ALL_WORLDS);
//				util.sendMessage(p, "Home list for all worlds: ");
			else
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOMELIST_FOR_WORLD,
						"world", world);
//				util.sendMessage(p, "Home list on world \""+world+"\": ");
			
			for(org.morganm.homespawnplus.entity.Home home : homes) {
				String name = home.getName();
				if( name == null )
					name = "<noname>";
				util.sendMessage(p, name+": "+ util.shortLocationString(home)
						+ (home.isDefaultHome()
								? " ("+util.getLocalizedMessage(HSPMessages.GENERIC_DEFAULT)+")"
								: ""));
				/*
				util.sendMessage(p, String.format("%-16s %12s/%6d/%6d/%6d %-8s",
						home.getName(),
						home.getWorld().trim(),
						(int) home.getX(), (int) home.getY(), (int) home.getZ(),
						home.isDefaultHome() ? "yes" : "no"
					));
					*/
			}
		}
		else
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMELIST_NO_HOMES_FOUND,
					"world", world);
//			util.sendMessage(p, "No homes found for world \""+world+"\"");

		return true;
	}

}
