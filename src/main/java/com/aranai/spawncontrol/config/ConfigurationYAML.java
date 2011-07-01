/**
 * 
 */
package com.aranai.spawncontrol.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.bukkit.util.config.Configuration;

import com.aranai.spawncontrol.SpawnControl;

/**
 * @author morganm
 *
 */
public class ConfigurationYAML extends Configuration implements Config {
	private static final Logger log = SpawnControl.log;
	private final String logPrefix; 
	
	private File file;
	private SpawnControl plugin;
	
	public ConfigurationYAML(File file, SpawnControl plugin) {
		super(file);
		this.file = file;
		this.plugin = plugin;
		
		this.logPrefix = SpawnControl.logPrefix;
	}

	@Override
    public void load() {
		// if no config exists, copy the default one out of the JAR file
		if( !file.exists() )
			copyConfigFromJar("config.yml");
		
		super.load();
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
}
