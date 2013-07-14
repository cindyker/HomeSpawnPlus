/**
 * 
 */
package com.andune.minecraft.hsp.commands.uber;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.hsp.server.api.Command;

/**
 * Uber commands can have a "fall through" command. When they do, they should
 * implement this interface to adhere to the fall through contract.
 * 
 * This is what enabled an Uber Command like "/home" to fall through to the
 * actual home command if the player types "/home myNamedHome". However, if the
 * arguments the player types don't match any Uber subCommand or any
 * fall-through arguments (say they type /home blahblah and they don't have a
 * home named "blahblah"), then the Uber Command knows to print the Uber Command
 * help syntax.
 * 
 * @author andune
 * 
 */
public interface UberCommandFallThrough extends Command {
	/**
	 * Process a given command context and determine whether or not the sub
	 * command would have done anything with it or if it would have just
	 * returned a usage or false value.
	 * 
	 * @param sender
	 * @param label
	 * @param args
	 * @return true if the sub command will process the command context and do
	 *         something with it.<br>
	 * 		   <br>
	 *         false if the sub command will just print out a usage or return
	 *         false for normal command processing
	 */
	public boolean processUberCommandDryRun(CommandSender sender, String label, String[] args);
	
	/**
	 * Return the explicit sub-command name to force a command to go to this sub
	 * command. For example, for Command /home can set this to "named" so that
	 * the uberCommand home will always pass "/home named xyz" to the subCommand
	 * (received as if "/home xyz" was typed). Can be null.
	 * 
	 * @return The explicit subCommand name. The first argument will be taken as
	 *         the name, any additional arguments (if any) will be taken as
	 *         aliases.
	 */
	public String[] getExplicitSubCommandName();
}
