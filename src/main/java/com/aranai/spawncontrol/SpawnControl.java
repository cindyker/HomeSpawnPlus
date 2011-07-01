package com.aranai.spawncontrol;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import com.aranai.spawncontrol.config.Config;
import com.aranai.spawncontrol.config.ConfigFactory;
import com.aranai.spawncontrol.storage.Storage;
import com.aranai.spawncontrol.storage.StorageException;
import com.aranai.spawncontrol.storage.StorageFactory;
import com.avaje.ebean.EbeanServer;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * SpawnControl for Bukkit
 *
 * @author morganm, Timberjaw
 */
public class SpawnControl {
    public static final Logger log = Logger.getLogger("SpawnControl");;
    public static String logPrefix;
    
    public final static String YAML_CONFIG_ROOT_PATH = "plugins/mSpawnControl/";
	public final static String BASE_PERMISSION_NODE = "SpawnControl";
    
    private PermissionHandler permissionHandler;
    private boolean usePermissions = false;
    private boolean usePerm3 = false;
    
    // singleton instance - not declared final as the plugin can be reloaded, and the instance
    // will change to the new plugin.  But this will always return the most recent plugin
    // object that was loaded.
    private static SpawnControl instance;
    
    // instance members that are set once in constructor - mostly references to static, unchanging
    // support classes
    private final SpawnControlPlugin spawnControlPlugin;
    private final Server server;
    private final PluginDescriptionFile pluginDescription;
    private final CooldownManager cooldownManager;
    private final SpawnUtils spawnUtils;
    private final String pluginName;

    // instance members that aren't set until we are first enabled
    private Storage storage;
    private Config config;

    /** Private constructor guarantees Singleton instance.
     */
    private SpawnControl(SpawnControlPlugin scp) {
    	spawnControlPlugin = scp;

    	server = scp.getServer();
    	pluginDescription = scp.getDescription();
    	pluginName = pluginDescription.getName();
    	logPrefix = "[" + pluginName + "]";
    	
    	cooldownManager = new CooldownManager(this);
    	spawnUtils = new SpawnUtils(this);
    }
    
    /** Not your typical singleton pattern - this CAN return null in the event the plugin is unloaded. 
     * 
     * @return the singleton instance or null if there is none
     */
    public static SpawnControl getInstance() {
    	return instance;
    }
    
    public synchronized static void createInstance(SpawnControlPlugin scp) {
		instance = new SpawnControl(scp);
    }
    
    /** Divergent from typical singleton pattern, our singleton CAN be unloaded.
     * 
     */
    public static void clearInstance() {
    	instance = null;
    }
    
    public ClassLoader getClassLoader() { return spawnControlPlugin.getPluginClassLoader(); }
    
    /** Returns the Config object SpawnControl is currently using.
     * 
     * @return
     */
    public Config getConfig()
    {
    	return config;
    }
    
    public void initDB() {
        spawnControlPlugin.initDB();
    }
    
    /** Load our data from the backing data store.
     * 
     * @throws IOException
     * @throws StorageException
     */
    public void loadDB() throws IOException, StorageException {
        storage = StorageFactory.getInstance(StorageFactory.Type.EBEANS, this);
        
        // Make sure storage system is initialized
        storage.initializeStorage();
        
        // TODO: possibly pre-cache the data here later
    }
    
    /** Initialize permission system.
     * 
     */
    private void initPermissions() {
        Plugin permissionsPlugin = server.getPluginManager().getPlugin("Permissions");
        if( permissionsPlugin != null ) {
        	permissionHandler = ((Permissions) permissionsPlugin).getHandler();
        	usePermissions = true;
        	
        	if( permissionsPlugin.getDescription().getVersion().startsWith("3") )
        		usePerm3 = true;
        }
        else
	    	log.warning(logPrefix+" Permissions system not enabled, using isOP instead.");
    }
    
    public void onEnable() {
    	boolean loadError = false;
    	
    	// load our configuration and database
    	try {
    		config = ConfigFactory.getInstance(ConfigFactory.Type.YAML, this, YAML_CONFIG_ROOT_PATH+"config.yml");
    		
            this.loadDB();
    	}
    	catch(Exception e) {
    		loadError = true;
    		log.severe("Error loading plugin: "+pluginDescription.getName());
    		e.printStackTrace();
    	}
    	
    	if( loadError ) {
    		log.severe("Error detected when loading plugin "+ pluginDescription.getName() +", plugin shutting down.");
    		server.getPluginManager().disablePlugin(spawnControlPlugin);
    		return;
    	}
    	
    	initPermissions();
    	
        PluginManager pm = server.getPluginManager();
    	SCPlayerListener playerListener = new SCPlayerListener(this);
        
    	// Register our events
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Highest, spawnControlPlugin);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, spawnControlPlugin);
        pm.registerEvent(Event.Type.WORLD_LOAD, new SCWorldListener(this), Priority.Monitor, spawnControlPlugin);
        
        log.info( logPrefix + " version [" + pluginDescription.getVersion() + "] loaded" );
    }
    
    public void onDisable() {
    	log.info( logPrefix + " version [" + pluginDescription.getVersion() + "] unloaded" );
    }
    
    /** Return true if the given player has access to the given permission node.
     * If we aren't using a Permission system, then defaults to op check - ops have
     * full access to all permissions.
     * 
     * @param p
     * @param permissionNode
     * @return
     */
    public boolean hasPermission(Player p, String permissionNode) {
    	if( permissionHandler != null ) 
    		return permissionHandler.has(p, permissionNode);
    	else
    		return p.isOp();
    }
    
    /** Return true if we found and are using Permissions system, false if not.
     * 
     * @return
     */
    public boolean isUsePermissions() { return usePermissions; }
    
    /** Return true if we are using Permission system and it is specifically Permissions 3.x
     * 
     * @return
     */
    public boolean isUsePerm3() { return usePermissions && usePerm3; }
    
    public Server getServer() { return server; }
    
    public Storage getStorage() { return storage; }
    
    public EbeanServer getDatabase() { return spawnControlPlugin.getDatabase(); }
    
    public PermissionHandler getPermissionHandler() { return permissionHandler; }
    
    public CooldownManager getCooldownManager() { return cooldownManager; }
    
    public SpawnUtils getSpawnUtils() { return spawnUtils; }
    
    public String getPluginName() { return pluginName; }
}