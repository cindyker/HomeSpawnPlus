/**
 * 
 */
package com.andune.minecraft.hsp.commands.uber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.server.api.Command;
import com.andune.minecraft.hsp.server.api.Factory;

/**
 * @author andune
 *
 */
public abstract class BaseUberCommand extends BaseCommand {
    private static final Logger log = LoggerFactory.getLogger(BaseUberCommand.class);
    private Reflections reflections;
    private Factory factory;
    protected String baseName;
    private final Map<String, Command> subCommands = new HashMap<String, Command>(10);
    private final Map<String, Command> subCommandAliases = new HashMap<String, Command>(10);
    private final Map<String, String> shortestAlias = new HashMap<String, String>(10);
    private ArrayList<String> sortedKeys;

    public BaseUberCommand(Factory factory, Reflections reflections) {
        setDependencies(factory, reflections, getClass().getSimpleName().toLowerCase());
    }
    public BaseUberCommand(Factory factory, Reflections reflections, String baseName) {
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

    /**
     * Return a simplified help that only shows commands that we have
     * permission to use.
     * 
     */
    public String getUsage(CommandSender sender) {
        Player p = null;
        if( sender instanceof Player )
            p = (Player) sender;

        if( sortedKeys == null ) {
            sortedKeys = new ArrayList<String>(subCommands.keySet());
            sortedKeys.add("help");
            Collections.sort(sortedKeys);
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append("Subcommand help for /"+baseName+"\n");
        for(String key: sortedKeys) {
            if( key.equals("help") ) {
                sb.append("  help: get help on this command or sub-commands\n");
            }
            else {
                Command cmd = subCommands.get(key);
                // don't show help for commands we don't have permission for
                if( p != null && !cmd.hasPermission(p, false) )
                    continue;
                
                UberCommand annotation = cmd.getClass().getAnnotation(UberCommand.class);
                String help = annotation.help();

                if( key.equals("")  ) {
                    if( help.length() > 0 )
                        sb.append("  (no arg)");
                    else
                        continue;   // don't show a line if there's no help
                }
                else {
                    sb.append("  ");
                    sb.append(key);
                }
            
                if( help.length() > 0 ) {
                    String shortAlias = shortestAlias.get(key);
                    if( shortAlias != null ) {
                        sb.append(" [");
                        sb.append(shortAlias);
                        sb.append("]");
                    }
                    sb.append(": ");
                    sb.append(help);
                }
                sb.append("\n");
            }
        }

        // drop last \n, server adds it's own
        return sb.substring(0, sb.length()-1);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Command command = null;
        
        if( args.length < 1 ) {
            command = subCommands.get("");
        }
        else if( args[0].equalsIgnoreCase("help") ) {
            if( args.length > 1 ) {
                command = subCommands.get(args[1]);
                if( command == null )
                    command = subCommandAliases.get(args[1]);
            }

            String usage = null;
            if( command != null ) {
                usage = command.getUsage();
                usage = usage.replaceAll("/<command>", "/"+baseName+" "+args[1]);
            }
            else
                usage = getUsage(sender);
            
            sender.sendMessage(usage);
            return true;
        }
        else {
            command = subCommands.get(args[0]);
            if( command == null )
                command = subCommandAliases.get(args[0]);
        }
        
        if( command != null ) {
            String[] newArgs = null;
            if( args.length > 0 )
                newArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
            else
                newArgs = args;

            return command.execute(sender, label, newArgs);
        }
        else {
            sender.sendMessage(getUsage());
            return true; 
        }
    }
    
    protected void loadSubCommands(final String uberCommand) {
        log.debug("loadSubCommands uberCommand={}", uberCommand);
        
        Set<Class<?>> uberSubs = reflections.getTypesAnnotatedWith(UberCommand.class);
        for(Class<?> sub : uberSubs) {
            UberCommand annotation = sub.getAnnotation(UberCommand.class);
            if( annotation == null ) {
                log.error("UberCommand annotation is null for sub {}", sub);
                continue;
            }
            
            if( uberCommand.equalsIgnoreCase(annotation.uberCommand()) ) {
                if( Command.class.isAssignableFrom(sub) ) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Command> commandClass = (Class<? extends Command>) sub;
                    final Command command = factory.newCommand(commandClass);
                    subCommands.put(annotation.subCommand(), command);
                    
                    // add an all lowercase alias for whatever the subCommand is
                    String lowerAlias = annotation.subCommand().toLowerCase();
                    if( lowerAlias.equals(annotation.subCommand()) )
                        lowerAlias = null;
                    
                    if( lowerAlias != null )
                        subCommandAliases.put(lowerAlias, command);
                    
                    String shortAlias = null;
                    String[] aliases = annotation.aliases();
                    if( aliases != null ) {
                        for(String alias : aliases) {
                            subCommandAliases.put(alias, command);
                            if( shortAlias == null )
                                shortAlias = alias;
                            else if( alias.length() < shortAlias.length() )
                                shortAlias = alias;
                        }
                    }
                    
                    // if there's a short alias <= 3 characters, store it for
                    // future use when displaying help
                    if( shortAlias != null && shortAlias.length() <= 3 )
                        this.shortestAlias.put(annotation.subCommand(), shortAlias);
                }
                else
                    log.error("Class {} has UberCommand annotation but is not subClass of Command", sub);
            }
        }
    }
}
