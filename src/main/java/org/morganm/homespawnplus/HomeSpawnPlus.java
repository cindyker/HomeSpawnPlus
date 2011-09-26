package org.morganm.homespawnplus;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.morganm.homespawnplus.command.CommandProcessor;
import org.morganm.homespawnplus.config.Config;
import org.morganm.homespawnplus.config.ConfigException;
import org.morganm.homespawnplus.config.ConfigFactory;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.StorageFactory;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * HomeSpawnPlus plugin for Bukkit
 *
 * @author morganm, Timberjaw
 */
public class HomeSpawnPlus extends JavaPlugin {
    public static final Logger log = Logger.getLogger("HomeSpawnPlus");;
    public static String logPrefix;
    
    public final static String YAML_CONFIG_ROOT_PATH = "plugins/HomeSpawnPlus/";
	public final static String BASE_PERMISSION_NODE = "hsp";
    
    private PermissionHandler permissionHandler;
    private boolean usePermissions = false;
    private boolean usePerm3 = false;
    
    // singleton instance - not declared final as the plugin can be reloaded, and the instance
    // will change to the new plugin.  But this will always return the most recent plugin
    // object that was loaded.
    private static HomeSpawnPlus instance;
    
    private PluginDescriptionFile pluginDescription;
    private CooldownManager cooldownManager;
    private WarmupManager warmupManager;
    private HomeSpawnUtils spawnUtils;
    private String pluginName;
    private Storage storage;
    private File jarFile;
	private Config config;
    private CommandProcessor cmdProcessor;
    private HSPPlayerListener playerListener;
    private HSPEntityListener entityListener;

    /** Not your typical singleton pattern - this CAN return null in the event the plugin is unloaded. 
     * 
     * @return the singleton instance or null if there is none
     */
    public static HomeSpawnPlus getInstance() {
    	return instance;
    }
    
    /** Divergent from typical singleton pattern, our singleton CAN be unloaded.
     * 
     */
    public static void clearInstance() {
    	instance = null;
    }
    
    /** Returns the Config object the plugin is currently using.
     * 
     * @return
     */
    public Config getConfig()
    {
    	return config;
    }
    
    /** Load our data from the backing data store.
     * 
     * @throws IOException
     * @throws StorageException
     */
    public void initializeDatabase() throws IOException, StorageException {
    	int type = config.getInt(ConfigOptions.STORAGE_TYPE, 0);
        storage = StorageFactory.getInstance(type, this);
        
        // Make sure storage system is initialized
        storage.initializeStorage();
        
        // TODO: possibly pre-cache the data here later
    }
    
    /** Initialize permission system.
     * 
     */
    private void initPermissions() {
        Plugin permissionsPlugin = getServer().getPluginManager().getPlugin("Permissions");
        if( permissionsPlugin != null ) {
        	permissionHandler = ((Permissions) permissionsPlugin).getHandler();
        	usePermissions = true;
        	
        	if( permissionsPlugin.getDescription().getVersion().startsWith("3") )
        		usePerm3 = true;
        }
        else
	    	log.warning(logPrefix+" Permissions system not enabled, using isOP instead.");
    }
    
    public void loadConfig() throws ConfigException, IOException {
		config = ConfigFactory.getInstance(ConfigFactory.Type.YAML, this, YAML_CONFIG_ROOT_PATH+"config.yml");
		config.load();
    }

    private boolean hasHookedOnMoveWarmups = false;
    private boolean hasHookedOnDamageWarmups = false;
    /** To be efficient, we don't hook warmup events unless the config option is set.  We keep
     * track in a boolean if we've already hooked it since Bukkit provides no way to unhook.
     */
    public void hookWarmups() {
        if( config.getBoolean(ConfigOptions.USE_WARMUPS, false) )
        {
        	PluginManager pm = getServer().getPluginManager();
        	
        	if( !hasHookedOnMoveWarmups && config.getBoolean(ConfigOptions.WARMUPS_ON_MOVE_CANCEL, false) ) {
            	hasHookedOnMoveWarmups = true;
	            pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Monitor, this);
	            pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Monitor, this);
	            pm.registerEvent(Event.Type.PLAYER_PORTAL, playerListener, Priority.Monitor, this);
        	}
            
        	if( !hasHookedOnDamageWarmups && config.getBoolean(ConfigOptions.WARMUPS_ON_DAMAGE_CANCEL, false) ) {
            	hasHookedOnDamageWarmups = true;
        		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
        	}
        }
    }
    
    public void onEnable() {
    	boolean loadError = false;
    	
    	instance = this;
    	pluginDescription = getDescription();
    	pluginName = pluginDescription.getName();
    	logPrefix = "[" + pluginName + "]";
    	
    	org.morganm.homespawnplus.entity.Player.setServer(getServer());
    	
    	jarFile = getFile();
    	
    	cooldownManager = new CooldownManager(this);
    	warmupManager = new WarmupManager(this);
    	spawnUtils = new HomeSpawnUtils(this);
    	
    	// load our configuration and database
    	try {
    		loadConfig();
            initializeDatabase();
    	}
    	catch(Exception e) {
    		loadError = true;
    		log.severe("Error loading plugin: "+pluginDescription.getName());
    		e.printStackTrace();
    	}
    	
    	if( loadError ) {
    		log.severe("Error detected when loading plugin "+ pluginDescription.getName() +", plugin shutting down.");
    		getServer().getPluginManager().disablePlugin(this);
    		return;
    	}
    	
    	initPermissions();
    	
        PluginManager pm = getServer().getPluginManager();
    	playerListener = new HSPPlayerListener(this);
    	entityListener = new HSPEntityListener(this);
        
    	// Register our events
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.WORLD_LOAD, new HSPWorldListener(this), Priority.Monitor, this);

        hookWarmups();
        
    	cmdProcessor = new CommandProcessor(HomeSpawnPlus.getInstance());

        log.info( logPrefix + " version [" + pluginDescription.getVersion() + "] loaded" );
    }
    
    public void onDisable() {
    	try {
    		config.save();
    	}
    	catch(ConfigException e) {
    		log.warning(logPrefix + " error saving configuration during onDisable");
    		e.printStackTrace();
    	}
    	
    	log.info( logPrefix + " version [" + pluginDescription.getVersion() + "] unloaded" );
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	return cmdProcessor.onCommand(sender, command, commandLabel, args);
    }
    
    public void installDatabaseDDL() {
        installDDL();
    }
    
    /** Define the Entity classes that we want serialized to the database.
     */
    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> classList = new LinkedList<Class<?>>();
        classList.add(Home.class);
        classList.add(Spawn.class);
        classList.add(org.morganm.homespawnplus.entity.Player.class);
        return classList;
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
//    	log.info(logPrefix + " checking permission "+permissionNode+" for player "+p.getName());
    	if( permissionHandler != null ) 
    		return permissionHandler.has(p, permissionNode);
    	else {
    		if( p.isOp() )
    			return true;
    		
    		List<String> defaultPerms = config.getStringList(ConfigOptions.DEFAULT_PERMISSIONS, null);
    		if( defaultPerms.contains(permissionNode) )
    			return true;
    		else
    			return false;
    	}
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

    @Override
    public ClassLoader getClassLoader() { return super.getClassLoader(); }
    
    public Storage getStorage() { return storage; }
    
    public PermissionHandler getPermissionHandler() { return permissionHandler; }
    
    public CooldownManager getCooldownManager() { return cooldownManager; }
    
    public WarmupManager getWarmupmanager() { return warmupManager; }
    
    public HomeSpawnUtils getUtil() { return spawnUtils; }
    
    public String getPluginName() { return pluginName; }
    
    public File getJarFile() { return jarFile;	}
}