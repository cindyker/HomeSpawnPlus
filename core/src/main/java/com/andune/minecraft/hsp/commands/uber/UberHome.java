/**
 * 
 */
package com.andune.minecraft.hsp.commands.uber;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.reflections.Reflections;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.server.api.Command;
import com.andune.minecraft.hsp.server.api.Factory;

/**
 * @author andune
 *
 */
public class UberHome extends BaseCommand {
    private final Reflections reflections;
    private final Factory factory;
    private final Map<String, Command> subCommands = new HashMap<String, Command>(10);
    private final Map<String, Command> subCommandAliases = new HashMap<String, Command>(10);

    @Inject
    public UberHome(Factory factory, Reflections reflections) {
        this.factory = factory;
        this.reflections = reflections;
        
//        loadSubCommands(getClass().getSimpleName().toLowerCase());
        loadSubCommands("home");
    }

    @Override
    public String getUsage() {
        // TODO: some auto-generated uber help
        return "Some help text";
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Command command = null;
        
        if( args.length < 1 ) {
            command = subCommands.get("");
        }
        else if( args[0].equalsIgnoreCase("help") ) {
            sender.sendMessage(getUsage());
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
    
    private void loadSubCommands(final String uberCommand) {
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
                    
                    String[] aliases = annotation.aliases();
                    if( aliases != null ) {
                        for(String alias : aliases) {
                            subCommandAliases.put(alias, command);
                        }
                    }
                }
                else
                    log.error("Class {} has UberCommand annotation but is not subClass of Command", sub);
            }
        }
    }
}
