/**
 * 
 */
package org.morganm.homespawnplus;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.util.Debug;


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
	private final Debug debug;
	
    private Hashtable<String, Long> cooldowns;

    public CooldownManager(HomeSpawnPlus plugin) {
    	this.plugin = plugin;
    	this.logPrefix = HomeSpawnPlus.logPrefix;
    	this.debug = Debug.getInstance();
    	
    	cooldowns = new Hashtable<String, Long>();
    }
    
    private boolean isExemptFromCooldown(Player p, String cooldown) {
    	final String cooldownBase = getCooldownBasename(cooldown);
    	if( plugin.hasPermission(p, HomeSpawnPlus.BASE_PERMISSION_NODE+".CooldownExempt."+cooldownBase) )
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
    	CooldownTime cdt = getCooldownTime(p, cooldown);
    	int cooldownAmount = cdt.cooldownTime;
    	cooldown = cdt.cooldownName;
    	
    	if(cooldownAmount > 0) {
//    		log.info(logPrefix + " saving cooldown "+p.getName()+"."+cooldown+", cooldownAmount = "+cooldownAmount);
    		cooldowns.put(p.getName()+"."+cdt.cooldownName, new Long(System.currentTimeMillis()));
    	}
    }
    
    /** Return the number of remaining seconds for a given cooldown.
     * 
     * @param p
     * @param cooldown
     * @return
     */
    public long getCooldownRemaining(final Player p, String cooldown)
    {
    	long cooldownRemaining = 0;
//    	log.info(logPrefix + " checking cooldown for "+cooldown+", player "+p.getName());

    	CooldownTime cdt = getCooldownTime(p, cooldown);
    	int cooldownAmount = cdt.cooldownTime;
    	cooldown = cdt.cooldownName;
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
    
	/** cooldowns can be named per home, such as: "home.myhome1" - this separates
	 * out the two parts and returns just "home", so that other routines can
     * lookup any cooldowns for "home" in the config.
     */
    private String getCooldownBasename(final String cooldown) {
    	String cooldownBase = null;
    	int index = cooldown.indexOf('.');
    	if( index != -1 )
    		cooldownBase = cooldown.substring(0, index-1);
    	else
    		cooldownBase = cooldown;
    	
    	return cooldownBase;
    	
    }
    
    /** Return the time for the cooldown for the given player. This takes world and
     * permission-specific cooldowns into account. This also returns the cooldown
     * name, which can change if the admin wants the cooldown to be specific to
     * the world or permission.
     * 
     * @param p
     * @param cooldown
     * @return
     */
    public CooldownTime getCooldownTime(final Player player, final String cooldown) {
    	final CooldownTime cdt = new CooldownTime();
    	cdt.cooldownName = cooldown;	// default to existing cooldown name
    	
    	final String cooldownBase = getCooldownBasename(cooldown);
    	
    	ConfigurationSection cs = plugin.getHSPConfig().getConfigurationSection(ConfigOptions.COOLDOWN_BASE
    			+ ConfigOptions.SETTING_EVENTS_PERMBASE);
    	if( cs != null ) {
    		Set<String> keys = cs.getKeys(false);
    		if( keys != null ) 
    			for(String entry : keys) {
    				// stop looping once we find a non-zero cooldownTime
    				if( cdt.cooldownTime != 0 )
    					break;
    				
    				int entryCooldown = plugin.getHSPConfig().getInt(ConfigOptions.COOLDOWN_BASE
    						+ ConfigOptions.SETTING_EVENTS_PERMBASE + "." + cooldownBase, 0);
    				
    				if( entryCooldown > 0 ) {
	    				List<String> perms = plugin.getHSPConfig().getStringList(ConfigOptions.COOLDOWN_BASE
	    						+ ConfigOptions.SETTING_EVENTS_PERMBASE + "."
	    						+ entry + ".permissions", null);
	
	    				for(String perm : perms) {
	    					debug.debug("getCooldownTime(): checking permission ",perm," for entry ",entry);
	
	    					if( plugin.hasPermission(player, perm) ) {
	    						cdt.cooldownTime = entryCooldown;
	    	    				if( plugin.getHSPConfig().getBoolean(ConfigOptions.COOLDOWN_BASE
	    	    						+ ConfigOptions.SETTING_EVENTS_PERMBASE + "."
	    	    						+ entry + ".cooldownPerPermission", false) )
	    	    					cdt.cooldownName = cooldown + "." + perm;
	    						break;
	    					}
	    				}
    				}
    				
    			}
    	}
    	
    	// if cooldownTime is still 0, then check for world-specific cooldown
    	if( cdt.cooldownTime == 0 ) {
    		final String worldName = player.getWorld().getName();
    		cdt.cooldownTime = plugin.getHSPConfig().getInt(ConfigOptions.COOLDOWN_BASE
					+ ConfigOptions.SETTING_EVENTS_WORLDBASE + "."
					+ worldName + "." + cooldownBase, 0);
			if( plugin.getHSPConfig().getBoolean(ConfigOptions.COOLDOWN_BASE
					+ ConfigOptions.SETTING_EVENTS_WORLDBASE + "."
					+ worldName + ".cooldownPerWorld", false) )
				cdt.cooldownName = cooldown + "." + worldName;
    	}
    	
    	// if cooldownTime is still 0, then check global cooldown setting
    	if( cdt.cooldownTime == 0 ) {
    		cdt.cooldownTime = plugin.getHSPConfig().getInt(ConfigOptions.COOLDOWN_BASE + cooldownBase, 0);
    	}
    	
    	return cdt;
    }
    
    class CooldownTime {
    	int cooldownTime = 0;
    	String cooldownName;
    }
}
