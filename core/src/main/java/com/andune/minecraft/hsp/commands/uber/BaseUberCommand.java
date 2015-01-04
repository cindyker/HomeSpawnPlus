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

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.server.api.Command;
import com.andune.minecraft.hsp.server.api.Factory;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author andune
 */
public abstract class BaseUberCommand extends BaseCommand {
    private static final Logger log = LoggerFactory.getLogger(BaseUberCommand.class);
    private Reflections reflections;
    private Factory factory;
    private String baseName;
    private final Map<String, Command> subCommands = new HashMap<String, Command>(10);
    private final Map<String, Command> subCommandAliases = new HashMap<String, Command>(10);
    private final Map<String, String> shortestAlias = new HashMap<String, String>(10);
    private final UberCommandFallThrough baseFallThroughCommand;
    private List<String> sortedKeys;

    public BaseUberCommand(Factory factory, Reflections reflections, UberCommandFallThrough baseFallThroughCommand) {
        this.baseFallThroughCommand = baseFallThroughCommand;
        setDependencies(factory, reflections, getClass().getSimpleName().toLowerCase());
    }

    public BaseUberCommand(Factory factory, Reflections reflections) {
        this.baseFallThroughCommand = null;
        setDependencies(factory, reflections, getClass().getSimpleName().toLowerCase());
    }

    public BaseUberCommand(Factory factory, Reflections reflections, String baseName) {
        this.baseFallThroughCommand = null;
        setDependencies(factory, reflections, baseName);
    }

    private void setDependencies(Factory factory, Reflections reflections, String baseName) {
        this.factory = factory;
        this.reflections = reflections;
        this.baseName = baseName;

        loadSubCommands(baseName);
        log.debug("setDependencies() baseName={}", baseName);
    }

    /**
     * The BaseCommand override will return full help syntax.
     */
    @Override
    public String getUsage() {
        return getUsage(null);
    }

    private Map<String, String> getAdditionalHelp() {
        if (baseFallThroughCommand != null)
            return baseFallThroughCommand.getAdditionalHelp();
        else
            return null;
    }

    private Map<String, String> getAdditionalHelpAliases() {
        if (baseFallThroughCommand != null)
            return baseFallThroughCommand.getAdditionalHelpAliases();
        else
            return null;
    }

    /**
     * Return a simplified help that only shows commands that we have permission
     * to use.
     */
    public String getUsage(CommandSender sender) {
        Player p = null;
        if (sender instanceof Player)
            p = (Player) sender;

        final Map<String, String> additionalHelp = getAdditionalHelp();
        final Map<String, String> additionalHelpAliases = getAdditionalHelpAliases();
        if (sortedKeys == null) {
            HashSet<String> keySet = new HashSet<String>(subCommands.keySet());
            keySet.add("help");

            if (additionalHelp != null)
                keySet.addAll(additionalHelp.keySet());

            sortedKeys = new ArrayList<String>(keySet);
            Collections.sort(sortedKeys);
        }

        StringBuffer sb = new StringBuffer();
        sb.append("Subcommand help for /");
        sb.append(baseName);
        sb.append("\n");

        for (String key : sortedKeys) {
            if (key.equals("help")) {
                sb.append("  help: get help on this command or sub-commands\n");
            } else {
                Command cmd = subCommands.get(key);

                if (cmd != null) {
                    // don't show help for commands we don't have permission for
                    if (p != null && !cmd.hasPermission(p, false))
                        continue;

                    UberCommand annotation = cmd.getClass().getAnnotation(UberCommand.class);
                    String help = annotation.help();

                    // Allow a FallThrough command to define it's own help text if it chooses
                    if (cmd instanceof UberCommandFallThrough && !key.equals("")) {
                        UberCommandFallThrough ucft = (UberCommandFallThrough) cmd;
                        if (ucft.getExplicitSubCommandHelp() != null)
                            help = ucft.getExplicitSubCommandHelp();
                    }

                    if (key.equals("")) {
                        if (help.length() > 0) {
                            sb.append("  (no arg)");
                        } else {
                            // don't show a line if there's no help
                            continue;
                        }
                    } else {
                        sb.append("  ");
                        sb.append(key);
                    }

                    if (help.length() > 0) {
                        String shortAlias = shortestAlias.get(key);
                        if (shortAlias != null) {
                            sb.append(" [");
                            sb.append(shortAlias);
                            sb.append("]");
                        }
                        sb.append(": ");
                        sb.append(help);
                    }
                    sb.append("\n");
                }
                // must be an additional command item
                else {
                    appendAdditionalHelp(sb, additionalHelp, additionalHelpAliases, key);
                }
            }
        }

        // drop last \n, server adds it's own
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Return true if this Uber Command requires a player argument as the
     * first arg when run from the console. Default is true but can be
     * overridden by a subclass that desired different behavior.
     *
     * @return
     */
    protected boolean requiresPlayerArgumentFromConsole() {
        return true;
    }

    private boolean appendAdditionalHelp(StringBuffer sb, Map<String, String> additionalHelp,
                                         Map<String, String> additionalHelpAliases, String key) {
        if (additionalHelp != null && additionalHelp.get(key) != null) {
            sb.append("  ");
            sb.append(key);
            if (additionalHelpAliases != null && additionalHelpAliases.get(key) != null) {
                sb.append(" [");
                sb.append(additionalHelpAliases.get(key));
                sb.append("]");
            }
            sb.append(": ");
            sb.append(additionalHelp.get(key));
            sb.append("\n");

            return true;
        }

        return false;
    }

    /**
     * This method signature is invoked from the command processing routines directly and
     * has the job of determining whether the command is coming from the console or not.
     *
     * If coming from the console, some special processing is done to allow admins to
     * run commands as a user, which can also be useful for command blocks.
     *
     * If not coming from the console, control passes through to normal command processing.
     *
     * @param sender the sender object (usually a Player or Console)
     * @param cmd    the actual command that was run (if a command supports aliases, this will
     *               return the exact alias that was used)
     * @param args   any arguments passed to the command
     * @return
     */
    @Override
    public boolean execute(CommandSender sender, String cmd, String[] args) {
        if (sender instanceof Player || !requiresPlayerArgumentFromConsole()) {
            return this.executePrivate(sender, cmd, args);
        }
        else {
            if (args.length < 1) {
                sender.sendMessage("From the console, command /" + cmd + " requires the first argument to be the player to run as");
                return true;
            }

            NewArgs newArgs = extractPlayerArgument(cmd, args);
            return super.runPlayerCommandFromConsole(sender, newArgs);
        }
    }

    private boolean executePrivate(CommandSender sender, String label, String[] args) {
        Command command = null;

        if (args.length < 1) {
            command = subCommands.get("");
        }
        else if (args[0].equalsIgnoreCase("help")) {
            if (args.length > 1) {
                command = subCommands.get(args[1]);
                if (command == null)
                    command = subCommandAliases.get(args[1]);
            }

            String usage = null;
            if (command != null) {
                usage = command.getUsage();
                usage = usage.replaceAll("/<command>", "/" + baseName + " " + args[1]);
            }
            else
                usage = getUsage(sender);

            // if there is a baseFallThroughCommand, check if we should run that
            if (baseFallThroughCommand != null && baseFallThroughCommand.processUberCommandDryRun(sender, label, args)) {
                return baseFallThroughCommand.execute(sender, label, args);
            }

            sender.sendMessage(usage);
            return true;
        }
        else {
            command = subCommands.get(args[0]);
            if (command == null)
                command = subCommandAliases.get(args[0]);
        }

        if (command != null) {
            String[] newArgs = null;
            if (args.length > 0)
                newArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
            else
                newArgs = args;

            if (command.execute(sender, label, newArgs)) {
                return true;
            }
            // print out the sub-command usage if there was an argument given
            else if (args.length > 0) {
                String usage = command.getUsage();
                usage = usage.replaceAll("/<command>", "/" + label + " " + args[0]);
                sender.sendMessage(usage);
                return true;
            }
            // otherwise print out uber-command help syntax
            else
                return false;
        }
        // if there is a baseFallThroughCommand, check if we should run that
        else if (baseFallThroughCommand != null && baseFallThroughCommand.processUberCommandDryRun(sender, label, args)) {
            return baseFallThroughCommand.execute(sender, label, args);
        }

        sender.sendMessage(getUsage());
        return true;
    }

    protected final void loadSubCommands(final String uberCommand) {
        log.debug("loadSubCommands uberCommand={}", uberCommand);

        Set<Class<?>> uberSubs = reflections.getTypesAnnotatedWith(UberCommand.class);
        for (Class<?> sub : uberSubs) {
            UberCommand annotation = sub.getAnnotation(UberCommand.class);
            if (annotation == null) {
                log.error("UberCommand annotation is null for sub {}", sub);
                continue;
            }

            if (uberCommand.equalsIgnoreCase(annotation.uberCommand())) {
                if (Command.class.isAssignableFrom(sub)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Command> commandClass = (Class<? extends Command>) sub;
                    final Command command = factory.newCommand(commandClass);
                    subCommands.put(annotation.subCommand(), command);

                    // add an all lowercase alias for whatever the subCommand is
                    String lowerAlias = annotation.subCommand().toLowerCase();
                    if (lowerAlias.equals(annotation.subCommand()))
                        lowerAlias = null;

                    if (lowerAlias != null)
                        subCommandAliases.put(lowerAlias, command);

                    String shortAlias = null;
                    String[] aliases = annotation.aliases();
                    if (aliases != null) {
                        shortAlias = getShortestAlias(aliases);
                        for (String alias : aliases) {
                            subCommandAliases.put(alias, command);
                        }
                    }

                    // if there's a short alias <= 3 characters, store it for
                    // future use when displaying help
                    if (shortAlias != null && shortAlias.length() <= 3)
                        this.shortestAlias.put(annotation.subCommand(), shortAlias);
                } else
                    log.error("Class {} has UberCommand annotation but is not subClass of Command", sub);
            }
        }

        if (baseFallThroughCommand != null) {
            String[] names = baseFallThroughCommand.getExplicitSubCommandName();
            if (names != null && names.length > 0) {
                final String cmdName = names[0];
                subCommands.put(cmdName, baseFallThroughCommand);

                String shortAlias = getShortestAlias(names);
                if (cmdName.equals(shortAlias))
                    shortAlias = null;
                if (shortAlias != null)
                    this.shortestAlias.put(cmdName, shortAlias);

                // add aliases to alias map
                for (int i = 1; i < names.length; i++) {
                    subCommandAliases.put(names[i], baseFallThroughCommand);
                }
            }
        }
    }

    /**
     * Given a list of aliases, return the shortest one.
     *
     * @param aliases
     * @return
     */
    private String getShortestAlias(final String[] aliases) {
        String shortAlias = null;
        if (aliases != null) {
            for (String alias : aliases) {
                if (shortAlias == null)
                    shortAlias = alias;
                else if (alias.length() < shortAlias.length())
                    shortAlias = alias;
            }
        }
        return shortAlias;
    }
}
