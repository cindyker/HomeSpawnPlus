/**
 * 
 */
package org.morganm.homespawnplus.command;

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
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;


/**
 * @author morganm
 *
 */
public class CommandProcessor
{
	private static final Logger log = HomeSpawnPlus.log;
	
	private final HomeSpawnPlus plugin;
	private final HashMap<String, Command> cmdHash;
	private final String logPrefix;
	
	public CommandProcessor(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.logPrefix = HomeSpawnPlus.logPrefix;
		
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
		cmd.setPlugin(plugin);
		
		// skip commands that are disabled in the config
		if( !cmd.isEnabled() )
			return;
		
		cmdHash.put(cmd.getCommandName(), cmd);
//		HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " added command " + cmd.getCommandName());
		
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
		ConsoleCommandSender console = null;

		if(sender instanceof Player)
			p = (Player) sender;
		else if(sender instanceof ConsoleCommandSender)
			console = (ConsoleCommandSender) sender;
		
		Command cmd = cmdHash.get(commandName);
//		HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " found cmd "+cmd);
		if( cmd != null ) {
			if( p != null )
				return cmd.execute(p, bukkitCommand, args);
			else if( console != null )
				return cmd.execute(console, bukkitCommand, args);
			else {	// of course, shouldn't ever happen
				log.severe(logPrefix+" Unknown message source: command not from Console or Player: "+sender.getClass()+", "+sender);
				return false;
			}
		}
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
//		InputStream is = loader.getResourceAsStream("META-INF/commandlist");
		InputStream is = plugin.getResource("META-INF/commandlist");
		
		if( is == null )
			throw new NullPointerException("Could not get commandlist resource");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			while( (line=reader.readLine()) != null ) {
				try {
					Class c = Class.forName(line, true, loader);
//					HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Trying command class " +c);

					// check if class extends the BaseCommand class, which implements the Command interface
					Class superClass = c.getSuperclass();
					if( BaseCommand.class.equals(superClass) ) {
						Command cmd = (Command) c.newInstance();
//						HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Adding command class "+cmd);
						cmds.add(cmd);
					}
					// No BaseCommand super, check to see if class implements Command interface directly
					else {
						Class[] interfaces = c.getInterfaces();
						for(Class i : interfaces) {
							if( i.equals(Command.class) ) {
								Command cmd = (Command) c.newInstance();
//								HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Adding command class "+cmd);
								cmds.add(cmd);
							}
						}
					}
					
				}
				catch(Exception e) {
					log.severe(logPrefix + " error trying to load command object "+line);
					e.printStackTrace();
				}
			}
		}
		catch(Exception e) {
			log.severe(logPrefix + " error trying to load command objects");
			e.printStackTrace();
		}
		
		return cmds;
	}
}
