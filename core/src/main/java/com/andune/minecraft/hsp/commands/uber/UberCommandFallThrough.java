/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
/**
 *
 */
package com.andune.minecraft.hsp.commands.uber;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.hsp.server.api.Command;

import java.util.Map;

/**
 * Uber commands can have a "fall through" command. When they do, they should
 * implement this interface to adhere to the fall through contract.
 * <p/>
 * This is what enabled an Uber Command like "/home" to fall through to the
 * actual home command if the player types "/home myNamedHome". However, if the
 * arguments the player types don't match any Uber subCommand or any
 * fall-through arguments (say they type /home blahblah and they don't have a
 * home named "blahblah"), then the Uber Command knows to print the Uber Command
 * help syntax.
 *
 * @author andune
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
     *         <br>
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

    /**
     * Help string for the Explicit SubCommand to be displayed in uber command
     * help. If null, the default uber help text for the command will be
     * displayed.
     *
     * @return
     */
    public String getExplicitSubCommandHelp();

    /**
     * Any additional help items to be shown by this command as a map, whose
     * keys are the sub arguments and values is the help text. Null is a valid
     * value.<br>
     * <br>
     * For example, for command /hsp, this should return a mapping with values
     * such as: "backup" => "backup database to a file".
     *
     * @return
     */
    public Map<String, String> getAdditionalHelp();

    /**
     * For the purposes of displaying an uber command help menu, this can
     * optionally can define aliases for sub commands. Can be null.
     *
     * @return
     */
    public Map<String, String> getAdditionalHelpAliases();
}
