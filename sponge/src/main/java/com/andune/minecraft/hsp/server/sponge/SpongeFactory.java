package com.andune.minecraft.hsp.server.sponge;

import com.andune.minecraft.commonlib.i18n.Colors;
import com.andune.minecraft.commonlib.server.api.*;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.server.api.Command;
import com.andune.minecraft.hsp.storage.dao.PlayerDAO;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.util.command.CommandSource;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author andune
 */
public class SpongeFactory implements com.andune.minecraft.hsp.server.api.Factory, SpongeFactoryInterface {
    private final Injector injector;
    private final Game game;
    private final ConfigCore configCore;
    private final PlayerDAO playerDAO;
    private final com.andune.minecraft.hsp.server.api.Server server;
    private final PermissionSystem perm;
    private final Colors colors;
    private final Map<String, WeakReference<CommandSender>> senderCache = new HashMap<String, WeakReference<CommandSender>>();

    @Inject
    protected SpongeFactory(Injector injector, PermissionSystem perm,
                            ConfigCore configCore, PlayerDAO playerDAO,
                            com.andune.minecraft.hsp.server.api.Server server,
                            Colors colors, Game game) {
        this.injector = injector;
        this.configCore = configCore;
        this.playerDAO = playerDAO;
        this.server = server;
        this.colors = colors;
        this.perm = perm;
        this.game = game;
    }

    @Override
    public StrategyContext newStrategyContext() {
        return injector.getInstance(StrategyContext.class);
    }

    @Override
    public Command newCommand(Class<? extends Command> commandClass) {
        return injector.getInstance(commandClass);
    }

    public SpongePlayer newSpongePlayer(org.spongepowered.api.entity.player.Player spongePlayer) {
        return new SpongePlayer(spongePlayer, this, server, perm, colors);
    }

    protected SpongeCommandSender newSpongeCommandSender(CommandSource spongeSource) {
        return new SpongeCommandSender(spongeSource, server, colors);
    }

    @Override
    public Location newLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        return new SpongeLocation(game, worldName, x, y, z, yaw, pitch);
    }

    public Location newLocation(org.spongepowered.api.world.Location spongeLocation) {
        return new SpongeLocation(game, spongeLocation);
    }

    @Override
    public TeleportOptions newTeleportOptions() {
        return injector.getInstance(TeleportOptions.class);
    }

    // TODO: not sure what I'm going to do about this yet..
    @Override
    public YamlFile newYamlFile() {
        return null;
    }

    public CommandSender getCommandSender(CommandSource spongeSource) {
        // lookup reference
        WeakReference<CommandSender> ref = senderCache.get(spongeSource.getName());

        // if reference isn't null, get the object
        CommandSender sender = null;
        if (ref != null)
            sender = ref.get();

        // if object is null, create a new reference
        if (sender == null) {
            WeakReference<CommandSender> wr = null;
            if (spongeSource instanceof Player)
                wr = new WeakReference<CommandSender>(newSpongePlayer((Player) spongeSource));
            else
                wr = new WeakReference<CommandSender>(newSpongeCommandSender(spongeSource));
            sender = wr.get();
            senderCache.put(spongeSource.getName(), wr);
        }

        return sender;
    }

    /**
     * Should be called whenever a player object is known to be invalidated.
     * Rather than clearing individual keys, we just evacuate the entire cache,
     * it is extremely fast to re-populate it.
     */
    public void clearPlayerCache() {
        senderCache.clear();
    }


    /**
     * Create and return a new vector object.
     *
     * @param x x velocity
     * @param y y velocity
     * @param z z velocity
     * @return the new Vector object
     */
    public Vector newVector(int x, int y, int z) {
        return new SpongeVector(x, y, z);
    }
}
