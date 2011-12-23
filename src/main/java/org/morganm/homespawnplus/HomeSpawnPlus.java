package org.morganm.homespawnplus;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.morganm.homespawnplus.command.CommandProcessor;
import org.morganm.homespawnplus.config.Config;
import org.morganm.homespawnplus.config.ConfigException;
import org.morganm.homespawnplus.config.ConfigFactory;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.entity.Version;
import org.morganm.homespawnplus.listener.HSPEntityListener;
import org.morganm.homespawnplus.listener.HSPPlayerListener;
import org.morganm.homespawnplus.listener.HSPWorldListener;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.StorageFactory;
import org.morganm.homespawnplus.util.CommandUsurper;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.JarUtils;
import org.morganm.homespawnplus.util.PermissionSystem;

/**
 * HomeSpawnPlus plugin for Bukkit
 *
 * @author morganm, Timberjaw
 */
public class HomeSpawnPlus extends JavaPlugin {
    public static final Logger log = Logger.getLogger("HomeSpawnPlus");
    public static final String logPrefix = "[HomeSpawnPlus]";
    
    public final static String YAML_CONFIG_ROOT_PATH = "plugins/HomeSpawnPlus/";
    public final static String YAML_BACKUP_FILE = YAML_CONFIG_ROOT_PATH + "backup.yml";
	public final static String BASE_PERMISSION_NODE = "hsp";
    
    // singleton instance - not declared final as the plugin can be reloaded,
	// and the instance will change to the new plugin. This will always
	// return the most recent plugin object that was loaded.
    private static HomeSpawnPlus instance;

    private PermissionSystem perms;
    
    private CooldownManager cooldownManager;
    private WarmupManager warmupManager;
    private HomeSpawnUtils spawnUtils;
	private Config config;
    private CommandProcessor cmdProcessor;
    private HSPPlayerListener playerListener;
    private HSPEntityListener entityListener;
    private JarUtils jarUtils;
	private int buildNumber = -1;
    private PluginDescriptionFile pluginDescription;
    private String pluginName;
    private Storage storage;

    public Economy vaultEconomy = null;
    
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
    
    @Override
    public void onEnable() {
    	boolean loadError = false;
    	
    	getConfig();
    	
    	instance = this;
    	pluginDescription = getDescription();
    	pluginName = pluginDescription.getName();
    	
    	Debug.getInstance().init(log, logPrefix+"[DEBUG] ", false);
    	jarUtils = new JarUtils(this, getFile(), log, logPrefix);
		buildNumber = jarUtils.getBuildNumber();
    	
    	// load our configuration and database
    	try {
    		loadConfig();
    		updateConfigDefaultFile();
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
    	setupVaultEconomy();
    	
    	cooldownManager = new CooldownManager(this);
    	warmupManager = new WarmupManager(this);
    	spawnUtils = new HomeSpawnUtils(this);
    	
        PluginManager pm = getServer().getPluginManager();
    	playerListener = new HSPPlayerListener(this);
    	entityListener = new HSPEntityListener(this);
        
    	// Register our events
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, getEventPriority(), this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, getEventPriority(), this);
        
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.WORLD_LOAD, new HSPWorldListener(this), Priority.Monitor, this);

        hookWarmups();
        
    	cmdProcessor = new CommandProcessor(HomeSpawnPlus.getInstance());
    	new CommandUsurper(this, log, logPrefix).usurpCommands();
    	
		log.info(logPrefix + " version "+pluginDescription.getVersion()+", build "+buildNumber+" is enabled");
    }
    
    @Override
    public void onDisable() {
    	/*
    	try {
    		config.save();
    	}
    	catch(ConfigException e) {
    		log.warning(logPrefix + " error saving configuration during onDisable");
    		e.printStackTrace();
    	}
    	*/
    	
    	Player[] players = getServer().getOnlinePlayers();
    	for(int i=0; i < players.length;i++) {
    		spawnUtils.updateQuitLocation(players[i]);
    	}

    	getServer().getScheduler().cancelTasks(this);
		log.info(logPrefix + " version "+pluginDescription.getVersion()+", build "+buildNumber+" is disabled");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	return cmdProcessor.onCommand(sender, command, commandLabel, args);
    }
    
    /** Returns the Config object the plugin is currently using.
     * 
     * @return
     */
    public Config getHSPConfig()
    {
    	return config;
    }
    
    // since we provide our own Configuration interface (via getHSPConfig()), we override
    // JavaPlugin's built-in one to be make it use ours (in the event that they are
    // compatible) or none at all.
    public FileConfiguration getConfig() {
    	if( config instanceof FileConfiguration )
    		return (FileConfiguration) config;
    	else
    		return null;
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
    
    private void initPermissions() {
    	perms = new PermissionSystem(this, log, logPrefix);
    	perms.setupPermissions();
    }
    
    private void updateConfigDefaultFile() {
		// make sure the config_defaults.yml file is always up-to-date. We do this by just 
		// deleting it and then letting the Config initialization code just copy it out
    	// of the JAR file on to disk.
		try {
			File configDefaultsFile = new File(YAML_CONFIG_ROOT_PATH+"config_defaults.yml");
			configDefaultsFile.delete();
			Debug.getInstance().devDebug("copying config_defaults.yml into place");
			ConfigFactory.getInstance(ConfigFactory.Type.YAML, this, YAML_CONFIG_ROOT_PATH+"config_defaults.yml").load();
		}
		catch(Exception e) {
			// we don't care if this fails, ignore any errors
		}
    }
    
    public void loadConfig() throws ConfigException, IOException {
    	if( config == null )
    		config = ConfigFactory.getInstance(ConfigFactory.Type.YAML, this, YAML_CONFIG_ROOT_PATH+"config.yml");
		config.load();
		Debug.getInstance().setDebug(config.getBoolean(ConfigOptions.DEV_DEBUG, false), Level.FINEST);
		Debug.getInstance().setDebug(config.getBoolean(ConfigOptions.DEBUG, false));
    }

    private Boolean setupVaultEconomy()
    {
    	Plugin vault = getServer().getPluginManager().getPlugin("Vault");
    	if( vault != null ) {
    		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    		if (economyProvider != null) {
    			vaultEconomy = economyProvider.getProvider();
    			log.info(logPrefix + " Vault interface found and will be used for economy-related functions");
    		}
    	}
    	else
    		log.info(logPrefix + " Vault not found, HSP economy features are disabled");

		return (vaultEconomy != null);
    }
    
    public Economy getEconomy() {
    	return vaultEconomy;
    }
    
    /** Lookup the event priority the admin has assigned in the config and return the
     * Bukkit value for that priority.
     * 
     * @return
     */
    private Priority getEventPriority() {
    	String strPriority = config.getString(ConfigOptions.EVENT_PRIORITY, "highest");
    	
    	if( strPriority.equalsIgnoreCase("highest") )
    		return Priority.Highest;
    	else if( strPriority.equalsIgnoreCase("high") )
    		return Priority.High;
    	else if( strPriority.equalsIgnoreCase("normal") )
    		return Priority.Normal;
    	else if( strPriority.equalsIgnoreCase("low") )
    		return Priority.Low;
    	else if( strPriority.equalsIgnoreCase("lowest") )
    		return Priority.Lowest;
    	else
    		return Priority.Highest;	// default
    }
    
    //    private boolean hasHookedOnMoveWarmups = false;
    private boolean hasHookedOnDamageWarmups = false;
    /** To be efficient, we don't hook warmup events unless the config option is set.  We keep
     * track in a boolean if we've already hooked it since Bukkit provides no way to unhook.
     */
    public void hookWarmups() {
        if( config.getBoolean(ConfigOptions.USE_WARMUPS, false) )
        {
        	PluginManager pm = getServer().getPluginManager();
        	
//        	if( !hasHookedOnMoveWarmups && config.getBoolean(ConfigOptions.WARMUPS_ON_MOVE_CANCEL, false) ) {
//            	hasHookedOnMoveWarmups = true;
//	            pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Monitor, this);
//	            pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Monitor, this);
//	            pm.registerEvent(Event.Type.PLAYER_PORTAL, playerListener, Priority.Monitor, this);
//        	}
            
        	if( !hasHookedOnDamageWarmups && config.getBoolean(ConfigOptions.WARMUPS_ON_DAMAGE_CANCEL, false) ) {
            	hasHookedOnDamageWarmups = true;
        		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
        	}
        }
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
        classList.add(Version.class);
        return classList;
    }
    
    public boolean hasPermission(String worldName, String playerName, String permissionNode) {
    	boolean result = perms.has(worldName, playerName, permissionNode);
    	
    	// if using OPS system, support legacy HSP "defaultPermission" setting
    	if( !result && perms.getSystemInUse() == PermissionSystem.OPS ) {
    		List<String> defaultPerms = config.getStringList(ConfigOptions.DEFAULT_PERMISSIONS, null);
    		if( defaultPerms.contains(permissionNode) )
    			result = true;
    	}
    	return result;
    }
    
   /** Return true if the given player has access to the given permission node.
     * If we aren't using a Permission system, then defaults to op check - ops have
     * full access to all permissions.
     * 
     * @param p
     * @param permissionNode
     * @return
     */
    public boolean hasPermission(CommandSender sender, String permissionNode) {
    	boolean result = perms.has(sender, permissionNode);
    	
    	// if using OPS system, support legacy HSP "defaultPermission" setting
    	if( !result && perms.getSystemInUse() == PermissionSystem.OPS ) {
    		List<String> defaultPerms = config.getStringList(ConfigOptions.DEFAULT_PERMISSIONS, null);
    		if( defaultPerms.contains(permissionNode) )
    			result = true;
    	}
    	return result;
    }
    
    /** Given a playerName, return the permissions group they are associated with (if any).
     * 
     * @param playerName
     * @return the group or null if no group
     */
	public String getPlayerGroup(String world, String playerName) {
    	return perms.getPlayerGroup(world, playerName);
    }
        
    @Override
    public ClassLoader getClassLoader() { return super.getClassLoader(); }
    
    public Storage getStorage() { return storage; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public WarmupManager getWarmupmanager() { return warmupManager; }
    public HomeSpawnUtils getUtil() { return spawnUtils; }
    public String getPluginName() { return pluginName; }
    public JarUtils getJarUtils() { return jarUtils; }
}