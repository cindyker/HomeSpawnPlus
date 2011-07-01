package com.aranai.spawncontrol;

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

import com.aranai.spawncontrol.command.CommandProcessor;
import com.aranai.spawncontrol.config.Config;
import com.aranai.spawncontrol.config.ConfigException;
import com.aranai.spawncontrol.config.ConfigFactory;
import com.aranai.spawncontrol.entity.Home;
import com.aranai.spawncontrol.entity.Spawn;
import com.aranai.spawncontrol.storage.Storage;
import com.aranai.spawncontrol.storage.StorageException;
import com.aranai.spawncontrol.storage.StorageFactory;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * SpawnControl for Bukkit
 *
 * @author morganm, Timberjaw
 */
public class SpawnControl extends JavaPlugin {
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
    
    private PluginDescriptionFile pluginDescription;
    private CooldownManager cooldownManager;
    private SpawnUtils spawnUtils;
    private String pluginName;
    private Storage storage;
    private File jarFile;
	private Config config;
    private CommandProcessor cmdProcessor;

    /** Not your typical singleton pattern - this CAN return null in the event the plugin is unloaded. 
     * 
     * @return the singleton instance or null if there is none
     */
    public static SpawnControl getInstance() {
    	return instance;
    }
    
    /** Divergent from typical singleton pattern, our singleton CAN be unloaded.
     * 
     */
    public static void clearInstance() {
    	instance = null;
    }
    
    /** Returns the Config object SpawnControl is currently using.
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
    
    public void onEnable() {
    	boolean loadError = false;
    	instance = this;
    	logPrefix = "[" + pluginName + "]";
    	
    	jarFile = getFile();
    	pluginDescription = getDescription();
    	pluginName = pluginDescription.getName();
    	
    	cooldownManager = new CooldownManager(this);
    	spawnUtils = new SpawnUtils(this);
    	
    	// load our configuration and database
    	try {
    		config = ConfigFactory.getInstance(ConfigFactory.Type.YAML, this, YAML_CONFIG_ROOT_PATH+"config.yml");
    		config.load();
    		
            this.loadDB();
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
    	SCPlayerListener playerListener = new SCPlayerListener(this);
        
    	// Register our events
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.WORLD_LOAD, new SCWorldListener(this), Priority.Monitor, this);
        
    	cmdProcessor = new CommandProcessor(SpawnControl.getInstance());

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
    
    public void initDB() {
        installDDL();
    }
    
    /** Define the Entity classes that we want serialized to the database.
     */
    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> classList = new LinkedList<Class<?>>();
        classList.add(Home.class);
        classList.add(Spawn.class);
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

    @Override
    public ClassLoader getClassLoader() { return super.getClassLoader(); }
    
    public Storage getStorage() { return storage; }
    
    public PermissionHandler getPermissionHandler() { return permissionHandler; }
    
    public CooldownManager getCooldownManager() { return cooldownManager; }
    
    public SpawnUtils getSpawnUtils() { return spawnUtils; }
    
    public String getPluginName() { return pluginName; }
    
    public File getJarFile() { return jarFile;	}
}