package com.aranai.spawncontrol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.aranai.spawncontrol.config.Config;
import com.aranai.spawncontrol.config.ConfigFactory;
import com.aranai.spawncontrol.storage.Storage;
import com.aranai.spawncontrol.storage.StorageException;
import com.aranai.spawncontrol.storage.StorageFactory;
import com.nijiko.permissions.PermissionHandler;
import com.nijiko.permissions.User;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * SpawnControl for Bukkit
 *
 * @author morganm, Timberjaw
 */
public class SpawnControl extends JavaPlugin {
    public static Logger log = Logger.getLogger("SpawnControl");;
    
    public final static String YAML_CONFIG_ROOT_PATH = "plugins/mSpawnControl/";
	public final static String BASE_PERMISSION_NODE = "SpawnControl";
    
    private Storage storage;
 
    // Permissions
    @Deprecated
    public static Permissions Permissions = null;
    
    public boolean usePermissions = false;
    private PermissionHandler permissionHandler;
    private Config config;
    
    // Cache variables
    private Hashtable<String,Integer> activePlayerIds;
    private Hashtable<Integer,Location> homes;
    private Hashtable<String,Integer> activeGroupIds;
    private Hashtable<Integer,Location> groupSpawns;
    private Hashtable<String,Boolean> respawning;
    private Hashtable<String,Long> cooldowns;
    private String lastSetting;
    private int lastSettingValue;
    
    // singleton instance - not declared final as the plugin can be reloaded, and the instance
    // will change to the new plugin.  But at most there will always be only one plugin object
    // loaded.
    private static SpawnControl instance;
    
    private final SpawnControlPlugin spawnControlPlugin;
    private final Server server;
    private final PluginDescriptionFile pluginDescription;
    
    /** Private constructor guarantees Singleton instance.
     */
    private SpawnControl(SpawnControlPlugin scp) {
    	spawnControlPlugin = scp;
    	server = scp.getServer();
    	pluginDescription = scp.getDescription();
    }
    
    /** This CAN return null in the event the plugin is unloaded. 
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
    private void initPerm() {
        Plugin permissionsPlugin = server.getPluginManager().getPlugin("Permissions");
        if( permissionsPlugin != null )
        	permissionHandler = ((Permissions) permissionsPlugin).getHandler();
        else
	    	log.warning("[SpawnControl] Permissions system not enabled, using isOP instead.");
    }
    
    public void onEnable() {
    	boolean loadError = false;
    	
    	log = Logger.getLogger("Minecraft");
    	
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
    	
    	initPerm();
    	
        PluginManager pm = server.getPluginManager();
    	SCPlayerListener playerListener = new SCPlayerListener(this);
        
    	// Register our events
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Highest, spawnControlPlugin);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, spawnControlPlugin);
        pm.registerEvent(Event.Type.WORLD_LOAD, new SCWorldListener(this), Priority.Monitor, spawnControlPlugin);
        
        log.info( "["+pluginDescription.getName()+"] version [" + pluginDescription.getVersion() + "] loaded" );
    }
    
    public void onDisable() {
    	log.info( "[SpawnControl] version [" + pluginDescription.getVersion() + "] unloaded" );
    }
    
    /** Return true if the given player has access to the given permission node.
     * 
     * @param p
     * @param permissionNode
     * @return
     */
    public boolean hasPermission(Player p, String permissionNode) {
    	return permissionHandler.has(p, permissionNode);
    }
    
	/** Utility method for making sure a cooldown is available before we execute a
	 * command.  If the cooldown is available, this will update the cooldown to the current
	 * time (thus starting the cooldown).
	 * 
	 * It also writes a message to the player letting them know they are still in cooldown.
	 * 
	 * @param p
	 * @param cooldownName
	 * @return true if cooldown is available, false if currently in cooldown period
	 */
	public boolean cooldownCheck(Player p, String cooldownName) {
		/*
		HashMap<String, Long> playerCooldowns = cooldowns.get(playerName);
		if( playerCooldowns == null ) {
			playerCooldowns = new HashMap<String, Long>();
			cooldowns.put(playerName, playerCooldowns);
		}
		
		long cooldownTimeLeft = playerCooldowns.get(cooldown);
		*/
		
		long cooldownTimeLeft = getCooldownRemaining(p, cooldownName);
		if(cooldownTimeLeft > 0)
		{
			p.sendMessage("Cooldown is in effect. You must wait " + cooldownTimeLeft + " seconds.");
			return true;
		}
		
		setCooldown(p, cooldownName);
		return false;
	}
	
    // Get timestamp
    public int getTimeStamp()
    {
    	return (int) (System.currentTimeMillis() / 1000L);
    }
    
    // Mark as respawning
    public void markPlayerRespawning(String name) { this.markPlayerDoneRespawning(name); this.respawning.put(name, true); }
    // Mark as done respawning
    public void markPlayerDoneRespawning(String name) { this.respawning.remove(name); }
    // Check to see if the player is respawning
    public boolean isPlayerRespawning(String name) { return this.respawning.containsKey(name); }
    
    
    // Get setting
    public int getSetting(String name)
    {
    	Connection conn = null;
    	PreparedStatement ps = null;
        ResultSet rs = null;
        int value = -1;
        
        if(this.lastSetting.equals(name))
        {
        	return this.lastSettingValue;
        }
		
		// Get from database
		try
        {
    		Class.forName("org.sqlite.JDBC");
        	conn = DriverManager.getConnection(db);
        	ps = conn.prepareStatement("SELECT * FROM `settings` WHERE `setting` = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
             
            while (rs.next()) { value = rs.getInt("value"); this.lastSetting = name; this.lastSettingValue = value; }
        }
        catch(Exception e)
        {
        	// Error
        	SpawnControl.log.warning("[SpawnControl] Could not get setting '"+name+"': " + e.getMessage());
        }
        finally
        {
        	if(conn != null) { try { conn.close(); } catch(Exception e) { e.printStackTrace(); } }
        }
        
        return value;
    }
    
    // Set setting
    public boolean setSetting(String name, int value, String setter)
    {
        boolean success = true;
        
        try
        {
	    	Class.forName("org.sqlite.JDBC");
	    	Connection conn = DriverManager.getConnection(db);
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement("REPLACE INTO `settings` (`setting`,`value`,`updated`,`updated_by`) VALUES (?, ?, ?, ?);");
	        ps.setString(1, name);
	        ps.setInt(2, value);
	        ps.setInt(3, this.getTimeStamp());
	        ps.setString(4, setter);
	        ps.execute();
	        conn.commit();
	        conn.close();
	        
	        if(this.lastSetting.equals(name))
	        {
	        	this.lastSetting = "";
	        	this.lastSettingValue = -1;
	        }
        }
        catch(Exception e)
        {
        	SpawnControl.log.severe("[SpawnControl] Failed to save setting '"+name+"' with value '"+value+"'");
        	success = false;
        }
        
    	return success;
    }
    
    // Spawn
    public void sendToSpawn(Player p)
    {
    	this.sendToGroupSpawn("scglobal", p);
    }
    
    // Set spawn
    public boolean setSpawn(Location l, String setter)
    {
    	return this.setGroupSpawn("scglobal", l, setter);
    }
    
    // Get spawn
    public Location getSpawn(World world)
    {
    	return this.getGroupSpawn("scglobal", world);
    }
    
    // Home
    public void sendHome(Player p)
    {
    	// Check for home
    	String nameHash = p.getName() + "-" + p.getWorld().getName();
    	if(!this.activePlayerIds.contains(nameHash))
    	{
    		if(!this.getPlayerData(p.getName(), p.getWorld()))
    		{
    			// No home available, use global
    			this.sendToSpawn(p);
    			return;
    		}
    	}
    	
    	// Teleport to home
    	p.teleport(this.homes.get(this.activePlayerIds.get(nameHash)));
    }
    
    // Get home
    public Location getHome(String name, World world)
    {
    	// Check for home
    	String nameHash = name + "-" + world.getName();
    	if(!this.activePlayerIds.contains(nameHash))
    	{
    		if(this.getPlayerData(name, world))
    		{
    			// Found home!
    			return this.homes.get(this.activePlayerIds.get(nameHash));
    		}
    	}
    	
    	return null;
    }
    
    // Sethome
    public boolean setHome(String name, Location l, String updatedBy)
    {
    	Connection conn = null;
    	PreparedStatement ps = null;
        Boolean success = false;
		
		// Save to database
		try
        {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(db);
			conn.setAutoCommit(false);
			ps = conn.prepareStatement("REPLACE INTO `players` (id, name, world, x, y, z, r, p, updated, updated_by) VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			ps.setString(1, name);
			ps.setString(2, l.getWorld().getName());
			ps.setDouble(3, l.getX());
			ps.setDouble(4, l.getY());
			ps.setDouble(5, l.getZ());
			ps.setFloat(6, l.getYaw());
			ps.setFloat(7, l.getPitch());
			ps.setInt(8, this.getTimeStamp());
			ps.setString(9, updatedBy);
			ps.execute();
			conn.commit();
        	conn.close();
        	
        	success = true;
        }
        catch(SQLException e)
        {
        	// ERROR
        	System.out.println("[setHome] DB ERROR - " + e.getMessage() + " | SQLState: " + e.getSQLState() + " | Error Code: " + e.getErrorCode());
        }
        catch(Exception e)
        {
        	// Error
        	System.out.println("Error: " + e.getMessage());
        	e.printStackTrace();
        }
        
        if(success)
        {
        	// Update local cache
        	this.getPlayerData(name, l.getWorld());
        }
        
        return success;
    }
    
    // Group spawn
    public void sendToGroupSpawn(String group, Player p)
    {
    	// Check for spawn
    	String groupHash = group + "-" + p.getWorld().getName();
    	if(!this.activeGroupIds.contains(groupHash))
    	{
    		if(!this.getGroupData(group, p.getWorld()))
    		{
    			if(group.equals("scglobal"))
    			{
    				// No global spawn found, set one
    				this.setGroupSpawn("scglobal", p.getWorld().getSpawnLocation(), "sendToGroupSpawn");
    			}
    			else
    			{
	    			// No group spawn available, use global
	    			this.sendToSpawn(p);
	    			return;
    			}
    		}
    	}
    	
    	// Teleport to home
    	p.teleport(this.groupSpawns.get(this.activeGroupIds.get(groupHash)));
    }
    
    // Set group spawn
    public boolean setGroupSpawn(String group, Location l, String updatedBy)
    {
    	Connection conn = null;
    	PreparedStatement ps = null;
        Boolean success = false;
		
		// Save to database
		try
        {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(db);
			conn.setAutoCommit(false);
			ps = conn.prepareStatement("REPLACE INTO `groups` (id, name, world, x, y, z, r, p, updated, updated_by) VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			ps.setString(1, group);
			ps.setString(2, l.getWorld().getName());
			ps.setDouble(3, l.getX());
			ps.setDouble(4, l.getY());
			ps.setDouble(5, l.getZ());
			ps.setFloat(6, l.getYaw());
			ps.setFloat(7, l.getPitch());
			ps.setInt(8, this.getTimeStamp());
			ps.setString(9, updatedBy);
			ps.execute();
			conn.commit();
        	conn.close();
        	
        	success = true;
        }
        catch(SQLException e)
        {
        	// ERROR
        	System.out.println("[setGroupSpawn] DB ERROR - " + e.getMessage() + " | SQLState: " + e.getSQLState() + " | Error Code: " + e.getErrorCode());
        }
        catch(Exception e)
        {
        	// Error
        	System.out.println("Error: " + e.getMessage());
        	e.printStackTrace();
        }
        
        if(success)
        {
        	// Update local cache
        	this.getGroupData(group, l.getWorld());
        }
        
        return success;
    }

    /** For any defined spawn groups we have setup, check to see if if the given
     * Player is in any of them.
     * 
     * @param world
     * @param playerName
     * @return the spawn group the player is in or null if none
     */
    public String getSpawnGroupName(String world, String playerName)
    {
    	User user = permissionHandler.getUserObject(world, playerName);
    	for(String group : activeGroupIds.keySet()) {
    		if( user.inGroup(world, group) )
    			return group;
    	}
    	
    	// if we make it here, the user is not in any of our defined spawn groups
    	return null;
    }
    
    // Get group spawn
    public Location getGroupSpawn(String group, World world)
    {
    	// Check for spawn
    	if(group.equals("Default"))
    	{
    		group = "scglobal";
    	}
    	
    	// Include group world in key
    	String groupHash = group + "-" + world.getName();
    	
    	if(this.activeGroupIds.contains(groupHash) || this.getGroupData(group, world))
    	{
    		return this.groupSpawns.get(this.activeGroupIds.get(groupHash));
    	}
    	
    	SpawnControl.log.warning("[SpawnControl] Could not find or load group spawn for '"+group+"'!");
    	
    	return null;
    }
    
    // Utility
    private boolean getPlayerData(String name, World world)
    {
    	Connection conn = null;
    	PreparedStatement ps = null;
        ResultSet rs = null;
        Boolean success = false;
        Integer id = 0;
		
		// Get from database
		try
        {
    		Class.forName("org.sqlite.JDBC");
        	conn = DriverManager.getConnection(db);
        	//conn.setAutoCommit(false);
        	ps = conn.prepareStatement("SELECT * FROM `players` WHERE `name` = ? AND `world` = ?");
            ps.setString(1, name);
            ps.setString(2, world.getName());
            rs = ps.executeQuery();
            //conn.commit();
             
             while (rs.next()) {
                 success = true;
                 String nameHash = name + "-" + world.getName();
                 this.activePlayerIds.put(nameHash, id);
                 Location l = new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("r"), rs.getFloat("p"));
                 this.homes.put(id, l);
             }
        	conn.close();
        }
        catch(SQLException e)
        {
        	// ERROR
        	System.out.println("[getPlayerData] DB ERROR - " + e.getMessage() + " | SQLState: " + e.getSQLState() + " | Error Code: " + e.getErrorCode());
        }
        catch(Exception e)
        {
        	// Error
        	System.out.println("Error: " + e.getMessage());
        	e.printStackTrace();
        }
        
        return success;
    }
    
    public boolean getGroupData(String name, World world)
    {
    	Connection conn = null;
    	PreparedStatement ps = null;
        ResultSet rs = null;
        Boolean success = false;
        Integer id = 0;
		
		// Get from database
		try
        {
    		Class.forName("org.sqlite.JDBC");
        	conn = DriverManager.getConnection(db);
        	ps = conn.prepareStatement("SELECT * FROM `groups` WHERE `name` = ? AND `world` = ?");
            ps.setString(1, name);
            ps.setString(2, world.getName());
            rs = ps.executeQuery();
             
             while (rs.next()) {
                 success = true;
                 String nameHash = name + "-" + world.getName();
                 this.activeGroupIds.put(nameHash, id);
                 Location l = new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("r"), rs.getFloat("p"));
                 this.groupSpawns.put(id, l);
             }
        	conn.close();
        }
        catch(SQLException e)
        {
        	// ERROR
        	System.out.println("[getGroupData] DB ERROR - " + e.getMessage() + " | SQLState: " + e.getSQLState() + " | Error Code: " + e.getErrorCode());
        }
        catch(Exception e)
        {
        	// Error
        	System.out.println("Error: " + e.getMessage());
        	e.printStackTrace();
        }
        
        return success;
    }
    
    public void setCooldown(Player p, String cooldown)
    {
    	String key = p.getName()+"."+cooldown;
    	long cooldownAmount = this.getSetting("cooldown_"+cooldown);
    	
    	if(cooldownAmount > 0)
    	{
    		cooldowns.put(key, System.currentTimeMillis());
    	}
    }
    
    public long getCooldownRemaining(Player p, String cooldown)
    {
    	String key = p.getName()+"."+cooldown;
    	long cooldownAmount = this.getSetting("cooldown_"+cooldown);
    	
    	if(cooldowns.containsKey(key))
    	{
    		// Compare time
    		long timeElapsed = (System.currentTimeMillis() - cooldowns.get(key))/1000;
    		
    		if(timeElapsed > cooldownAmount)
    		{
    			// Remove cooldown
    			cooldowns.remove(key);
    		}
    		else
    		{
    			// Return number of seconds left
    			return cooldownAmount-timeElapsed;
    		}
    	}
    	
    	return 0;
    }
    
    public void importConfig()
    {
    	File cf = new File(directory+"/spawncontrol-players.properties");
    	
    	if(cf.exists())
    	{
    		// Attempt import
            BufferedReader reader = null;

            try
            {
                reader = new BufferedReader(new FileReader(cf));
                String text = null;

                // Read a line
                while ((text = reader.readLine()) != null)
                {
                	// Skip if comment
                	if(!text.startsWith("#"))
                	{
                		// Format: Timberjaw=-86.14281646837361\:75.0\:233.43342838872454\:168.00002\:17.40001
                		text = text.replaceAll("\\\\", "");
                		String[] parts = text.split("=");
                		String name = parts[0];
                		String[] coords = parts[1].split(":");
                		Location l = new Location(null,
                				Double.parseDouble(coords[0]),
                				Double.parseDouble(coords[1]),
                				Double.parseDouble(coords[2]),
                				Float.parseFloat(coords[3]),
                				Float.parseFloat(coords[4]));
                		
                		// Set home
                		this.setHome(name, l, "ConfigImport");
                		
                		log.info("[SpawnControl] Found home for '"+name+"' at: "+l.getX()+","+l.getY()+","+l.getZ()+","+l.getYaw()+","+l.getPitch());
                	}
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    if (reader != null)
                    {
                        reader.close();
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
    	}
    }
    
    public void importGroupConfig()
    {
    	File cf = new File(directory+"/spawncontrol-groups.properties");
    	
    	if(cf.exists())
    	{
    		// Attempt import
            BufferedReader reader = null;

            try
            {
                reader = new BufferedReader(new FileReader(cf));
                String text = null;

                // Read a line
                while ((text = reader.readLine()) != null)
                {
                	// Skip if comment
                	if(!text.startsWith("#"))
                	{
                		// Format: admins=-56.50158762045817:12.0:265.4291449731157
                		text = text.replaceAll("\\\\", "");
                		String[] parts = text.split("=");
                		String name = parts[0];
                		String[] coords = parts[1].split(":");
                		Location l = new Location(this.getServer().getWorlds().get(0),
                				Double.parseDouble(coords[0]),
                				Double.parseDouble(coords[1]),
                				Double.parseDouble(coords[2]),
                				0.0f,
                				0.0f);
                		
                		// Set home
                		this.setGroupSpawn(name, l, "ConfigImport");
                		
                		log.info("[SpawnControl] Found group spawn for '"+name+"' at: "+l.getX()+","+l.getY()+","+l.getZ()+","+l.getYaw()+","+l.getPitch());
                	}
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    if (reader != null)
                    {
                        reader.close();
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
    	}
    }
}