/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;

/**
 * @author morganm
 *
 */
public class SetMapSpawn extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"sms"}; }

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

		final World world = p.getWorld();
		final Location l = p.getLocation();
		world.setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		util.sendLocalizedMessage(p, HSPMessages.CMD_SETMAPSPAWN_SET_SUCCESS,
				"world", world.getName(), "location", util.shortLocationString(l));

		return true;
	}

}
