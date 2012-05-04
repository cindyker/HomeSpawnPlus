/**
 * 
 */
package org.morganm.homespawnplus.manager;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.General;


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
    	final CooldownNames cn = parseCooldownNames(cooldown);
    	if( plugin.hasPermission(p, HomeSpawnPlus.BASE_PERMISSION_NODE+".CooldownExempt."+cn.baseName) )
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
			plugin.getUtil().sendLocalizedMessage(p, HSPMessages.COOLDOWN_IN_EFFECT,
					"name", cooldownName,
					"time", General.getInstance().displayTimeString(cooldownTimeLeft*1000,
							false, null));
//			plugin.getUtil().sendMessage(p, "Cooldown "+cooldownName+" is in effect. You must wait " + cooldownTimeLeft + " seconds.");
			return false;
		}
		
		// no longer update cooldown here, but require an explicit call to setCooldown() instead.
		// this forces the interface to work around a bug where a cooldown gets applied
		// at the start of a command even though the command aborts due to an error condition
		// (such as player arguments being wrong, etc).
//		setCooldown(p, cooldownName);
		return true;
	}
	
    public void setCooldown(final Player p, final String cooldown)
    {
    	CooldownTime cdt = getCooldownTime(p, cooldown);
    	
    	if(cdt.cooldownTime > 0) {
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
    public long getCooldownRemaining(final Player p, final String cooldown)
    {
    	long cooldownRemaining = 0;
    	debug.debug("getCooldownRemaining: p=",p," cooldown=",cooldown);

    	CooldownTime cdt = getCooldownTime(p, cooldown);
    	int cooldownAmount = cdt.cooldownTime;
    	if( cooldownAmount == 0 )
    		return 0;
    	
    	String key = p.getName()+"."+cdt.cooldownName;
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

    	debug.debug("getCooldownRemaining: cooldown remaining for key ",key," is ",cooldownRemaining);
    	return cooldownRemaining;
    }
    
	/** cooldowns can can be as generic as just "home" or as specific as
	 * "home-named.home1". This breaks down whatever input name is passed in and
	 * breaks it down to it's component parts.
     */
    private CooldownNames parseCooldownNames(final String cooldown) {
    	CooldownNames cn = new CooldownNames();

    	cn.fullName = cooldown;					// "home-named.home1"

    	int index = cooldown.indexOf('.');
    	if( index != -1 )
    		cn.extendedName = cooldown.substring(0, index);		// "home-named";
    	else
    		cn.extendedName = cooldown;
		
		index = cn.extendedName.indexOf('-');
		if( index != -1 )
			cn.baseName = cn.extendedName.substring(0, index);		// "home"
		else
			cn.baseName = cn.extendedName;

    	// up until this point, if just "home" was passed in, it will be in all
		// 3 variables. These checks will reduce it down to just the baseName.
    	if( cn.fullName.equals(cn.baseName) )
    		cn.fullName = null;
    	if( cn.fullName != null && cn.fullName.equals(cn.extendedName) )
    		cn.fullName = null;
    	if( cn.extendedName != null && cn.extendedName.equals(cn.baseName) )
    		cn.extendedName = null;
    	
    	if( cn.fullName != null && cn.extendedName != null )
    		cn.allNames = new String[] {cn.fullName, cn.extendedName, cn.baseName};
    	else if( cn.extendedName != null )
    		cn.allNames = new String[] {cn.extendedName, cn.baseName};
    	else
    		cn.allNames = new String[] {cn.baseName};

    	return cn;
    }

    public boolean isCooldownSeparationEnabled(final String cooldown) {
    	List<String> separateCooldowns = plugin.getHSPConfig().getStringList(ConfigOptions.COOLDOWN_SEPARATION, null);
    	return separateCooldowns.contains(cooldown);
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
    	
    	final CooldownNames cn = parseCooldownNames(cooldown);
    	debug.debug("getCooldownTime(): cn.baseName=",cn.baseName,", cn.extendedName=",cn.extendedName,
    			", cn.fullName=",cn.fullName);
    	
    	// check if a per-entity cooldown is being used, such as "home.myhome1". If so,
    	// we need to check the cooldownPerHomeOverride flag in case the admin wants
    	// these handled differently.
    	// no longer necessary with the addition of the "home-named" cooldown
//    	if( cn.fullName != null ) {
//    		cdt.cooldownTime = plugin.getHSPConfig().getInt(ConfigOptions.COOLDOWN_PER_HOME_OVERRIDE, 0);
//        	debug.debug("getCooldownTime(): per-home override cooldown=",cdt.cooldownTime);
//    	}
    	
    	if( cdt.cooldownTime == 0 ) {
	    	ConfigurationSection cs = plugin.getHSPConfig().getConfigurationSection(ConfigOptions.COOLDOWN_BASE
	    			+ ConfigOptions.SETTING_EVENTS_PERMBASE);
	    	if( cs != null ) {
	    		Set<String> keys = cs.getKeys(false);
	    		if( keys != null ) 
	    			for(String entry : keys) {
						debug.debug("getCooldownTime(): checking entry ",entry);
	    				// stop looping once we find a non-zero cooldownTime
	    				if( cdt.cooldownTime != 0 )
	    					break;
	    				
	    				String permMatch = null;
						List<String> perms = plugin.getHSPConfig().getStringList(ConfigOptions.COOLDOWN_BASE
								+ ConfigOptions.SETTING_EVENTS_PERMBASE + "."
								+ entry + ".permissions", null);
						for(String perm : perms) {
							debug.debug("getCooldownTime(): checking permission ",perm," for entry ",entry);

							if( plugin.hasPermission(player, perm) ) {
								permMatch = perm;
								break;
							}
						}

						if( permMatch != null ) {
		    				for(String name : cn.allNames) {
		    					cdt.cooldownTime = plugin.getHSPConfig().getInt(ConfigOptions.COOLDOWN_BASE
		    							+ ConfigOptions.SETTING_EVENTS_PERMBASE + "." + entry + "." + name, 0);
	
	    						if( plugin.getHSPConfig().getBoolean(ConfigOptions.COOLDOWN_BASE
	    								+ ConfigOptions.SETTING_EVENTS_PERMBASE + "."
	    								+ entry + ".cooldownPerPermission", false) )
	    							cdt.cooldownName = cooldown + "." + permMatch;
	    			    		if( cdt.cooldownTime != 0 )
	    			    			break;
		    				}// end for(String name : cn.allNames)
						}//end if( isPermMatch )
	    			}// end for(String entry : keys)
	    	}// end if( cs != null )
	    	
        	debug.debug("getCooldownTime(): post-permission cooldown=",cdt.cooldownTime,", name=",cdt.cooldownName);
    	}
    	
    	// if cooldownTime is still 0, then check for world-specific cooldown
    	if( cdt.cooldownTime == 0 ) {
    		final String worldName = player.getWorld().getName();
			for(String name : cn.allNames) {
	    		cdt.cooldownTime = plugin.getHSPConfig().getInt(ConfigOptions.COOLDOWN_BASE
						+ ConfigOptions.SETTING_EVENTS_WORLDBASE + "."
						+ worldName + "." + name, 0);
				if( plugin.getHSPConfig().getBoolean(ConfigOptions.COOLDOWN_BASE
						+ ConfigOptions.SETTING_EVENTS_WORLDBASE + "."
						+ worldName + ".cooldownPerWorld", false) )
					cdt.cooldownName = cooldown + "." + worldName;
	    		if( cdt.cooldownTime != 0 )
	    			break;
			}
			
	    	debug.debug("getCooldownTime(): post-world world=",worldName,", cooldown=",cdt.cooldownTime,", name=",cdt.cooldownName);
    	}
    	
    	// if cooldownTime is still 0, then check global cooldown setting
    	if( cdt.cooldownTime == 0 ) {
			for(String name : cn.allNames) {
	        	debug.debug("getCooldownTime(): check global cooldown config ",ConfigOptions.COOLDOWN_BASE,name);
	    		cdt.cooldownTime = plugin.getHSPConfig().getInt(ConfigOptions.COOLDOWN_BASE + name, 0);
	    		if( cdt.cooldownTime != 0 )
	    			break;
			}
        	debug.debug("getCooldownTime(): post-global cooldown=",cdt.cooldownTime,", name=",cdt.cooldownName);
    	}
    	
    	return cdt;
    }
    
    class CooldownTime {
    	int cooldownTime = 0;
    	String cooldownName;
    }
    
    /** When a cooldown name is passed in, it can be of the full form "sethome-named.home1".
     * It will be broken down into it's component parts and put into the members of this
     * class for processing by the cooldown algorithms.
     * 
     * @author morganm
     *
     */
    class CooldownNames {
    	String baseName = null;			// "home"
    	String extendedName = null;		// "home-named"
    	String fullName = null;			// "home-named.home1" or just "home.home1"
    	String allNames[];
    }
}
