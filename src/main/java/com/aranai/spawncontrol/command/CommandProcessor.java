/**
 * 
 */
package com.aranai.spawncontrol.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aranai.spawncontrol.SpawnControl;

/**
 * @author morganm
 *
 */
public class CommandProcessor
{
	private final SpawnControl plugin;
	private final HashMap<String, Command> cmdHash;
	
	public CommandProcessor(SpawnControl plugin) {
		this.plugin = plugin;
		cmdHash = new HashMap<String, Command>();
		
		// initialize commands and add them to our command map
		List<Command> commands = getCommands();
		for(Command cmd : commands)
			addCommand(cmd);
	}
	
	/** Add a command to the command processor.  If the command is disabled, it will be
	 * ignored.
	 * 
	 * @param cmd
	 */
	public void addCommand(Command cmd) {
		SpawnControl.log.info(SpawnControl.logPrefix + " attempting to add command " + cmd.getCommandName());
		cmd.setPlugin(plugin);
		
		// skip commands that are disabled in the config
		if( !cmd.isEnabled() )
			return;
		
		cmdHash.put(cmd.getCommandName(), cmd);
		SpawnControl.log.info(SpawnControl.logPrefix + " added command " + cmd.getCommandName());
		
		String[] aliases = cmd.getCommandAliases();
		if( aliases != null )
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
//		SpawnControl.log.info(SpawnControl.logPrefix + " found cmd "+cmd);
		if( cmd != null )
			return cmd.execute(p, bukkitCommand, args);
		else
			return false;
	}
	
	   public Set<URL> getUrlsForName(String name) {
	        try {
	            final Set<URL> result = new HashSet<URL>();

	            String resourceName = name.replace(".", "/");
	            final Enumeration<URL> urls = plugin.getClassLoader().getResources(resourceName);
	            while (urls.hasMoreElements()) {
	                URL url = urls.nextElement();
	                result.add(new URL(url.toExternalForm().substring(0, url.toExternalForm().lastIndexOf(resourceName))));
	            }

	            return result;
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	    }
	   
	/** Find all Command objects and return them.  This works by looking for a file on the classpath
	 * which has all the command object list. To keep this automated, there is an ant build script
	 * that automatically finds all classes in the ".commands" package and builds the file to then be
	 * found at runtime later by this method.
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List<Command> getCommands() {
		List<Command> cmds = new ArrayList<Command>();

		ClassLoader loader = plugin.getClassLoader();
		InputStream is = loader.getResourceAsStream("META-INF/commandlist");
		
		if( is == null )
			throw new NullPointerException("Could not get commandlist resource");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			while( (line=reader.readLine()) != null ) {
				try {
					Class c = Class.forName(line, true, loader);
//					SpawnControl.log.info(SpawnControl.logPrefix + " Trying command class " +c);

					// check if class extends the BaseCommand class, which implements the Command interface
					Class superClass = c.getSuperclass();
					if( BaseCommand.class.equals(superClass) ) {
						Command cmd = (Command) c.newInstance();
//						SpawnControl.log.info(SpawnControl.logPrefix + " Adding command class "+cmd);
						cmds.add(cmd);
					}
					// No BaseCommand super, check to see if class implements Command interface directly
					else {
						Class[] interfaces = c.getInterfaces();
						for(Class i : interfaces) {
							if( i.equals(Command.class) ) {
								Command cmd = (Command) c.newInstance();
//								SpawnControl.log.info(SpawnControl.logPrefix + " Adding command class "+cmd);
								cmds.add(cmd);
							}
						}
					}
					
				}
				catch(Exception e) {
					SpawnControl.log.severe(SpawnControl.logPrefix + " error trying to load command object "+line);
					e.printStackTrace();
				}
			}
		}
		catch(Exception e) {
			SpawnControl.log.severe(SpawnControl.logPrefix + " error trying to load command objects");
			e.printStackTrace();
		}
		
		return cmds;
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
