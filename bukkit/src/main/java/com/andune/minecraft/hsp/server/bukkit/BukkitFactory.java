/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;


import com.andune.minecraft.hsp.Permissions;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.server.api.CommandSender;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.server.api.TeleportOptions;
import com.andune.minecraft.hsp.server.api.YamlFile;
import com.andune.minecraft.hsp.storage.dao.PlayerDAO;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.google.inject.Injector;

/**
 * @author morganm
 *
 */
@Singleton
public class BukkitFactory implements Factory {
    private final Injector injector;
    private final ConfigCore configCore;
    private final PlayerDAO playerDAO;
    private final Permissions perm;
    private final Map<String, WeakReference<CommandSender>> senderCache = new HashMap<String, WeakReference<CommandSender>>();
    
    @Inject
    BukkitFactory(Injector injector, ConfigCore configCore, PlayerDAO playerDAO, Permissions perm) {
        this.injector = injector;
        this.configCore = configCore;
        this.playerDAO = playerDAO;
        this.perm = perm;
    }

    @Override
    public Location newLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        return new BukkitLocation(worldName, x, y, z, yaw, pitch);
    }

    @Override
    public TeleportOptions newTeleportOptions() {
        return injector.getInstance(TeleportOptions.class);
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
    
    public BukkitPlayer newBukkitPlayer(org.bukkit.entity.Player bukkitPlayer) {
        return new BukkitPlayer(configCore, playerDAO, perm, bukkitPlayer);
    }
}
