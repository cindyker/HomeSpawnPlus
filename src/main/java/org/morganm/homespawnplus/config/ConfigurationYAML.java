/**
 * 
 */
package org.morganm.homespawnplus.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.bukkit.util.config.Configuration;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.SpawnStrategy;


/**
 * @author morganm
 *
 */
public class ConfigurationYAML extends Configuration implements Config {
	private static final Logger log = HomeSpawnPlus.log;
	private final String logPrefix; 
	
	private File file;
	private HomeSpawnPlus plugin;
	
	public ConfigurationYAML(File file, HomeSpawnPlus plugin) {
		super(file);
		this.file = file;
		this.plugin = plugin;
		
		this.logPrefix = HomeSpawnPlus.logPrefix;
	}

	@Override
    public void load() {
		// if no config exists, copy the default one out of the JAR file
		if( !file.exists() )
			copyConfigFromJar("config.yml");
		
		super.load();
    }

	/** Right now we don't allow updates in-game, so we don't do anything, because if we
	 * let it save, all the comments are lost.  In the future, I may allow in-game updates
	 * to the config file and this will just call super.save();
	 */
	@Override
	public boolean save() {
		return true;
	}
	
	/** Code adapted from Puckerpluck's MultiInv plugin.
	 * 
	 * @param string
	 * @return
	 */
    private void copyConfigFromJar(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        
        if (!file.canRead()) {
            try {
            	JarFile jar = new JarFile(plugin.getJarFile());
            	
                file.getParentFile().mkdirs();
                JarEntry entry = jar.getJarEntry(fileName);
                InputStream is = jar.getInputStream(entry);
                FileOutputStream os = new FileOutputStream(file);
                byte[] buf = new byte[(int) entry.getSize()];
                is.read(buf, 0, (int) entry.getSize());
                os.write(buf);
                os.close();
            } catch (Exception e) {
                log.warning(logPrefix + " Could not copy config file "+fileName+" to default location");
            }
        }
    }

    /** TODO: add caching so we aren't string->enum converting on every join/death
     * 
     */
	@Override
	public List<SpawnStrategy> getStrategies(String node) {
		List<SpawnStrategy> spawnStrategies = new ArrayList<SpawnStrategy>();
    	List<String> strategies = getStringList(node, null);

    	for(String s : strategies) {
    		spawnStrategies.add(SpawnStrategy.mapStringToStrategy(s));
    	}
    	
		return spawnStrategies;
	}
}
