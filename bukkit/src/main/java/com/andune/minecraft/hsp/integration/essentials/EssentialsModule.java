/**
 * 
 */
package com.andune.minecraft.hsp.integration.essentials;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Scheduler;
import com.andune.minecraft.hsp.server.bukkit.command.BukkitCommandRegister;
import com.earth2me.essentials.AlternativeCommandsHandler;
import com.earth2me.essentials.Essentials;

/**
 * @author andune
 *
 */
@Singleton
public class EssentialsModule implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(EssentialsModule.class);
    
    private Plugin essentialsPlugin;
    private Map<String, List<PluginCommand>> altcommands;
    private final Plugin plugin;
    private final BukkitCommandRegister bukkitCommandRegister;
    private final Scheduler scheduler;
    
    @Inject
    public EssentialsModule(Plugin bukkitPlugin, BukkitCommandRegister bukkitCommandRegister,
            Scheduler scheduler) {
        this.plugin = bukkitPlugin;
        this.bukkitCommandRegister = bukkitCommandRegister;
        this.scheduler = scheduler;
    }
    
    /**
     * Called to register HSP's commands with Essentials, which then enables
     * Essentials "respectful" command usurp behavior which will let HSP own
     * commands like "/home" and "/spawn".
     */
    private void registerCommands() {
        log.debug("entering registerCommands()");
        
        essentialsPlugin = plugin.getServer().getPluginManager().getPlugin("Essentials");
        
        if( essentialsPlugin == null ) {
            log.debug("Essentials plugin not found, registerComamnds() doing nothing");
            return;
        }
        
        try {
            grabInternalAltCommands();
            mapHSPCommands();
        }
        catch(Exception e) {
            log.error("Caught exception when trying to register commands with Essentials", e);
        }
        
        log.debug("exiting registerCommands()");
    }
    
    /**
     * Using Essentials own internal alternate commands map, assign HSP
     * commands into the map. This is a replication of the internal algorithm
     * that Essentials uses in AlternativeCommandsHandler as of Essentials
     * 2.10.1.
     */
    private void mapHSPCommands() {
        Map<String, PluginCommand> hspCommands = bukkitCommandRegister.getLoadedCommands();
        Collection<PluginCommand> commands = hspCommands.values();
//        final String pluginName = plugin.getDescription().getName().toLowerCase();

        log.debug("commands.size() = {}", commands == null ? null : commands.size());
        for (Command command : commands)
        {
            final PluginCommand pc = (PluginCommand)command;
            final List<String> labels = new ArrayList<String>(pc.getAliases());
            labels.add(pc.getName());

            log.debug("registering command {}", pc.getName());
//            PluginCommand reg = plugin.getServer().getPluginCommand(pluginName + ":" + pc.getName().toLowerCase());
//            if (reg == null)
//            {
//                reg = plugin.getServer().getPluginCommand(pc.getName().toLowerCase());
//            }
//            if (reg == null || !reg.getPlugin().equals(plugin))
//            {
//                continue;
//            }
//            log.debug("reg = {}", reg);
            for (String label : labels)
            {
                log.debug("registering label {}", label);
                List<PluginCommand> plugincommands = altcommands.get(label.toLowerCase());
                if (plugincommands == null)
                {
                    plugincommands = new ArrayList<PluginCommand>();
                    altcommands.put(label.toLowerCase(), plugincommands);
                }
                boolean found = false;
                for (PluginCommand pc2 : plugincommands)
                {
                    if (pc2.getPlugin().equals(plugin))
                    {
                        found = true;
                    }
                }
                if (!found)
                {
                    plugincommands.add(pc);
                }
            }
        }
    }

    /**
     * Method to grab Essentials internal altCommands hash. This is ugly code
     * but Essentials offers no external APIs to make this any cleaner.
     * 
     * @throws NoSuchFieldException 
     * @throws SecurityException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    @SuppressWarnings("unchecked")
    private void grabInternalAltCommands() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Essentials pluginObject = (Essentials) essentialsPlugin;
        Field commandHandler = pluginObject.getClass().getDeclaredField("alternativeCommandsHandler");
        commandHandler.setAccessible(true);
        AlternativeCommandsHandler ach = (AlternativeCommandsHandler) commandHandler.get(pluginObject);
        Field altCommandsField = ach.getClass().getDeclaredField("altcommands");
        altCommandsField.setAccessible(true);
        altcommands = (HashMap<String, List<PluginCommand>>) altCommandsField.get(ach);
        
        log.debug("altcommands = {}", altcommands);
    }

    @Override
    public void init() throws Exception {
        // we register commands a few ticks after the server has started
        // up. This gives Essentials time to load (if present).
        scheduler.scheduleSyncDelayedTask(new Runnable() {
            public void run() {
                registerCommands();
            }
        }, 3);
    }

    @Override
    public void shutdown() throws Exception {
    }

    @Override
    public int getInitPriority() {
        return 9;
    }
}
