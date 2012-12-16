/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.morganm.homespawnplus.server.api.CommandSender;
import org.morganm.homespawnplus.server.api.Factory;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.TeleportOptions;
import org.morganm.homespawnplus.server.api.YamlFile;
import org.morganm.homespawnplus.strategy.StrategyContext;

import com.google.inject.Injector;

/**
 * @author morganm
 *
 */
public class BukkitFactory implements Factory {
    private final Injector injector;
    private final Map<String, WeakReference<CommandSender>> senderCache = new HashMap<String, WeakReference<CommandSender>>();
    
    @Inject
    BukkitFactory(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Location newLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        return new BukkitLocation(worldName, x, y, z, yaw, pitch);
    }

    @Override
    public TeleportOptions newTeleportOptions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StrategyContext newStrategyContext() {
        return injector.getInstance(StrategyContext.class);
    }

    @Override
    public YamlFile newYamlFile() {
        return injector.getInstance(BukkitYamlConfigFile.class);
    }
    
    public CommandSender getCommandSender(org.bukkit.command.CommandSender bukkitSender) {
        // lookup reference
        WeakReference<CommandSender> ref = senderCache.get(bukkitSender.getName());

        // if reference isn't null, get the object
        CommandSender sender = null;
        if( ref != null )
            sender = ref.get();

        // if object is null, create a new reference
        if( sender == null ) {
            WeakReference<CommandSender> wr = new WeakReference<CommandSender>(new BukkitCommandSender(bukkitSender));
            sender = wr.get();
            senderCache.put(bukkitSender.getName(), wr);
        }

        return sender;
    }
}
