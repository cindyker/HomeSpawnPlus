package com.andune.minecraft.hsp.server.sponge;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.i18n.Colors;
import com.andune.minecraft.commonlib.i18n.Locale;
import com.andune.minecraft.commonlib.server.api.*;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.server.api.*;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.text.Texts;

import java.util.*;

/**
 * @author andune
 */
public class SpongeServer implements com.andune.minecraft.hsp.server.api.Server, Initializable {
    private final Logger log = LoggerFactory.getLogger(SpongeServer.class);

    private final Game game;
    private final Plugin plugin;
    private final Teleport teleport;
    private final Locale locale;
    private final SpongeFactoryInterface spongeFactory;
    private final Colors colors;

    /* A cached list of worlds, so we don't have to constantly recreate new world
     * wrapper objects.
     */
    private Map<String, World> worlds;
    private List<World> worldList;
    /* A flag to tell us when the underlying Bukkit worlds have possibly changed,
     * in which case we reload our world cache before using it.
     */
    private boolean clearWorldCache = true;

    @Inject
    public SpongeServer(Game game, Plugin plugin, Teleport teleport, Locale locale,
                        SpongeFactoryInterface spongeFactory, Colors colors)
    {
        this.game = game;
        this.plugin = plugin;
        this.teleport = teleport;
        this.locale = locale;
        this.spongeFactory = spongeFactory;
        this.colors = colors;
    }

    @Override
    public String getLocalizedMessage(HSPMessages key, Object... args) {
        return getLocalizedMessage(key.toString(), args);
    }

    @Override
    public void sendLocalizedMessage(CommandSender sender, HSPMessages key, Object... args) {
        sendLocalizedMessage(sender, key.toString(), args);
    }

    @Override
    public String getDefaultColor() {
        return colors.getDefaultColorString();
    }

    // TODO: implement for Sponge
    @Override
    public void delayedTeleport(Player player, Location location) {
        /*
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                new DelayedTeleport(player, location), 2);
                */
    }

    private class DelayedTeleport implements Runnable {
        private Player p;
        private Location l;

        public DelayedTeleport(Player p, Location l) {
            this.p = p;
            this.l = l;
        }

        public void run() {
            log.debug("delayed teleporting {} to {}", p, l);
            teleport.safeTeleport(p, l);
        }
    }

    @Override
    public String getLocalizedMessage(String key, Object... args) {
        return locale.getMessage(key, args);
    }

    @Override
    public void sendLocalizedMessage(CommandSender sender, String key, Object... args) {
        sender.sendMessage(locale.getMessage(key, args));
    }

    @Override
    public Player getPlayer(String playerName) {
        Optional<org.spongepowered.api.entity.player.Player> optional = game.getServer().getPlayer(playerName);
        if (optional.isPresent()) {
            org.spongepowered.api.entity.player.Player player = optional.get();
            if( player != null )
                return spongeFactory.newSpongePlayer(player);
        }

        return null;
    }

    // TODO: figure out how to implement in Sponge
    @Override
    public OfflinePlayer getOfflinePlayer(String name) {
        return null;
    }


    /** Given a string, look for the best possible player match. Returned
     * object could be of subclass Player (if the player is online).
     *
     * @param playerName
     * @return the found OfflinePlayer object (possibly class Player) or null
     */
    public OfflinePlayer getBestMatchPlayer(String playerName) {
        // TODO: figure out how to implement in Sponge, possibly put algorithm
        // in common place to be shared between all server implementations
        return null;
    }

    // TODO: figure out how to do in Sponge
    @Override
    public String translateColorCodes(String stringToTranslate) {
        return Texts.replaceCodes(stringToTranslate, '&');
    }

    private void cacheWorlds() {
        Collection<org.spongepowered.api.world.World> spongeWorlds = game.getServer().getWorlds();

        // (re-)initialize array of the appropriate size
        worlds = new HashMap<String, World>(spongeWorlds.size());

        for(org.spongepowered.api.world.World spongeWorld : spongeWorlds) {
            worlds.put(spongeWorld.getName(), new SpongeWorld(spongeWorld));
        }

        worldList = Collections.unmodifiableList(new ArrayList<World>(worlds.values()));

        clearWorldCache = false;
    }

    @Override
    public List<World> getWorlds() {
        if( clearWorldCache )
            cacheWorlds();

        return worldList;
    }

    @Override
    public World getWorld(String worldName) {
        if( clearWorldCache )
            cacheWorlds();

        return worlds.get(worldName);
    }

    // TODO: figure out how to do this in Sponge
    @Override
    public OfflinePlayer[] getOfflinePlayers() {
        OfflinePlayer[] offlinePlayers = new OfflinePlayer[0];
        return offlinePlayers;
    }

    @Override
    public Player[] getOnlinePlayers() {
        Collection<org.spongepowered.api.entity.player.Player> spongeOnline = game.getServer().getOnlinePlayers();
        Player[] players = new Player[spongeOnline.size()];
        int i=-1;
        for(org.spongepowered.api.entity.player.Player spongePlayer : spongeOnline) {
            i++;
            players[i] = spongeFactory.newSpongePlayer(spongePlayer);
        }
        return players;
    }

    @Override
    public void init() throws Exception {
        // TODO: determine if we still need to listen to world events for some reason
//        this.plugin.getServer().getPluginManager().registerEvents(new WorldListener(), this.plugin);
    }

    @Override
    public void shutdown() throws Exception {
    }

    @Override
    public int getInitPriority() {
        return 9;
    }

}
