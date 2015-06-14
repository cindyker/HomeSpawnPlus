package com.andune.minecraft.hsp.server.sponge;

import com.andune.minecraft.commonlib.JarUtils;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.google.inject.Inject;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * @author andune
 */
public class SpongePlugin implements Plugin {
    protected static final Logger log = LoggerFactory.getLogger(SpongePlugin.class);

    private final PluginContainer pluginContainer;
    private final JarUtils jarUtil;
    private final File pluginDataFolder;
    private String build = null;

    @Inject
    public SpongePlugin(PluginContainer pluginContainer, JarUtils jarUtil, File pluginDataFolder) {
        this.pluginContainer = pluginContainer;
        this.jarUtil = jarUtil;
        this.pluginDataFolder = pluginDataFolder;
    }

    public Object getPluginObject() { return pluginContainer.getInstance(); }

    @Override
    public File getDataFolder() {
        return pluginDataFolder;
    }

    // TODO: figure out how to do this in Sponge
    @Override
    public File getJarFile() {
        return null;
    }

    @Override
    public String getName() {
        return pluginContainer.getName();
    }

    // TODO: figure out how to do this in Sponge - or if it's even necessary?
    @Override
    public ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    @Override
    public String getVersion() {
        return pluginContainer.getVersion();
    }

    @Override
    public String getBuild() {
        if( build == null )
            build = jarUtil.getBuild();
        return build;
    }

    // TODO: figure out how to do this in Sponge
    @Override
    public InputStream getResource(String filename) {
        return null;
    }
}
