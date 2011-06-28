package com.aranai.spawncontrol;


public class OldSpawnControl {
	/*
    private final SCPlayerListener playerListener = new SCPlayerListener(this);
    private final SCWorldListener worldListener = new SCWorldListener(this);
    
    protected Connection conn;
    public final static String directory = "plugins/SpawnControl";
    public final static String db = "jdbc:sqlite:" + SpawnControl.directory + File.separator + "spawncontrol.db";
    
    // Schema version
    public static final int SchemaVersion = 1;
    
    // SQL Strings
    protected static String SQLCreatePlayersTable = "CREATE TABLE `players` (`id` INTEGER PRIMARY KEY, `name` varchar(32) NOT NULL, "
		+"`world` varchar(50), `x` REAL, `y` REAL, `z` REAL, `r` REAL, `p` REAL, "
		+"`updated` INTEGER, `updated_by` varchar(32));";
    protected static String SQLCreatePlayersIndex = "CREATE UNIQUE INDEX playerIndex on `players` (`name`,`world`);";
    protected static String SQLCreateGroupsTable = "CREATE TABLE `groups` (`id` INTEGER PRIMARY KEY, `name` varchar(32) NOT NULL, "
		+"`world` varchar(50), `x` REAL, `y` REAL, `z` REAL, `r` REAL, `p` REAL, "
		+"`updated` INTEGER, `updated_by` varchar(32));";
    protected static String SQLCreateGroupsIndex = "CREATE UNIQUE INDEX groupIndex on `groups` (`name`,`world`);";
    
    // Settings
    public static final class Settings {
    	public static final int UNSET = -1;
    	public static final int NO = 0;
    	public static final int YES = 1;
    	public static final int DEATH_NONE = 0;
    	public static final int DEATH_HOME = 1;
    	public static final int DEATH_GROUPSPAWN = 2;
    	public static final int DEATH_GLOBALSPAWN = 3;
    	public static final int JOIN_NONE = 0;
    	public static final int JOIN_HOME = 1;
    	public static final int JOIN_GROUPSPAWN = 2;
    	public static final int JOIN_GLOBALSPAWN = 3;
    	public static final int GLOBALSPAWN_DEFAULT = 0;
    	public static final int GLOBALSPAWN_OVERRIDE = 1;
    	public static final int SPAWN_GLOBAL = 0;
    	public static final int SPAWN_GROUP = 1;
    	public static final int SPAWN_HOME = 2;
    }

    public static final List<String> validSettings = Arrays.asList(
    		"enable_home", "enable_groupspawn", "enable_globalspawn",
    		"behavior_join", "behavior_death", "behavior_globalspawn", "behavior_spawn",
    		"cooldown_home", "cooldown_sethome", "cooldown_spawn", "cooldown_groupspawn" 
    );
    */

    public void onEnable() {
    	
    	/*
    	// Initialize active player ids and homes
        this.activePlayerIds = new Hashtable<String,Integer>();
        this.homes = new Hashtable<Integer,Location>();
        
        // Initialize active group ids and group spawns
        this.activeGroupIds = new Hashtable<String,Integer>();
        this.groupSpawns = new Hashtable<Integer,Location>();
        
        // Initialize respawn list
        this.respawning = new Hashtable<String,Boolean>();
        
        // Initialize cooldown list
        this.cooldowns = new Hashtable<String,Long>();
        
        // Initialize last setting info
        this.lastSetting = "";
        this.lastSettingValue = -1;
    	
    	// Make sure we have a local folder for our database and such
        if (!new File(directory).exists()) {
            try {
                (new File(directory)).mkdir();
            } catch (Exception e) {
                SpawnControl.log.log(Level.SEVERE, "[SpawnControl]: Unable to create spawncontrol/ directory.");
            }
        }
        */
        
        /*
        // Initialize permissions system
    	Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

    	if(SpawnControl.Permissions == null) {
    	    if(test != null) {
    	    	SpawnControl.Permissions = (Permissions)test;
    	    	this.usePermissions = true;
    	    } else {
    	    	log.warning("[SpawnControl] Permissions system not enabled, using isOP instead.");
    	    }
    	}
    	*/
        
    }
    
    /*
    // Initialize database
    private void initDB()
    {
    	ResultSet rs = null;
    	Statement st = null;
    	
    	try
        {
    		Class.forName("org.sqlite.JDBC");
        	conn = DriverManager.getConnection(db);
        	
        	DatabaseMetaData dbm = conn.getMetaData();
        	
        	// Check players table
            rs = dbm.getTables(null, null, "players", null);
            if (!rs.next())
            {
            	// Create table
            	log.info("[SpawnControl]: Table 'players' not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute(SpawnControl.SQLCreatePlayersTable);
                st.execute(SpawnControl.SQLCreatePlayersIndex);
                conn.commit();
                
                log.info("[SpawnControl]: Table 'players' created.");
            }
            
            // Check groups table
            rs = dbm.getTables(null, null, "groups", null);
            if (!rs.next())
            {
            	// Create table
            	log.info("[SpawnControl]: Table 'groups' not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute(SpawnControl.SQLCreateGroupsTable);
                st.execute(SpawnControl.SQLCreateGroupsIndex);
                conn.commit();
                
                log.info("[SpawnControl]: Table 'groups' created.");
            }
            
            // Check settings table
            boolean needSettings = false;
            rs = dbm.getTables(null, null, "settings", null);
            if (!rs.next())
            {
            	// Create table
            	needSettings = true;
            	System.out.println("[SpawnControl]: Table 'settings' not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute("CREATE TABLE `settings` (`setting` varchar(32) PRIMARY KEY, `value` INT, "
                		+"`updated` INTEGER, `updated_by` varchar(32));");
                conn.commit();
                
                log.info("[SpawnControl]: Table 'settings' created.");
            }
        	
	        rs.close();
	        conn.close();
	        
	        if(needSettings)
	        {
	            // Insert default settings
		        this.setSetting("enable_home", Settings.YES, "initDB");
		        this.setSetting("enable_groupspawn", Settings.YES, "initDB");
		        this.setSetting("enable_globalspawn", Settings.YES, "initDB");
		        this.setSetting("behavior_death", Settings.DEATH_GLOBALSPAWN, "initDB");
		        this.setSetting("behavior_join", Settings.JOIN_NONE, "initDB");
		        this.setSetting("behavior_globalspawn", Settings.GLOBALSPAWN_DEFAULT, "initDB");
		        this.setSetting("behavior_spawn", Settings.SPAWN_GLOBAL, "initDB");
		        this.setSetting("schema_version", SpawnControl.SchemaVersion, "initDB");
		        this.setSetting("cooldown_home", 0, "initDB");
		        this.setSetting("cooldown_sethome", 0, "initDB");
		        this.setSetting("cooldown_groupspawn", 0, "initDB");
		        this.setSetting("cooldown_spawn", 0, "initDB");
	        }
	        
	        // Check schema version
	    	int sv = this.getSetting("schema_version");
	    	if(sv < SpawnControl.SchemaVersion)
	    	{
	    		SCUpdater.run(sv, this);
	    	}
        }
        catch(SQLException e)
        {
        	// ERROR
        	System.out.println("[initDB] DB ERROR - " + e.getMessage() + " | SQLState: " + e.getSQLState() + " | Error Code: " + e.getErrorCode());
        }
        catch(Exception e)
        {
        	// Error
        	System.out.println("Error: " + e.getMessage());
        	e.printStackTrace();
        }
    }
    */

}
