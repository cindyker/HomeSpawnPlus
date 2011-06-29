/**
 * 
 */
package com.aranai.spawncontrol.command;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aranai.spawncontrol.SpawnControl;
import com.aranai.spawncontrol.commands.Home;
import com.aranai.spawncontrol.commands.SetHome;
import com.aranai.spawncontrol.commands.SetSpawn;
import com.aranai.spawncontrol.commands.Spawn;

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
		Command[] commands = {new Home(), new Spawn(), new SetHome(), new SetSpawn()};;
//		List<Command> commands = getCommands();
		for(Command cmd : commands)
			addCommand(cmd);
	}
	
	/** Automatically find all Command objects and return them.
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List<Command> getCommands() {
		List<Command> cmds = new ArrayList<Command>();
		
		try {
			Class[] classes = getClasses("com.aranai.spawncontrol.commands");
			
			for(Class c : classes) {
				SpawnControl.log.info(SpawnControl.logPrefix + " Trying command class " +c);
				Class[] interfaces = c.getInterfaces();
				for(Class i : interfaces) {
					if( i.equals(Command.class) ) {
						Command cmd = (Command) i.newInstance();
						SpawnControl.log.info(SpawnControl.logPrefix + " Adding command class "+cmd);
						cmds.add(cmd);
					}
				}
			}
		}
		catch(Exception e) {
			SpawnControl.log.severe(SpawnControl.logPrefix + " error trying to load command objects");
			e.printStackTrace();
		}
		
		return cmds;
	}
	
   /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @SuppressWarnings("rawtypes")
	private static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
			SpawnControl.log.severe(SpawnControl.logPrefix + " processing resource "+resource);
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }
	
    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("rawtypes")
	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
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
		SpawnControl.log.info(SpawnControl.logPrefix + " found cmd "+cmd);
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
