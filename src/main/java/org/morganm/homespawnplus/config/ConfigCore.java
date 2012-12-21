/**
 * 
 */
package org.morganm.homespawnplus.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.Initializable;
import org.morganm.homespawnplus.server.api.YamlFile;


/**
 * @author morganm
 *
 */
@Singleton
public class ConfigCore extends ConfigBase implements ConfigInterface, Initializable {
    @Inject
    public ConfigCore(YamlFile yaml) {
        super("core.yml", "core", yaml);
    }

    @Override
    public int getPriority() {
        return 0;   // core config has lowest priority
    }

    /**
     * Return the configured locale, such as "en", "de", "fr", etc.
     * 
     * @return
     */
    public String getLocale() {
        return super.getString("locale");
    }
    
    public boolean isDebug() {
        return super.getBoolean("debug");
    }
    
    /**
     * Is verbose logging enabled?
     * 
     * @return true if verbose logging is enabled
     */
    public boolean isVerboseLogging() {
        return super.getBoolean("verboseLogging");
    }
    
    /**
     *  Is verbose strategy logging enabled?
     *  
     * @return
     */
    public boolean isVerboseStrategyLogging() {
        return super.getBoolean("verboseStrategyLogging");
    }
    
    /**
     * Is safe teleport mode enabled?
     * 
     * @return true if safe teleport is enabled
     */
    public boolean isSafeTeleport() {
        return super.getBoolean("safeTeleport");
    }
    
    /**
     * Millisecond value for controlling performance-related warnings.
     * 
     * @return
     */
    public int getPerformanceWarnMillis() {
        return super.getInt("warnPerformanceMillis");
    }
    
    /**
     * Determine if the last home on a given world is always considered
     * the default.
     * 
     * @return true if the last home is the default
     */
    public boolean isLastHomeDefault() {
        return super.getBoolean("lastHomeIsDefault");
    }
    
    /**
     * Determine if teleport messages should be sent on home/spawn
     * commands to tell the player they have arrived.
     * 
     * @return
     */
    public boolean isTeleportMessages() {
        return super.getBoolean("teleportMessages");
    }
    
    /**
     * Determine if named permissions should be used for spawns, such that
     * if a player types "/spawn spawn1", a specific permission for spawn1
     * will be checked, such as "hsp.command.spawn.named.spawn1"
     * 
     * @return
     */
    public boolean isSpawnNamedPermissions() {
        return super.getBoolean("spawnNamedPermissions");
    }
    
    /**
     * Determine if setting default world spawn should also set the
     * spawn for that world on the backing server (ie. in the MC maps
     * itself).
     * 
     * @return
     */
    public boolean isOverrideWorld() {
        return super.getBoolean("override_world");
    }
    
    /**
     * Determine if sleeping in a bed should overwrite the defaultHome if a
     * bed home isn't found. In practice, when set, this means the bedHome
     * and the defaultHome will be the same.
     * 
     * @return
     */
    public boolean isBedHomeOverwriteDefault() {
        return super.getBoolean("bedHomeOverwritesDefault");
    }
    
    /**
     * Return a string representing the default color, of the form
     * "%yellow", "%red%", etc.  TODO: link to documentation on colors
     * 
     * @return
     */
    public String getDefaultColor() {
        return super.getString("defaultMessageColor");
    }
    
    /**
     * Return the default world, used anywhere "default world" is referenced
     * in spawn strategies.
     * 
     * @return
     */
    public String getDefaultWorld() {
        return super.getString("defaultWorld");
    }
    
    /**
     * Boolean value to control whether or not HSP monitors events to see
     * if the player is appearing somewhere other than where HSP intended
     * and prints out warnings to the log if so.
     * 
     * @return
     */
    public boolean isWarnLocationChange() {
        return super.getBoolean("warnLocationChange");
    }
    
    /**
     * Boolean value to control whether or not sleeping in a bed sets a
     * player's HSP home to that location. If this config value is false,
     * HSP ignores bed events entirely.
     * 
     * @return
     */
    public boolean isBedSetHome() {
        return super.getBoolean("bedsethome");
    }
    
    /**
     * Boolean value to control whether or not homes can be set during
     * the day. If this value is true, HSP follows "vanilla Minecraft"
     * behavior and bed homes can only be set at night.
     * 
     * @return
     */
    public boolean isBedHomeMustBeNight() {
        return super.getBoolean("bedHomeMustBeNight");
    }
    
    /**
     * By default, HSP has "2-click" protection enabled, which requires
     * a player to click twice to save their home to a bed; also by
     * default HSP will cancel the first click of the 2-click safety,
     * which avoids an extra "You can only sleep at night" message when
     * clicking during daylight.
     * 
     * Some admins preferred the original behavior where HSP did not
     * cancel the first click, so this config parameter can be used
     * to bring back that behavior.
     * 
     * @return
     */
    public boolean isBedHomeOriginalBehavior() {
        return super.getBoolean("bedHomeOriginalBehavior");
    }
    
    /**
     * HSP allows players to set their bedHome during the day, but
     * vanilla Minecraft will still print the "You can only sleep
     * at night" message. With this option enabled, HSP will
     * suppress that message.
     * 
     * However, it comes at the cost of canceling the bed click
     * which means players can never actually sleep in a bed.
     * 
     * @return
     */
    public boolean isBedNeverDisplayNightMessage() {
        return super.getBoolean("bedHomeNeverDisplayNightMessage");
    }
    
    /**
     * Boolean value that determines whether or not 2 clicks are
     * required to set a bed home. If false, only 1 click is
     * required.
     * 
     * @return
     */
    public boolean isBedHome2Clicks() {
        return super.getBoolean("bedhome2clicks");
    }
    
    /**
     * Boolean value to control whether or not HSP records the
     * location when a player logs out.
     * 
     * @return
     */
    public boolean isRecordLastLogout() {
        return super.getBoolean("recordLastLogout");
    }
    
    /**
     * Boolean value that determines whether Multiverse integration
     * is enabled or not.
     * 
     * @return
     */
    public boolean isMultiverseEnabled() {
        return super.getBoolean("multiverseEnabled");
    }
    
    /**
     * Return the configured string for the storage type we should
     * use. Examples: "ebeans", "yaml"
     * @return
     */
    public String getStorageType() {
        return super.getString("storage");
    }
}
