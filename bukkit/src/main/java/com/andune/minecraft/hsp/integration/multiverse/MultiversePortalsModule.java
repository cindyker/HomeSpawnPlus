/**
 * 
 */
package com.andune.minecraft.hsp.integration.multiverse;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;

import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.config.ConfigCore;

/**
 * @author andune
 *
 */
@Singleton
public class MultiversePortalsModule implements MultiversePortals, Initializable {
    private static final Logger log = LoggerFactory.getLogger(MultiversePortalsModule.class);

    private final Plugin plugin;
    private final ConfigCore configCore;
    private final MultiverseListener multiverseListener;
    private String sourcePortalName;
    private String destinationPortalName;

    @Inject
    public MultiversePortalsModule(ConfigCore configCore, Plugin plugin, MultiverseCoreModule mvCore) {
        this.configCore = configCore;
        this.plugin = plugin;
        this.multiverseListener = mvCore.getMultiverseListener();
    }

    @Override
    public boolean isEnabled() {
        if( configCore.isMultiverseEnabled() ) {    // enabled in config?
            Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
            if( p != null )                         // plugin exists?
                return p.isEnabled();               // plugin is enabled?
        }

        // if we get here, this module is not enabled
        return false;
    }

    @Override
    public String getVersion() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
        if( p != null )
            return p.getDescription().getVersion();
        else
            return null;
    }

    @Override
    public void init() throws Exception {
        if( !isEnabled() )
            return;
        
        Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
        if( p != null ) {
            if( p.getDescription().getVersion().startsWith("2.4") ) {
                log.debug("Registering Multiverse-Portals listener");
                registerListener();
            }
        }
    }

    @Override
    public void shutdown() throws Exception {
    }

    @Override
    public int getInitPriority() {
        return 10;
    }

    @Override
    public String getSourcePortalName() {
        return sourcePortalName;
    }

    @Override
    public void setSourcePortalName(String sourcePortalName) {
        this.sourcePortalName = sourcePortalName;
    }

    @Override
    public String getDestinationPortalName() {
        return destinationPortalName;
    }

    @Override
    public void setDestinationPortalName(String destinationPortalName) {
        this.destinationPortalName = destinationPortalName;
    }

    private void registerListener() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
        if( p != null ) {
            plugin.getServer().getPluginManager().registerEvent(com.onarandombox.MultiversePortals.event.MVPortalEvent.class,
                    multiverseListener,
                    EventPriority.NORMAL,
                    new EventExecutor() {
                        public void execute(Listener listener, Event event) throws EventException {
                            try {
                                multiverseListener.onMultiversePortalEvent((com.onarandombox.MultiversePortals.event.MVPortalEvent) event);
                            } catch (Throwable t) {
                                throw new EventException(t);
                            }
                        }
                    },
                    plugin);
        }
    }
}
