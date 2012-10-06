/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
package org.morganm.homespawnplus;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.morganm.homespawnplus.command.CommandConfig;
import org.morganm.homespawnplus.command.CommandRegister;
import org.morganm.homespawnplus.config.Config;
import org.morganm.homespawnplus.config.ConfigException;
import org.morganm.homespawnplus.config.ConfigFactory;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.HomeInvite;
import org.morganm.homespawnplus.entity.PlayerLastLocation;
import org.morganm.homespawnplus.entity.PlayerSpawn;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.entity.Version;
import org.morganm.homespawnplus.i18n.Colors;
import org.morganm.homespawnplus.i18n.Locale;
import org.morganm.homespawnplus.i18n.LocaleConfig;
import org.morganm.homespawnplus.i18n.LocaleFactory;
import org.morganm.homespawnplus.integration.dynmap.DynmapModule;
import org.morganm.homespawnplus.integration.multiverse.MultiverseIntegration;
import org.morganm.homespawnplus.integration.worldguard.WorldGuardIntegration;
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
import org.morganm.homespawnplus.storage.yaml.serialize.SerializablePlayerLastLocation;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializablePlayerSpawn;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableSpawn;
import org.morganm.homespawnplus.strategy.StrategyEngine;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.General;
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
    private org.morganm.homespawnplus.util.Logger hspLogger;
    
    public final static String YAML_CONFIG_ROOT_PATH = "plugins/HomeSpawnPlus/";
    public final static String YAML_BACKUP_FILE = YAML_CONFIG_ROOT_PATH + "backup.yml";
	public final static String BASE_PERMISSION_NODE = "hsp";
    
	static {
		ConfigurationSerialization.registerClass(SerializableHome.class, "Home");
		ConfigurationSerialization.registerClass(SerializableSpawn.class, "Spawn");
		ConfigurationSerialization.registerClass(SerializablePlayer.class, "Player");
		ConfigurationSerialization.registerClass(SerializableHomeInvite.class, "HomeInvite");
		ConfigurationSerialization.registerClass(SerializablePlayerLastLocation.class, "PlayerLastLocation");
		ConfigurationSerialization.registerClass(SerializablePlayerSpawn.class, "PlayerSpawn");
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
    private HSPPlayerListener playerListener;
    private HSPEntityListener entityListener;
    private JarUtils jarUtils;
	private int buildNumber = -1;
    private PluginDescriptionFile pluginDescription;
    private String pluginName;
    private Storage storage;
    private Locale locale;
    private Debug debug;
    private Metrics metrics;
    private MultiverseIntegration multiverse;
    private WorldGuardIntegration worldGuardIntegration;

    public Economy vaultEconomy = null;
    
    public HomeSpawnPlus() {
    	this.hspLogger = new org.morganm.homespawnplus.util.LoggerImpl(this);
    }
    
    /** Not your typical singleton pattern - this CAN return null in the event the plugin is unloaded. 
     * 
     * @return the singleton instance or null if there is none
     */
    public static HomeSpawnPlus getInstance() {
    	return instance;
    }
    
    /** Routine to detect other plugins that use the same commands as HSP and
     * often cause conflicts and create confusion.
     * 
     */
    private void detectAndWarn() {
    	// do nothing if warning is disabled
    	if( !getConfig().getBoolean(ConfigOptions.WARN_CONFLICTS, true) )
    		return;
    	
    	if( getServer().getPluginManager().getPlugin("Essentials") != null ) {
    		log.warning(logPrefix+" Essentials found. It is likely your HSP /home and /spawn commands will"
    				+ " end up going to Essentials instead.");
    		log.warning(logPrefix+" Also note that HSP can convert your homes from Essentials for you. Just"
    				+ " run the command \"/hspconvert essentials\" (must have hsp.command.admin permission)");
    		log.warning(logPrefix+" Set \"core.warnConflicts\" to false in your HSP config.yml to disable"
    				+ " this warning.");
    	}
    	
    	if( getServer().getPluginManager().getPlugin("CommandBook") != null ) {
    		log.warning(logPrefix+" CommandBook found. It is likely your HSP /home and /spawn commands will"
    				+ " end up going to CommandBook instead. Please add \"homes\" and"
    				+ " \"spawn-locations\" to your CommandBook config.yml \"components.disabled\" section.");
    		log.warning(logPrefix+" Set \"core.warnConflicts\" to false in your HSP config.yml to disable"
    				+ " this warning.");
    	}
    }

    static long startupBegin = 0L;
    static long startupTimer = 0L;
    /** Start the timer, print a message.
     * 
     * @param s
     */
    private void debugStartTimer(String s) {
    	startupTimer = System.currentTimeMillis();
    	debug.debug("[Startup Timer] starting "+s+" (t+", System.currentTimeMillis()-startupBegin+")");
    }
    /** End the timer, print the timing information.
     * 
     * @param s
     */
    private void debugEndTimer(String s) {
    	debug.debug("[Startup Timer] "+s+" finished in ", System.currentTimeMillis()-startupTimer, "ms");
    }
    
    @Override
    public void onEnable() {
    	startupBegin = System.currentTimeMillis();
    	boolean loadError = false;
    	instance = this;
    	
    	getConfig();
    	
    	General.getInstance().setLogger(log);
    	General.getInstance().setLogPrefix(logPrefix+" ");
    	
    	pluginDescription = getDescription();
    	pluginName = pluginDescription.getName();
    	
    	Debug.getInstance().init(log, logPrefix, "plugins/HomeSpawnPlus/debug.log", false);
		debug = Debug.getInstance();
    	jarUtils = new JarUtils(this, getFile(), log, logPrefix);
		buildNumber = jarUtils.getBuildNumber();

    	// load our configuration and database
    	try {
        	strategyEngine = new StrategyEngine(this);
        	
        	debugStartTimer("config");
    		loadConfig(false);
    		updateConfigDefaultFile();
    		debugEndTimer("config");
        	
        	debugStartTimer("database");
            initializeDatabase();
    		debugEndTimer("database");
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
    	
    	debugStartTimer("permissions");
    	initPermissions();
    	debugEndTimer("permissions");
    	
    	debugStartTimer("economy");
    	setupVaultEconomy();
    	debugEndTimer("economy");
    	
    	debugStartTimer("HSP managers");
    	cooldownManager = new CooldownManager(this);
    	warmupManager = new WarmupManager(this);
    	spawnUtils = new HomeSpawnUtils(this);
    	homeInviteManager = new HomeInviteManager(this);
    	debugEndTimer("HSP managers");
    	
    	debugStartTimer("Bukkit events");
        final PluginManager pm = getServer().getPluginManager();

    	playerListener = new HSPPlayerListener(this);
    	pm.registerEvents(playerListener, this);		// bukkit annotation events
    	playerListener.registerEvents();				// runtime priority events
    	
    	pm.registerEvents(new HSPWorldListener(this), this);

    	entityListener = new HSPEntityListener(this);
        hookWarmups();
    	debugEndTimer("Bukkit events");
        
    	debugStartTimer("commands");
    	CommandConfig config = new CommandConfig(getLog());
    	ConfigurationSection section = getConfig().getConfigurationSection("commands");
    	config.loadConfig(section);
    	CommandRegister register = new CommandRegister(this);
    	register.setCommandConfig(config);
    	register.registerAllCommands();
    	debugEndTimer("commands");
    	
    	debugStartTimer("Plugin integrations");
    	// hook multiverse, if available
    	multiverse = new MultiverseIntegration(this);
    	multiverse.onEnable();
    	worldGuardIntegration = new WorldGuardIntegration(this);
    	worldGuardIntegration.init();
    	debugEndTimer("Plugin integrations");
        
    	debugStartTimer("strategies");
    	processStrategyConfig();
    	detectAndWarn();
    	debugEndTimer("strategies");
    	
    	debugStartTimer("metrics");
        // Load up the Plugin metrics
        try {
            metrics = new Metrics(this);
            metrics.findCustomData(this);
            metrics.start();
        } catch (IOException e) {
            // ignore exception
        }
    	debugEndTimer("metrics");
    	
    	if( getConfig().getBoolean(ConfigOptions.DYNMAP_INTEGRATION_ENABLED, false) ) {
        	debugStartTimer("dynmap");
    		DynmapModule dynmap = new DynmapModule(this);
    		dynmap.init();
        	debugEndTimer("dynmap");
    	}
    	
		log.info(logPrefix + " version "+pluginDescription.getVersion()+", build "+buildNumber+" is enabled");
    	debug.debug("[Startup Timer] HSP total initialization time: ", System.currentTimeMillis()-startupBegin, "ms");
    }
    
    @Override
    public void onDisable() {
    	// unhook multiverse (if needed)
    	multiverse.onDisable();
    	
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
    
//    @Override
//    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
//    	return cmdProcessor.onCommand(sender, command, commandLabel, args);
//    }
    
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
    
    public void loadConfig(boolean processStrategies) throws ConfigException, IOException {
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
		
		if( processStrategies )
			processStrategyConfig();
    	
    	General.getInstance().setLocale(getLocale());
    }
    
    private void processStrategyConfig() {
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
        classList.add(PlayerSpawn.class);
        classList.add(PlayerLastLocation.class);
        return classList;
    }
    
    @Override
    public com.avaje.ebean.EbeanServer getDatabase() {
    	// override method to use new Bukkit Persistance reimplemented ebean server
    	if( storage instanceof StorageEBeans ) {
    		StorageEBeans storageEbeans = (StorageEBeans) storage;
    		if( storageEbeans.usePersistanceReimplemented() ) {
    			return storageEbeans.getPersistanceReimplementedDatabase().getDatabase();
    		}
    	}

		return super.getDatabase();
    }
    
    public boolean hasPermission(String worldName, String playerName, String permissionNode) {
    	boolean result = perms.has(worldName, playerName, permissionNode);
    	
    	// if using OPS system, support legacy HSP "defaultPermission" setting
    	if( !result && perms.getSystemInUse() == PermissionSystem.Type.OPS ) {
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
    	if( !result && perms.getSystemInUse() == PermissionSystem.Type.OPS ) {
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
    public org.morganm.homespawnplus.util.Logger getLog() { return hspLogger; }
    public java.util.logging.Logger getLogger() { return log; }
    public String getLogPrefix() { return logPrefix; }
    public Storage getStorage() { return storage; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public WarmupManager getWarmupmanager() { return warmupManager; }
    public HomeSpawnUtils getUtil() { return spawnUtils; }
    public HomeInviteManager getHomeInviteManager() { return homeInviteManager; }
    public String getPluginName() { return pluginName; }
    public JarUtils getJarUtils() { return jarUtils; }
    public MultiverseIntegration getMultiverseIntegration() { return multiverse; }
    public WorldGuardIntegration getWorldGuardIntegration() { return worldGuardIntegration; }
}
