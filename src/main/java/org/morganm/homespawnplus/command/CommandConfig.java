/**
 * 
 */
package org.morganm.homespawnplus.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.morganm.homespawnplus.util.Logger;

/** This class is used to store state of commands configuration. This amounts to three
 * key pieces of data: any disabled commands, all custom defined commands and all
 * properties related to any custom defined commands.
 * 
 * @author morganm
 *
 */
public class CommandConfig {
	private Logger log;
	private Set<String> disabledCommands;
	private Map<String, Map<String, Object>> commandParams;
	
	public CommandConfig(Logger log) {
		if( log == null )
			throw new NullPointerException("log is null");

		this.log = log;
		emptyDefaults();
	}
	
	/** Setup empty default variables that do nothing (but avoid NPEs).
	 */
	private void emptyDefaults() {
		disabledCommands = new HashSet<String>();
		commandParams = new HashMap<String, Map<String, Object>>();
	}
	
	public boolean isDisabledCommand(String command) {
		if( disabledCommands.contains("*") )
			return true;
		else
			return disabledCommands.contains(command.toLowerCase());
	}
	
	public Set<String> getDisabledCommands() {
		return disabledCommands;
	}
	
	/** Return a list of all commands that have been defined and have
	 * command parameters.
	 * 
	 * @return
	 */
	public Set<String> getDefinedCommands() {
		return commandParams.keySet();
	}
	
	/** Return command parameters for a specific command.
	 * 
	 * @return guaranteed to not return null
	 */
	public Map<String, Object> getCommandParameters(String command) {
		Map<String, Object> ret = commandParams.get(command);
		
		// If null, create empty map and save it for future use 
		if( ret == null ) {
			ret = new HashMap<String, Object>();
			commandParams.put(command, ret);
		}
		
		return ret;
	}
	
	/** This method does the heavy lifting of processing a configuration to load
	 * the configuration state. An example config state:
	 * 
	 * commands:
	 *   disabledCommands: [home, sethome]
	 *   randomspawn:
	 *     class: CustomEventCommand
	 *     event: randomspawn
	 * 
	 * @param section
	 */
	public void loadConfig(ConfigurationSection section) {
		if( section == null ) {
			emptyDefaults();
			return;
		}
		
		disabledCommands = new HashSet<String>();
		List<String> theList = section.getStringList("disabledCommands");
		if( theList != null ) {
			for(String s : theList) {
				disabledCommands.add(s.toLowerCase());
			}
		}
		else
		
		commandParams = new HashMap<String, Map<String, Object>>();
		Set<String> keys = section.getKeys(false);
		if( keys != null ) {
			for(String key : keys) {
				if( key.equals("disabledCommands") )
					continue;
				
				log.devDebug("loading config params for command ",key);
				ConfigurationSection cmdSection = section.getConfigurationSection(key);
				if( cmdSection == null ) {
					log.warn("no parameters defined for command ",key,", skipping");
					continue;
				}
				
				Set<String> parameters = cmdSection.getKeys(false);
				if( parameters == null || parameters.size() == 0 ) {
					log.warn("no parameters defined for command ",key,", skipping");
					continue;
				}
				
				HashMap<String, Object> paramMap = new HashMap<String, Object>();
				commandParams.put(key,  paramMap);
				for(String param : parameters) {
					final Object val = cmdSection.get(param);
					log.devDebug("command ",key,"; key=",param,", val=",val);
					paramMap.put(param, val);
				}
			}
		}
	}
}
