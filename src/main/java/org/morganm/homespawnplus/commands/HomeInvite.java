/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.util.General;

/**
 * @author morganm
 *
 */
public class HomeInvite extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"hi"}; }

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		if( args.length < 1 ) {
			p.sendMessage(command.getUsage());
			return true;
		}
		
		String invitee = args[0];
		if( args.length > 1 ) {
			StringBuffer lengthOfTime = new StringBuffer();
			for(int i=1; i < args.length; i++) {
				if( lengthOfTime.length() > 0 )
					lengthOfTime.append(" ");
				lengthOfTime.append(args[i]);
			}
			long timeInMilliseconds = General.getInstance().parseTimeInput(lengthOfTime.toString()); 
		}
		
		return true;
	}

}
