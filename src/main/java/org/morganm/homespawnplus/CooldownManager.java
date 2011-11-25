/**
 * 
 */
package org.morganm.homespawnplus;

import java.util.Hashtable;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.morganm.homespawnplus.config.ConfigOptions;


/** Class which manages player cooldowns.
 * 
 * @author morganm
 *
 */
public class CooldownManager {
	@SuppressWarnings("unused")
	private static final Logger log = HomeSpawnPlus.log;
	
	private final HomeSpawnPlus plugin;
	@SuppressWarnings("unused")
	private final String logPrefix;
	
    private Hashtable<String, Long> cooldowns;

    public CooldownManager(HomeSpawnPlus plugin) {
    	this.plugin = plugin;
    	this.logPrefix = HomeSpawnPlus.logPrefix;
    	
    	cooldowns = new Hashtable<String, Long>();
    }
    
    private boolean isExemptFromCooldown(Player p, String cooldown) {
    	if( plugin.hasPermission(p, HomeSpawnPlus.BASE_PERMISSION_NODE+".CooldownExempt."+cooldown) )
    		return true;
    	else
    		return false;
    }
    
	/** Utility method for making sure a cooldown is available before we execute a
	 * command.  
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
			plugin.getUtil().sendMessage(p, "Cooldown is in effect. You must wait " + cooldownTimeLeft + " seconds.");
			return false;
		}
		
		// no longer update cooldown here, but require an explicit call to setCooldown() instead.
		// this forces the interface to work around a bug where a cooldown gets applied
		// at the start of a command even though the command aborts due to an error condition
		// (such as player arguments being wrong, etc).
//		setCooldown(p, cooldownName);
		return true;
	}
	
    public void setCooldown(Player p, String cooldown)
    {
    	int cooldownAmount = plugin.getHSPConfig().getInt(ConfigOptions.COOLDOWN_BASE + cooldown, 0);
    	
    	if(cooldownAmount > 0) {
//    		log.info(logPrefix + " saving cooldown "+p.getName()+"."+cooldown+", cooldownAmount = "+cooldownAmount);
    		cooldowns.put(p.getName()+"."+cooldown, new Long(System.currentTimeMillis()));
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
//    	log.info(logPrefix + " checking cooldown for "+cooldown+", player "+p.getName());

    	int cooldownAmount = plugin.getHSPConfig().getInt(ConfigOptions.COOLDOWN_BASE + cooldown, 0);
    	if( cooldownAmount == 0 )
    		return 0;
    	
    	String key = p.getName()+"."+cooldown;
    	Long cooldownStartTime = cooldowns.get(key);
    	if( cooldownStartTime != null )
    	{
//        	log.info(logPrefix + " cooldown start Time for key "+key+" = "+cooldownStartTime/1000);
        	
    		// Compare time
    		long timeElapsed = (System.currentTimeMillis() - cooldownStartTime)/1000;
    		
    		if(timeElapsed > cooldownAmount)
    			cooldowns.remove(key);    					// cooldown expired, remote it
    		else
    			cooldownRemaining = cooldownAmount-timeElapsed;
    	}
    	
//    	log.info(logPrefix + " cooldown remaining for key "+key+" = "+cooldownRemaining);
    	return cooldownRemaining;
    }
}
