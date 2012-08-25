/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;

/** Command to return the group HSP thinks a player is in, based on the underlying
 * Permission system in use.
 * 
 * @author morganm
 *
 */
public class GroupQuery extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"gq"}; }
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		String playerName = null;
		String playerWorld = null;
		boolean playerOffline = false;
		
		if( args.length > 0 ) {
			Player p = plugin.getServer().getPlayer(args[0]);
			if( p != null ) {
				playerName = p.getName();
				playerWorld = p.getWorld().getName();
			}
			else {
				// look for an OfflinePlayer by that name
				OfflinePlayer offline = plugin.getServer().getOfflinePlayer(args[0]);
				if( offline.hasPlayedBefore() ) {
					playerOffline = true;
					playerName = offline.getName();
					
					if( args.length > 1 )
						playerWorld = args[1];
					else {
						// no way to get the world of an offline player, so we have
						// to just assume the default world
						playerWorld = util.getDefaultWorld();
					}
				}
				
				// didn't find any player by that name, error out
				if( playerName == null ) {
					util.sendMessage(sender, "Player "+args[0]+" not found.");
					return true;
				}
			}
		}
		else if( sender instanceof Player ) {
			Player p = (Player) sender;
			playerName = p.getName();
			playerWorld = p.getWorld().getName();
		}
		
		if( playerName == null )
			return false;

		String group = plugin.getPlayerGroup(playerWorld, playerName);
		util.sendMessage(sender, "Player "+playerName+" is in group \""+group+"\" on "+playerWorld
				+ (playerOffline ? " [player offline]" : "")
				+ " (using perms "+plugin.getPermissionSystem().getSystemInUseString()+")");
		return true;
	}
}
