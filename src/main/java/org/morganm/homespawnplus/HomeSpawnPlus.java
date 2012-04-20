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
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
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
import org.morganm.homespawnplus.entity.HomeInvite;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.entity.Version;
import org.morganm.homespawnplus.i18n.Colors;
import org.morganm.homespawnplus.i18n.Locale;
import org.morganm.homespawnplus.i18n.LocaleConfig;
import org.morganm.homespawnplus.i18n.LocaleFactory;
import org.morganm.homespawnplus.listener.HSPEntityListener;
import org.morganm.homespawnplus.listener.HSPPlayerListener;
import org.morganm.homespawnplus.listener.HSPWorldListener;
import org.morganm.homespawnplus.manager.CooldownManager;
import org.morganm.homespawnplus.manager.HomeInviteManager;
import org.morganm.homespawnplus.manager.WarmupManager;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.StorageFactory;
import org.morganm.homespawnplus.storage.ebean.StorageEBeans;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableHome;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableHomeInvite;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializablePlayer;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableSpawn;
import org.morganm.homespawnplus.strategy.StrategyEngine;
import org.morganm.homespawnplus.util.CommandUsurper;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.JarUtils;
import org.morganm.homespawnplus.util.PermissionSystem;

/**
 * HomeSpawnPlus plugin for Bukkit.
 *
 * @author morganm
 */
public class HomeSpawnPlus extends JavaPlugin {
    public static final Logger log = Logger.getLogger("HomeSpawnPlus");
    public static final String logPrefix = "[HomeSpawnPlus]";
    
    public final static String YAML_CONFIG_ROOT_PATH = "plugins/HomeSpawnPlus/";
    public final static String YAML_BACKUP_FILE = YAML_CONFIG_ROOT_PATH + "backup.yml";
	public final static String BASE_PERMISSION_NODE = "hsp";
    
	static {
		ConfigurationSerialization.registerClass(SerializableHome.class, "Home");
		ConfigurationSerialization.registerClass(SerializableSpawn.class, "Spawn");
		ConfigurationSerialization.registerClass(SerializablePlayer.class, "Player");
		ConfigurationSerialization.registerClass(SerializableHomeInvite.class, "HomeInvite");
	}
	
    // singleton instance - This will always return the most recent
	// plugin object that was loaded.
    private static HomeSpawnPlus instance;

    private PermissionSystem perms;
    
    private CooldownManager cooldownManager;
    private WarmupManager warmupManager;
    private HomeSpawnUtils spawnUtils;
    private HomeInviteManager homeInviteManager;
    private StrategyEngine strategyEngine;
	private Config config;
    private CommandProcessor cmdProcessor;
    private HSPPlayerListener playerListener;
    private HSPEntityListener entityListener;
    private JarUtils jarUtils;
	private int buildNumber = -1;
    private PluginDescriptionFile pluginDescription;
    private String pluginName;
    private Storage storage;
    private Locale locale;
    private Debug debug;

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
    	long startupBegin = System.currentTimeMillis();
    	long startupTimer = 0;
    	boolean loadError = false;
    	instance = this;
    	
    	getConfig();
    	
    	pluginDescription = getDescription();
    	pluginName = pluginDescription.getName();
    	
    	Debug.getInstance().init(log, logPrefix, "plugins/HomeSpawnPlus/debug.log", false);
		debug = Debug.getInstance();
    	jarUtils = new JarUtils(this, getFile(), log, logPrefix);
		buildNumber = jarUtils.getBuildNumber();

    	// load our configuration and database
    	try {
        	strategyEngine = new StrategyEngine(this);
        	
        	startupTimer = System.currentTimeMillis();
        	debug.debug("[Startup Timer] loading config (t+", System.currentTimeMillis()-startupBegin+")");
    		loadConfig();
    		updateConfigDefaultFile();
        	debug.debug("[Startup Timer] config loaded in ", System.currentTimeMillis()-startupTimer, "ms");
        	
        	startupTimer = System.currentTimeMillis();
        	debug.debug("[Startup Timer] initializing database (t+", System.currentTimeMillis()-startupBegin+")");
            initializeDatabase();
        	debug.debug("[Startup Timer] database initialized in ", System.currentTimeMillis()-startupTimer, "ms");
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
    	
    	startupTimer = System.currentTimeMillis();
    	debug.debug("[Startup Timer] initializing permissions (t+", System.currentTimeMillis()-startupBegin+")");
    	initPermissions();
    	debug.debug("[Startup Timer] permissions initialized in ", System.currentTimeMillis()-startupTimer, "ms");
    	
    	startupTimer = System.currentTimeMillis();
    	debug.debug("[Startup Timer] initializing economy (t+", System.currentTimeMillis()-startupBegin+")");
    	setupVaultEconomy();
    	debug.debug("[Startup Timer] economy initialized in ", System.currentTimeMillis()-startupTimer, "ms");
    	
    	startupTimer = System.currentTimeMillis();
    	debug.debug("[Startup Timer] initializing HSP managers (t+", System.currentTimeMillis()-startupBegin+")");
    	cooldownManager = new CooldownManager(this);
    	warmupManager = new WarmupManager(this);
    	spawnUtils = new HomeSpawnUtils(this);
    	homeInviteManager = new HomeInviteManager(this);
    	debug.debug("[Startup Timer] HSP managers initialized in ", System.currentTimeMillis()-startupTimer, "ms");
    	
    	startupTimer = System.currentTimeMillis();
    	debug.debug("[Startup Timer] initializing Bukkit event registration (t+", System.currentTimeMillis()-startupBegin+")");
        final PluginManager pm = getServer().getPluginManager();

    	playerListener = new HSPPlayerListener(this);
    	pm.registerEvents(playerListener, this);		// bukkit annotation events
    	playerListener.registerEvents();				// runtime priority events
    	
    	pm.registerEvents(new HSPWorldListener(this), this);

    	entityListener = new HSPEntityListener(this);
        hookWarmups();
    	debug.debug("[Startup Timer] Bukkit event registration initialized in ", System.currentTimeMillis()-startupTimer, "ms");
        
    	startupTimer = System.currentTimeMillis();
    	debug.debug("[Startup Timer] initializing command system (t+", System.currentTimeMillis()-startupBegin+")");
    	cmdProcessor = new CommandProcessor(HomeSpawnPlus.getInstance());
    	new CommandUsurper(this, log, logPrefix).usurpCommands();
    	debug.debug("[Startup Timer] command system initialized in ", System.currentTimeMillis()-startupTimer, "ms");
    	
		log.info(logPrefix + " version "+pluginDescription.getVersion()+", build "+buildNumber+" is enabled");
    	debug.debug("[Startup Timer] HSP total initialization time: ", System.currentTimeMillis()-startupBegin, "ms");
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
    	
    	try {
    		storage.flushAll();
    	}
    	catch(StorageException e) {
    		log.log(Level.WARNING, logPrefix+" Caught exception: "+e.getMessage(), e);
    	}
    	
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
    
    public Locale getLocale() { return locale; }

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
    	Debug.getInstance().devDebug("TRACE: BEGIN initializeDatabase");
    	
    	StorageFactory.Type type = null;
    	
    	String stringType = config.getString(ConfigOptions.STORAGE_TYPE, "EBEANS");
    	int intType = -1;
    	// backwards compatibility means it might be an integer,
    	// so look for that
    	try {
    		intType = Integer.valueOf(stringType);
    	}
    	catch(NumberFormatException e) {}	// ignore, we don't care
    	
    	if( intType != -1 )
    		type = StorageFactory.getType(intType);
    	else
    		type = StorageFactory.getType(stringType);
    		
    	Debug.getInstance().debug("using storage type ",type);
        storage = StorageFactory.getInstance(type, this);
        
        // Make sure storage system is initialized
        storage.initializeStorage();
        
        // TODO: possibly pre-cache the data here later
    	Debug.getInstance().devDebug("TRACE: END initializeDatabase");
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
			ConfigFactory.getInstance(ConfigFactory.Type.YAML_EXTENDED_DEFAULT_FILE, this, YAML_CONFIG_ROOT_PATH+"config_defaults.yml").load();
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

		// also load/reload Locale
		LocaleConfig localeConfig = new LocaleConfig(
				config.getString("core.locale", "en"), this, "hsp", getFile(),
				log, logPrefix);
		locale = LocaleFactory.getLocale(localeConfig);
		Colors.setDefaultColor(config.getString("core.defaultMessageColor", "%yellow%"));
		
    	strategyEngine.getStrategyConfig().loadConfig();
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
    public EventPriority getEventPriority() {
    	String strPriority = config.getString(ConfigOptions.EVENT_PRIORITY, "highest");
    	
    	if( strPriority.equalsIgnoreCase("highest") )
    		return EventPriority.HIGHEST;
    	else if( strPriority.equalsIgnoreCase("high") )
    		return EventPriority.HIGH;
    	else if( strPriority.equalsIgnoreCase("normal") )
    		return EventPriority.NORMAL;
    	else if( strPriority.equalsIgnoreCase("low") )
    		return EventPriority.LOW;
    	else if( strPriority.equalsIgnoreCase("lowest") )
    		return EventPriority.LOWEST;
    	else
    		return EventPriority.HIGHEST;	// default
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
            	pm.registerEvents(entityListener, this);
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
        classList.add(HomeInvite.class);
        return classList;
    }
    
    @Override
    public com.avaje.ebean.EbeanServer getDatabase() {
    	// override method to use new Bukkit Persistance reimplemented ebean server
    	if( storage instanceof StorageEBeans ) {
    		StorageEBeans storageEbeans = (StorageEBeans) storage;
    		return storageEbeans.getDatabase().getDatabase();
    	}
    	else
    		return super.getDatabase();
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
	
	public PermissionSystem getPermissionSystem() { return perms; }

    @Override
    public ClassLoader getClassLoader() { return super.getClassLoader(); }
    
    public StrategyEngine getStrategyEngine() { return strategyEngine; }
    public Logger getLogger() { return log; }
    public String getLogPrefix() { return logPrefix; }
    public Storage getStorage() { return storage; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public WarmupManager getWarmupmanager() { return warmupManager; }
    public HomeSpawnUtils getUtil() { return spawnUtils; }
    public HomeInviteManager getHomeInviteManager() { return homeInviteManager; }
    public String getPluginName() { return pluginName; }
    public JarUtils getJarUtils() { return jarUtils; }
}