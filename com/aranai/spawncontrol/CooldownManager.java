/**
 * 
 */
package com.aranai.spawncontrol;

import java.util.Hashtable;

import org.bukkit.entity.Player;

import com.aranai.spawncontrol.config.ConfigOptions;

/** Class which manages player cooldowns.
 * 
 * @author morganm
 *
 */
public class CooldownManager {
	private final SpawnControl plugin;
	
    private Hashtable<String, Long> cooldowns;

    public CooldownManager(SpawnControl plugin) {
    	this.plugin = plugin;
    }
    
    private boolean isExemptFromCooldown(Player p, String cooldown) {
    	if( plugin.getPermissionHandler().has(p, SpawnControl.BASE_PERMISSION_NODE+"CooldownExempt."+cooldown) )
    		return true;
    	else
    		return false;
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
		if( isExemptFromCooldown(p, cooldownName) )
			return true;
		
		long cooldownTimeLeft = getCooldownRemaining(p, cooldownName);
		if(cooldownTimeLeft > 0)
		{
			p.sendMessage("Cooldown is in effect. You must wait " + cooldownTimeLeft + " seconds.");
			return true;
		}
		
		setCooldown(p, cooldownName);
		return false;
	}
	
    public void setCooldown(Player p, String cooldown)
    {
    	int cooldownAmount = plugin.getConfig().getInt(ConfigOptions.COOLDOWN_BASE + cooldown, 0);
    	
    	if(cooldownAmount > 0) {
    		cooldowns.put(p.getName()+"."+cooldown, System.currentTimeMillis());
    	}
    }
    
    /** Return the number of remaining seconds for a given cooldown.
     * 
     * @param p
     * @param cooldown
     * @return
     */
    public long getCooldownRemaining(Player p, String cooldown)
    {
    	long cooldownRemaining = 0;

    	int cooldownAmount = plugin.getConfig().getInt(ConfigOptions.COOLDOWN_BASE + cooldown, 0);
    	if( cooldownAmount == 0 )
    		return 0;
    	
    	String key = p.getName()+"."+cooldown;
    	Long cooldownStartTime = cooldowns.get(key);
    	if( cooldownStartTime != null )
    	{
    		// Compare time
    		long timeElapsed = (System.currentTimeMillis() - cooldownStartTime)/1000;
    		
    		if(timeElapsed > cooldownAmount)
    			cooldowns.remove(key);    					// cooldown expired, remote it
    		else
    			cooldownRemaining = cooldownAmount-timeElapsed;
    	}
    	
    	return cooldownRemaining;
    }
}
