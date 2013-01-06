/**
 * 
 */
package com.andune.minecraft.hsp.convert;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.andune.minecraft.hsp.server.api.CommandSender;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.server.api.Plugin;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.storage.Storage;

/**
 * @author morganm
 *
 */
public abstract class BaseConverter implements Converter {
    protected Logger log = LoggerFactory.getLogger(BaseConverter.class);
    protected CommandSender initiatingSender;

    protected Plugin plugin;
    protected Server server;
    protected Storage storage;
    protected Factory factory;
    
    @Inject public void setPlugin(Plugin plugin) { this.plugin = plugin; }
    @Inject public void setServer(Server server) { this.server = server; }
    @Inject public void setStorage(Storage storage) { this.storage = storage; }
    @Inject public void setFactory(Factory factory) { this.factory = factory; }
    
    @Override
    public void run() {
        try {
            int homesConverted = convert();
            if( initiatingSender != null )
                initiatingSender.sendMessage("Finished converting "+homesConverted+" homes");
        }
        catch(Exception e) {
            log.warn("error trying to convert homes", e);
            initiatingSender.sendMessage("Error converting homes, check your server.log");
        }
    }

    @Override
    public void setInitiatingSender(CommandSender initiatingSender) {
        this.initiatingSender = initiatingSender;
    }
}
