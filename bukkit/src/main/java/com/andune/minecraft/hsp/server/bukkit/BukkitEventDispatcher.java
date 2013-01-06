/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import com.andune.minecraft.hsp.config.ConfigWarmup;
import com.andune.minecraft.hsp.server.api.event.EventListener;

/** Bridge class between Bukkit event system and HSP API event interface.
 * 
 * @author morganm
 *
 */
@Singleton
public class BukkitEventDispatcher implements com.andune.minecraft.hsp.server.api.event.EventDispatcher, org.bukkit.event.Listener {
    private final EventListener eventListener;
    private final Plugin plugin;
    private final BukkitFactory bukkitFactory;
    private final ConfigWarmup configWarmup;
    
    @Inject
    public BukkitEventDispatcher(EventListener listener, Plugin plugin, BukkitFactory bukkitFactory, ConfigWarmup configWarmup) {
        this.eventListener = listener;
        this.plugin = plugin;
        this.bukkitFactory = bukkitFactory;
        this.configWarmup = configWarmup;
    }

    /**
     * Register events with Bukkit server.
     * 
     */
    public void registerEvents() {
        // register annotated event methods
        plugin.getServer().getPluginManager().registerEvents(this,  plugin);
        
        // register events with config-defined priorities 
        plugin.getServer().getPluginManager().registerEvent(PlayerJoinEvent.class,
                this,
                getEventPriority(),
                new EventExecutor() {
                    public void execute(Listener listener, Event event) throws EventException {
                        try {
                            com.andune.minecraft.hsp.server.api.events.PlayerJoinEvent apiEvent =
                                    new com.andune.minecraft.hsp.server.bukkit.events.PlayerJoinEvent((PlayerJoinEvent) event, bukkitFactory);
                            eventListener.playerJoin(apiEvent);
                        } catch (Throwable t) {
                            throw new EventException(t);
                        }
                    }
                },
                plugin);

        plugin.getServer().getPluginManager().registerEvent(PlayerRespawnEvent.class,
                this,
                getEventPriority(),
                new EventExecutor() {
                    public void execute(Listener listener, Event event) throws EventException {
                        try {
                            com.andune.minecraft.hsp.server.api.events.PlayerRespawnEvent apiEvent =
                                    new com.andune.minecraft.hsp.server.bukkit.events.PlayerRespawnEvent((PlayerRespawnEvent) event, bukkitFactory);
                            eventListener.playerRespawn(apiEvent);
                        } catch (Throwable t) {
                            throw new EventException(t);
                        }
                    }
                },
                plugin);

        // only hook EntityDamageEvent if it's needed by warmups
        if( configWarmup.isEnabled() && configWarmup.isCanceledOnDamage() ) {
            plugin.getServer().getPluginManager().registerEvent(EntityDamageEvent.class,
                    this,
                    EventPriority.MONITOR,
                    new EventExecutor() {
                        public void execute(Listener listener, Event event) throws EventException {
                            try {
                                onEntityDamage((EntityDamageEvent) event);
                            } catch (Throwable t) {
                                throw new EventException(t);
                            }
                        }
                    },
                    plugin);
        }
    }
    
    /**
     * Return event priority as defined by the admin in the config file.
     * 
     * @return
     */
    private EventPriority getEventPriority() {
        return EventPriority.HIGHEST;   // TODO: drive from config
    }
    
    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void playerTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
        com.andune.minecraft.hsp.server.api.events.PlayerTeleportEvent apiEvent =
                new com.andune.minecraft.hsp.server.bukkit.events.PlayerTeleportEvent(event, bukkitFactory);
        eventListener.observePlayerTeleport(apiEvent);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void playerTeleportObserver(org.bukkit.event.player.PlayerTeleportEvent event) {
        com.andune.minecraft.hsp.server.api.events.PlayerTeleportEvent apiEvent =
                new com.andune.minecraft.hsp.server.bukkit.events.PlayerTeleportEvent(event, bukkitFactory);
        eventListener.observePlayerTeleport(apiEvent);
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block b = event.getClickedBlock();
        // did they click on a bed? short-circuit this method if not (fail-fast)
        if( b.getTypeId() != 26 )
            return;

        // if we get here, it was a bed right-click event, so fire one
        com.andune.minecraft.hsp.server.api.events.PlayerBedRightClickEvent apiEvent =
                new com.andune.minecraft.hsp.server.bukkit.events.PlayerBedRightClickEvent(event, bukkitFactory);
        eventListener.bedRightClick(apiEvent);
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
    public void onBedEvent(PlayerBedEnterEvent event) {
        com.andune.minecraft.hsp.server.api.events.PlayerBedEnterEvent apiEvent =
                new com.andune.minecraft.hsp.server.bukkit.events.PlayerBedEnterEvent(event, bukkitFactory);
        eventListener.bedEvent(apiEvent);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event)
    {
        com.andune.minecraft.hsp.server.api.events.PlayerQuitEvent apiEvent =
                new com.andune.minecraft.hsp.server.bukkit.events.PlayerQuitEvent(event, bukkitFactory);
        eventListener.playerQuit(apiEvent);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(org.bukkit.event.player.PlayerKickEvent event)
    {
        com.andune.minecraft.hsp.server.api.events.PlayerKickEvent apiEvent =
                new com.andune.minecraft.hsp.server.bukkit.events.PlayerKickEvent(event, bukkitFactory);
        eventListener.playerKick(apiEvent);
    }

    // this event is dynamically hooked only if needed
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event)
    {
        if( event.isCancelled() )
            return;
        if( !(event.getEntity() instanceof org.bukkit.entity.Player) )
            return;

        com.andune.minecraft.hsp.server.api.events.PlayerDamageEvent apiEvent =
                new com.andune.minecraft.hsp.server.bukkit.events.PlayerDamageEvent((Player) event.getEntity(), bukkitFactory);
        eventListener.playerDamage(apiEvent);
    }

    /* Not needed, not used outside of WorldGuard module which will register
     * the event natively if needed.
    @Override
    public void registerMoveEvent() {
        plugin.getServer().getPluginManager().registerEvent(PlayerMoveEvent.class,
                this,
                getEventPriority(),
                new EventExecutor() {
                    public void execute(Listener listener, Event event) throws EventException {
                        try {
                            org.morganm.homespawnplus.server.api.events.PlayerMoveEvent apiEvent =
                                    new org.morganm.homespawnplus.server.bukkit.events.PlayerMoveEvent((PlayerMoveEvent) event, bukkitFactory);
                            eventListener.playerMove(apiEvent);
                        } catch (Throwable t) {
                            throw new EventException(t);
                        }
                    }
                },
                plugin);
    }
    */
}
