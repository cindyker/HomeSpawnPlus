package com.andune.minecraft.hsp.config;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.JarUtils;
import com.andune.minecraft.commonlib.server.api.Factory;
import com.andune.minecraft.commonlib.server.api.Plugin;

import javax.inject.Inject;
import java.io.File;

/**
 * Simple class that copies the config/README.txt file into place from the
 * plugin JAR file.
 *
 * @author andune
 */
public class CopyConfigReadme implements Initializable {
    private final String README_FILE = "README.txt";

    private final Plugin plugin;
    private final JarUtils jarUtil;

    @Inject
    public CopyConfigReadme(Plugin plugin, JarUtils jarUtil) {
        this.plugin = plugin;
        this.jarUtil = jarUtil;
    }

    @Override
    public void init() throws Exception {
        File pluginDir = plugin.getDataFolder();
        // create the config directory if it doesn't exist
        File configDir = new File(pluginDir, "config");
        if (!configDir.exists())
            configDir.mkdirs();

        File readmeFile = new File(configDir, README_FILE);
        if (!readmeFile.exists())
            jarUtil.copyConfigFromJar("config/" + README_FILE, readmeFile);
    }

    @Override
    public void shutdown() throws Exception {
        // do nothing
    }

    @Override
    public int getInitPriority() {
        return 9;
    }
}
