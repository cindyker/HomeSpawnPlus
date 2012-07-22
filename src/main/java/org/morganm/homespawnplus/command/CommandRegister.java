/**
 * 
 */
package org.morganm.homespawnplus.command;

import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.util.Logger;

/** Class whose job is to register all of HSP commands with Bukkit dynamically,
 * as opposed to through static plugin.yml configurations. This is used by HSP
 * to give admins maximum flexibility in naming their commands however they like.
 * 
 * @author morganm
 *
 */
public class CommandRegister {
	private final HomeSpawnPlus plugin;
	private final Logger log;
	
	public CommandRegister(HomeSpawnPlus plugin) {
		this.plugin = plugin;
		this.log = plugin.getLog(); 
	}
	
	public void register(Command command) {
		command.setPlugin(plugin);
		
		CraftServer cs = (CraftServer) Bukkit.getServer();
		SimpleCommandMap commandMap = cs.getCommandMap();
		
		try {
			Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			constructor.setAccessible(true);
			PluginCommand pc = constructor.newInstance(command.getCommandName(), plugin);
			pc.setExecutor(command);
			commandMap.register("hsp", pc);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void registerAllCommands() {
		Class<?>[] classes = plugin.getJarUtils().getClasses("org.morganm.homespawnplus.commands");
		
		for(int i=0; i < classes.length; i++) {
//			log.debug("checking class "+classes[i]);
			try {
				Class<?> superClass = classes[i].getSuperclass();
				if( BaseCommand.class.equals(superClass) ) {
					Command cmd = (Command) classes[i].newInstance();
					register(cmd);
				}
				else {
					Class<?>[] interfaces = classes[i].getInterfaces();
					for(Class<?> iface : interfaces) {
						if( iface.equals(Command.class) ) {
							Command cmd = (Command) classes[i].newInstance();
							register(cmd);
						}
					}
				}
			}
			catch(Exception e) {
				log.severe("error trying to load command class "+classes[i]);
				e.printStackTrace();
			}
		}
	}
}
