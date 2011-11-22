/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;

/**
 * @author morganm
 *
 */
public class ListHomes extends BaseCommand {

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		Set<org.morganm.homespawnplus.entity.Home> homes;
		String world = p.getWorld().getName();
		
		if( args.length > 1 )
			world = args[0];
		
		homes = plugin.getStorage().getHomes(world, p.getName());
		
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
			util.sendMessage(p, "Home list on world \""+world+"\": ");
			for(org.morganm.homespawnplus.entity.Home home : homes) {
				String name = home.getName();
				if( name == null )
					name = "<noname>";
				util.sendMessage(p, name+": "+ util.shortLocationString(home.getLocation())
						+ (home.isDefaultHome() ? " (default)" : ""));
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
			util.sendMessage(p, "No homes found for world \""+world+"\"");

		return true;
	}

}
