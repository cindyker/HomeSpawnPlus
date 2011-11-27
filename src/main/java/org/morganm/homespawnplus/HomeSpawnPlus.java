package org.morganm.homespawnplus;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
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
    public static final Logger log = Logger.getLogger("HomeSpawnPlus");
    public static String logPrefix = "[HomeSpawnPlus]";
    
    public final static String YAML_CONFIG_ROOT_PATH = "plugins/HomeSpawnPlus/";
    public final static String YAML_BACKUP_FILE = YAML_CONFIG_ROOT_PATH + "backup.yml";
	public final static String BASE_PERMISSION_NODE = "hsp";
    
    private PermissionHandler permissionHandler;
    
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

    // Vault interface variables
    public static Permission vaultPermission = null;
    public static Economy vaultEconomy = null;

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
    
    /** Initialize permission system.
     * 
     */
    private void initPermissions() {
    	Debug.getInstance().debug("Initializing Permission system");
    	
    	if( !setupVaultPermissions() ) {
	        Plugin permissionsPlugin = getServer().getPluginManager().getPlugin("Permissions");
	        if( permissionsPlugin != null ) {
	        	permissionHandler = ((Permissions) permissionsPlugin).getHandler();
	        	
//	        	if( permissionsPlugin.getDescription().getVersion().startsWith("3") )
//	        		usePerm3 = true;
	        }
	        else {
		    	log.warning(logPrefix+" Permissions system not enabled, using isOP instead.");
	        }
    	}
    	else {
	    	log.info(logPrefix+" Vault plugin found, using Vault interface for Permissions");
    	}
    }
    
    /** Given a playerName, return the permissions group they are associated with (if any).
     * 
     * @param playerName
     * @return the group or null if no group
     */
    @SuppressWarnings("deprecation")
	public String getPlayerGroup(String world, String playerName) {
    	if( vaultPermission != null ) {
    		return vaultPermission.getPrimaryGroup(world, playerName);
    	}
    	else if( permissionHandler != null ) {
    		return permissionHandler.getGroup(world, playerName);
    	}
    	else {
    		return null;
    	}
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

    private Boolean setupVaultPermissions()
    {
    	Plugin vault = getServer().getPluginManager().getPlugin("Vault");
    	if( vault != null ) {
	        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
	        if (permissionProvider != null) {
	            vaultPermission = permissionProvider.getProvider();
	        }
    	}
    	// we don't print any errors on "else" because we just fall back to our own perms code
    	// at this point and no functionality is lost.
    	
        return (vaultPermission != null);
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
    
    private void unhookOtherCommands() {
    	UsurpCommandExecutor usurp = new UsurpCommandExecutor(this);
    	
    	List<String> commands = config.getStringList("usurpCommands", null);
    	if( commands != null ) {
	    	for(String command : commands) {
	        	PluginCommand cmd = getServer().getPluginCommand(command);
	        	if( cmd != null ) {
	        		log.info(logPrefix + " usurping command "+command+" as specified by usurpCommands config option");
		        	// TODO: "being nice" might be best to keep track of the "old" executor
		        	// and restore that if this plugin is unloaded. At this point, restoring
		        	// the old executor requires turning off the usurp config option and
		        	// restarting the server.
		        	cmd.setExecutor(usurp);
	        	}
	    	}
    	}
    }
    
    /** Private class which is used to re-route commands being processed by other plugins
     * to our plugin instead (if the admin enabled config flag to do so).
     * 
     * @author morganm
     *
     */
    private class UsurpCommandExecutor implements CommandExecutor {
    	private HomeSpawnPlus plugin;
    	
    	public UsurpCommandExecutor(HomeSpawnPlus plugin) {
    		this.plugin = plugin;
		}
    	
		@Override
		public boolean onCommand(CommandSender sender, Command command, String commandLabel,
				String[] args) {
			return plugin.onCommand(sender, command, commandLabel, args);
		}
    	
    }
    
    public void onEnable() {
    	boolean loadError = false;
    	
    	getConfig();
    	
    	instance = this;
    	pluginDescription = getDescription();
    	pluginName = pluginDescription.getName();
    	logPrefix = "[" + pluginName + "]";
    	
    	Debug.getInstance().init(log, logPrefix+"[DEBUG] ", false);
    	
    	org.morganm.homespawnplus.entity.Player.setServer(getServer());
    	
    	jarFile = getFile();
    	
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
    	unhookOtherCommands();
    	
        log.info( logPrefix + " version [" + pluginDescription.getVersion() + "] loaded" );
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
        classList.add(Version.class);
        return classList;
    }
    
    public boolean hasPermission(String worldName, String playerName, String permissionNode) {
    	if( playerName == null )
    		return false;
    	
    	// use Vault for permissions, if it was found
    	if( vaultPermission != null )
    		return vaultPermission.has(worldName, playerName, permissionNode);
    	
//    	log.info(logPrefix + " checking permission "+permissionNode+" for player "+p.getName());
    	if( permissionHandler != null )
    		return permissionHandler.has(worldName, playerName, permissionNode);
    	else
    	{
    		// ordinarilly we would try superperms here, but superperms doesn't seem to have
    		// any support for cross-world or offline player permission checks.  FAIL.
    		return false;
    	}
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
    	Player p = null;
    	
    	// console always has access
    	if( sender instanceof ConsoleCommandSender )
    		return true;
    	if( sender instanceof Player )
    		p = (Player) sender;
    	
    	// shouldn't happen, but if it does, something weird going on: deny access
    	if( p == null )
    		return false;
    	
    	// use Vault for permissions, if it was found
    	if( vaultPermission != null )
    		return vaultPermission.has(p, permissionNode);
    	
//    	log.info(logPrefix + " checking permission "+permissionNode+" for player "+p.getName());
    	if( permissionHandler != null ) 
    		return permissionHandler.has(p, permissionNode);
    	else
    	{
    		// try "superperms" - this covers DefaultPerms, bPerms, etc
    		if( p.hasPermission(permissionNode) )
    			return true;
    		
    		// no superperms?  if they are an op, always return true (maybe delete this and depend only on superperms in the future?) 
    		if( p.isOp() )
    			return true;
    		
    		// last-ditch, no superperms, no op; see if this was enabled as a "defaultPerm" in the config file 
    		List<String> defaultPerms = config.getStringList(ConfigOptions.DEFAULT_PERMISSIONS, null);
    		if( defaultPerms.contains(permissionNode) )
    			return true;
    		else
    			return false;
    	}
    }
    
    /** Return true if we are using Permission system and it is specifically Permissions 3.x
     * 
     * @return
     */
//    public boolean isUsePerm3() { return usePermissions && usePerm3; }

    @Override
    public ClassLoader getClassLoader() { return super.getClassLoader(); }
    
    public Storage getStorage() { return storage; }
    
//    public PermissionHandler getPermissionHandler() { return permissionHandler; }
    
    public CooldownManager getCooldownManager() { return cooldownManager; }
    
    public WarmupManager getWarmupmanager() { return warmupManager; }
    
    public HomeSpawnUtils getUtil() { return spawnUtils; }
    
    public String getPluginName() { return pluginName; }
    
    public File getJarFile() { return jarFile;	}
}