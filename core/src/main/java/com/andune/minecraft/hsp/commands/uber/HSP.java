/**
 * 
 */
package com.andune.minecraft.hsp.commands.uber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.reflections.Reflections;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.server.api.Factory;

/**
 * @author andune
 *
 */
public class HSP extends BaseUberCommand {
	@Inject com.andune.minecraft.hsp.commands.HSP hspCommand;
	
	private Map<String, String> hspCommandHelp = null;
	
    @Inject
    public HSP(Factory factory, Reflections reflections) {
        super(factory, reflections);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
    	// all /hsp uber commands are admin functions and are admin-only
        if( !permissions.isAdmin(sender) )
			return false;
    	
        if( args.length > 0 && hspCommand.findMatchingCommand(args[0]) != null ) {
        	return hspCommand.execute(sender, label, args);
        }
        else {
        	return super.execute(sender, label, args);
        }
    }
    
    @Override
    protected Map<String, String> getAdditionalHelp() {
    	if( hspCommandHelp != null )
    		return hspCommandHelp;
    	
    	hspCommandHelp = new HashMap<String, String>();
    	List<String> subCommands = hspCommand.getSubCommandNames();
    	for(String cmdName : subCommands) {
    		String help = server.getLocalizedMessage(HSPMessages.CMD_HSP_UBER_USAGE + "_" + cmdName.toUpperCase());
    		if( help != null )
    			hspCommandHelp.put(cmdName, help);
    		else
    			hspCommandHelp.put(cmdName, "(no additional help available)");
    	}
    	
    	return hspCommandHelp;
    }

    protected Map<String, String> getAdditionalHelpAliases() {
    	return hspCommand.getSubCommandAliases();
    }
}
