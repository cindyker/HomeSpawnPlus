/**
 * 
 */
package com.aranai.spawncontrol.command;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aranai.spawncontrol.SpawnControl;
import com.aranai.spawncontrol.commands.Home;
import com.aranai.spawncontrol.commands.Spawn;

/**
 * @author morganm
 *
 */
public class CommandProcessor
{
	private final HashMap<String, Command> cmdHash;
	
	public CommandProcessor(SpawnControl plugin) {
		cmdHash = new HashMap<String, Command>();
		
		// initialize commands and add them to our command map
		Command[] commands = {new Home(), new Spawn()};;
		for(Command cmd : commands)
			addCommand(cmd);
	}
	
	/** Add a command to the command processor.  If the command is disabled, it will be
	 * ignored.
	 * 
	 * @param cmd
	 */
	public void addCommand(Command cmd) {
		// skip commands that are disabled in the config
		if( !cmd.isEnabled() )
			return;
		
		cmdHash.put(cmd.getCommandName(), cmd);
		
		String[] aliases = cmd.getCommandAliases();
		for(String alias : aliases)
			cmdHash.put(alias, cmd);
	}
	
	/** Remove a command from the command processor.  Useful for live-disabling functionality.
	 * 
	 * @param cmd
	 */
	public void removeCommand(String commandName) {
		String activeCommandKey = null;
		
		for(Entry<String, Command> e : cmdHash.entrySet()) {
			Command cmd = e.getValue();
			if( cmd.getCommandName().equals(commandName) ) {
				activeCommandKey = e.getKey();
				break;
			}
		}
		
		if( activeCommandKey != null )
			cmdHash.remove(activeCommandKey);
	}
	
	/** Find a command in our command hash and execute it.
	 * 
	 * @param sender
	 * @param bukkitCommand
	 * @param commandLabel
	 * @param args
	 * @return
	 */
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command bukkitCommand, String commandLabel, String[] args)
	{
		String commandName = bukkitCommand.getName().toLowerCase();
		Player p = null;

		if(sender instanceof Player)
			p = (Player) sender;
		
		// this would represent commands from the console. Right now we don't support any commands from the console.
		if( p == null )
			return false;
		
		Command cmd = cmdHash.get(commandName);
		if( cmd != null )
			return cmd.execute(p, bukkitCommand, args);
		else
			return false;
	}
	
	

	/*
	public boolean defunct()
	{

		// Set setting
		if(commandName.equals("sc_config") && this.canUseScConfig(p))
		{
			if(args.length < 2)
			{
				// Command format is wrong
				p.sendMessage("Command format: /sc_config [setting] [value]");
			}
			else
			{
				// Verify setting
				if(!SpawnControl.validSettings.contains(args[0]))
				{
					// Bad setting key
					p.sendMessage("Unknown configuration value.");
				}
				else
				{
					// Parse value
					try
					{
						int tmpval = Integer.parseInt(args[1]);

						if(tmpval < 0)
						{
							p.sendMessage("Value must be >= 0.");
						}
						else
						{
							// Save
							if(!plugin.setSetting(args[0], tmpval, p.getName()))
							{
								p.sendMessage("Could not save value for '"+args[0]+"'!");
							}
							else
							{
								p.sendMessage("Saved value for '"+args[0]+"'.");
							}
						}
					}
					catch(Exception ex)
					{
						// Bad number
						p.sendMessage("Couldn't read value.");
					}
				}
			}
			return true;
		}
    	
    	// Import config
    	if(commandName.equals("scimportconfig") && p.isOp())
    	{
    		SpawnControl.log.info("[SpawnControl] Attempting to import player configuration file.");
    		plugin.importConfig();
    		return true;
    	}
    	
    	// Import group config
    	if(commandName.equals("scimportgroupconfig") && p.isOp())
    	{
    		SpawnControl.log.info("[SpawnControl] Attempting to import group configuration file.");
    		plugin.importGroupConfig();
    		return true;
    	}
    	
    	return true;
    }
    */
	
}
