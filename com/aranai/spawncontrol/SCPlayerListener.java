package com.aranai.spawncontrol;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

import com.aranai.spawncontrol.config.Config;
import com.aranai.spawncontrol.config.ConfigOptions;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * Handle events for all Player related events
 * @author Timberjaw
 */
public class SCPlayerListener extends PlayerListener {
    private final SpawnControl plugin;
    private Config config;
    private static PermissionHandler permissionHandler;
    private boolean isPerm3 = false;

    public SCPlayerListener(SpawnControl instance) {
        plugin = instance;
        config = plugin.getConfig();
        
        Plugin permissionsPlugin = plugin.getServer().getPluginManager().getPlugin("Permissions");
        String permVersion = permissionsPlugin.getDescription().getVersion();
        if( permVersion.startsWith("3") ) {
        	isPerm3 = true;
        }
        
        permissionHandler = ((Permissions) permissionsPlugin).getHandler();
    }

    /** Get the spawn group the player is associated with.
     * 
     * @param p
     * @return
     */
    /*
    @SuppressWarnings("deprecation")
	private String getPlayerSpawnGroup(Player p)
    {
    	if( p == null )
    		return null;
    	
    	String world = p.getWorld().getName();
    	String group = null;
    	
    	
    	
    	if( isPerm3 ) {
			User user = permissionHandler.getUserObject(world, p.getName());
			LinkedHashSet<Entry> parents = user.getParents(world);
			for(Entry e : parents) {
				e.get
			}
    	}
    	else {
    		group = permissionHandler.getGroup(p.getWorld().getName(), p.getName());
    	}
		plugin.sendToGroupSpawn(Permissions.Security.getGroup(p.getWorld().getName(), p.getName()), p);
    	
		return null;
    }
    */
    
    public void onPlayerJoin(PlayerJoinEvent e)
    {
    	if(plugin.getHome(e.getPlayer().getName(), e.getPlayer().getWorld()) == null)
    	{
    		// Probably a new player
    		SpawnControl.log.info("[SpawnControl] Sending new player " + e.getPlayer().getName() + " to global spawn.");
    		
    		// Send player to global spawn
    		plugin.sendToSpawn(e.getPlayer());
    		
    		// Set home for player
    		plugin.setHome(e.getPlayer().getName(), plugin.getSpawn(e.getPlayer().getWorld()), "SpawnControl");
    	}
    	
    	int jb = plugin.getSetting("behavior_join");
    	if(jb != SpawnControl.Settings.JOIN_NONE)
    	{
	    	// Get player
	    	Player p = e.getPlayer();
	    	
	    	// Check for home
	    	SpawnControl.log.info("[SpawnControl] Attempting to respawn player "+p.getName()+" (joining).");
	    	
	    	switch(jb)
	    	{
	    		case SpawnControl.Settings.JOIN_HOME:
	    			plugin.sendHome(p);
	    			break;
	    		case SpawnControl.Settings.JOIN_GROUPSPAWN:
	    			if(plugin.usePermissions)
	    			{
	    				plugin.sendToGroupSpawn(Permissions.Security.getGroup(p.getWorld().getName(), p.getName()), p);
	    			}
	    			else
	    			{
	    				plugin.sendToSpawn(p);
	    			}
	    			break;
	    		case SpawnControl.Settings.JOIN_GLOBALSPAWN:
	    		default:
	    			plugin.sendToSpawn(p);
	    			break;
	    	}
    	}
    }
    
    public void onPlayerRespawn(PlayerRespawnEvent e)
    {
    	int db = plugin.getSetting("behavior_death");
    	if(db != SpawnControl.Settings.DEATH_NONE)
    	{
    		// Get player
	    	Player p = e.getPlayer();
	    	
	    	// Check for home
	    	SpawnControl.log.info("[SpawnControl] Attempting to respawn player "+p.getName()+" (respawning).");
	    	
	    	// Build respawn location
	    	Location l;
	    	
    		switch(db)
	    	{
	    		case SpawnControl.Settings.DEATH_HOME:
	    			l = plugin.getHome(p.getName(), p.getWorld());
	    			break;
	    		case SpawnControl.Settings.DEATH_GROUPSPAWN:
	    			if(plugin.usePermissions)
	    			{
	    				l = plugin.getGroupSpawn(Permissions.Security.getGroup(p.getWorld().getName(), p.getName()), p.getWorld());
	    			}
	    			else
	    			{
	    				l = plugin.getGroupSpawn("scglobal", p.getWorld());
	    			}
	    			break;
	    		case SpawnControl.Settings.DEATH_GLOBALSPAWN:
	    		default:
	    			l = plugin.getGroupSpawn("scglobal", p.getWorld());
	    			break;
	    	}
    		
    		if(l == null)
    		{
    			// Something has gone wrong
    			SpawnControl.log.warning("[SpawnControl] Could not find respawn for " + p.getName() + "!");
    			return;
    		}
    		else
    		{
    			// Set world
    			l.setWorld(p.getWorld());
    		}
    		
    		SpawnControl.log.info("[SpawnControl] DEBUG: Respawn Location: " + l.toString());
    		e.setRespawnLocation(l);
    	}
    }
    
    public boolean isExemptFromCooldowns(Player p, String cooldown)
    {
    	if(plugin.usePermissions)
    	{
    		return Permissions.Security.permission(p, "SpawnControl.CooldownExempt."+cooldown);
    	}
    	
    	return p.isOp();
    }
    
    public long cooldownLeft(Player p, String cooldown)
    {
    	// Check cooldown setting
    	int cooldownAmount = plugin.getSetting("cooldown_"+cooldown);
    	
    	if(cooldownAmount > 0 && !this.isExemptFromCooldowns(p, cooldown))
    	{
    		// Check cooldown status for player
    		return plugin.getCooldownRemaining(p, cooldown);
    	}
    	
    	return 0;
    }
    
    public void setCooldown(Player p, String cooldown)
    {
    	if(!this.isExemptFromCooldowns(p, cooldown))
    	{
    		plugin.setCooldown(p, cooldown);
    	}
    }
    
    public boolean canUseSpawn(Player p)
    {
    	if(plugin.usePermissions)
    	{
    		return Permissions.Security.permission(p, "SpawnControl.spawn.use");
    	}
    	
    	return true;
    }
    
    public boolean canUseSetSpawn(Player p)
    {
    	if(plugin.usePermissions)
    	{
    		return Permissions.Security.permission(p, "SpawnControl.spawn.set");
    	}
    	
    	return p.isOp();
    }
    
    public boolean canUseSetGroupSpawn(Player p)
    {
    	if(plugin.usePermissions)
    	{
    		return Permissions.Security.permission(p, "SpawnControl.groupspawn.set");
    	}
    	
    	// Disabled without group support
    	return false;
    }
    
    public boolean canUseGroupSpawn(Player p)
    {
    	if(plugin.usePermissions)
    	{
    		return Permissions.Security.permission(p, "SpawnControl.groupspawn.use");
    	}
    	
    	// Disabled without group support
    	return false;
    }
    
    public boolean canUseHomeBasic(Player p)
    {
    	if(plugin.usePermissions)
    	{
    		return Permissions.Security.permission(p, "SpawnControl.home.basic");
    	}
    	
    	return true;
    }
    
    public boolean canUseSetHomeBasic(Player p)
    {
    	if(plugin.usePermissions)
    	{
    		return Permissions.Security.permission(p, "SpawnControl.sethome.basic");
    	}
    	
    	return true;
    }
    
    public boolean canUseSetHomeProxy(Player p)
    {
    	if(plugin.usePermissions)
    	{
    		return Permissions.Security.permission(p, "SpawnControl.sethome.proxy");
    	}
    	
    	return p.isOp();
    }
    
    public boolean canUseScConfig(Player p)
    {
    	if(plugin.usePermissions)
    	{
    		return Permissions.Security.permission(p, "SpawnControl.config");
    	}
    	
    	return p.isOp();
    }
}