/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.storage.Storage;

/**
 * @author morganm
 *
 */
public class SpawnList extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"spawnl", "listspawns"}; }

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(final Player p, final Command command, final String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		String world = "all";
		if( args.length > 0 )
			world = args[0];
		
		final Set<org.morganm.homespawnplus.entity.Spawn> spawns = plugin.getStorage().getAllSpawns();
		
		boolean displayedSpawn = false;
		if( spawns != null && spawns.size() > 0 ) {
			if( world.equals("all") || world.equals("*") ) {
				world = "all";
				util.sendMessage(p, "Spawn list for all worlds: ");
			}
			else
				util.sendMessage(p, "Spawn list on world \""+world+"\": ");
			
			for(org.morganm.homespawnplus.entity.Spawn spawn : spawns) {
				if( !world.equals("all") && !world.equals(spawn.getWorld()) )
					continue;
					
				displayedSpawn = true;
				
				String group = spawn.getGroup();
				if( Storage.HSP_WORLD_SPAWN_GROUP.equals(group) )
					group = null;
				String name = spawn.getName();
				
				p.sendMessage(ChatColor.YELLOW + "id: " + ChatColor.RED + spawn.getId() + " " + ChatColor.YELLOW
						+ (name != null ? "[name: " + ChatColor.RED + name + ChatColor.YELLOW + "] " : "")
						+ (group != null ? "[group: " + ChatColor.RED + group + ChatColor.YELLOW + "] " : "")
						+ util.shortLocationString(spawn)
						+ (Storage.HSP_WORLD_SPAWN_GROUP.equals(spawn.getGroup()) ? ChatColor.GREEN + " (world default)" : ""));
			}
		}
		
		if( !displayedSpawn )
			util.sendMessage(p, "No spawns found for world \""+world+"\"");

		return true;
	}

}
