/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
/**
 * 
 */
package com.andune.minecraft.hsp.util;

import java.io.File;
import java.util.Set;

import javax.inject.Inject;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.yaml.StorageYaml;

/**
 * @author andune
 *
 */
public class BukkitBackupUtil implements BackupUtil {
    private static final Logger log = LoggerFactory.getLogger(BukkitBackupUtil.class);
    
    private final Plugin plugin;
    private final Storage storage;
    private final Server server;

    @Inject
    public BukkitBackupUtil(Plugin plugin, Storage storage, Server server) {
        this.plugin = plugin;
        this.storage = storage;
        this.server = server;
    }
    
    @Override
    public String backup() {
        String errorMessage = null;
        
        final File backupFile = getBackupFile();
        if( backupFile.exists() )
            backupFile.delete();
        
        try {
            StorageYaml backupStorage = new StorageYaml(plugin, true, backupFile);
            backupStorage.initializeStorage();

            backupStorage.setDeferredWrites(true);
            for(com.andune.minecraft.hsp.entity.Home o : storage.getHomeDAO().findAllHomes()) {
                log.debug("backing up Home object id ",o.getId());
                backupStorage.getHomeDAO().saveHome(o);
            }
            for(com.andune.minecraft.hsp.entity.Spawn o : storage.getSpawnDAO().findAllSpawns()) {
                log.debug("backing up Spawn object id ",o.getId());
                backupStorage.getSpawnDAO().saveSpawn(o);
            }
            for(com.andune.minecraft.hsp.entity.Player o : storage.getPlayerDAO().findAllPlayers()) {
                log.debug("backing up Player object id ",o.getId());
                backupStorage.getPlayerDAO().savePlayer(o);
            }
            for(com.andune.minecraft.hsp.entity.HomeInvite o : storage.getHomeInviteDAO().findAllHomeInvites()) {
                log.debug("backing up HomeInvite object id ",o.getId());
                backupStorage.getHomeInviteDAO().saveHomeInvite(o);
            }

            backupStorage.flushAll();

            log.info("Data backed up to file {}", backupFile);
        }
        catch(StorageException e) {
            log.warn("Error saving backup file", e);
            errorMessage = server.getLocalizedMessage(HSPMessages.CMD_HSP_DATA_BACKUP_ERROR);
        }

        return errorMessage;
    }

    @Override
    public String restore() {
        String errorMessage = null;

        final File backupFile = getBackupFile();
        if( backupFile.exists() ) {
            try {
                StorageYaml backupStorage = new StorageYaml(plugin, true, backupFile);
                backupStorage.initializeStorage();

                storage.deleteAllData();
                storage.setDeferredWrites(true);

                Set<? extends com.andune.minecraft.hsp.entity.Home> homes = backupStorage.getHomeDAO().findAllHomes();
                for(com.andune.minecraft.hsp.entity.Home home : homes) {
                    log.debug("Restoring home ",home);
                    home.setLastModified(null);
                    storage.getHomeDAO().saveHome(home);
                }
                Set<? extends com.andune.minecraft.hsp.entity.Spawn> spawns = backupStorage.getSpawnDAO().findAllSpawns();
                for(com.andune.minecraft.hsp.entity.Spawn spawn : spawns) {
                    log.debug("Restoring spawn ",spawn);
                    spawn.setLastModified(null);
                    storage.getSpawnDAO().saveSpawn(spawn);
                }
                Set<com.andune.minecraft.hsp.entity.Player> players = backupStorage.getPlayerDAO().findAllPlayers();
                for(com.andune.minecraft.hsp.entity.Player player : players) {
                    log.debug("Restoring player ",player);
                    player.setLastModified(null);
                    storage.getPlayerDAO().savePlayer(player);
                }
                Set<com.andune.minecraft.hsp.entity.HomeInvite> homeInvites = backupStorage.getHomeInviteDAO().findAllHomeInvites();
                for(com.andune.minecraft.hsp.entity.HomeInvite homeInvite : homeInvites) {
                    log.debug("Restoring homeInvite ",homeInvite);
                    homeInvite.setLastModified(null);
                    storage.getHomeInviteDAO().saveHomeInvite(homeInvite);
                }

                storage.flushAll();
            }
            catch(StorageException e) {
                errorMessage = server.getLocalizedMessage(HSPMessages.CMD_HSP_DATA_BACKUP_ERROR);
                log.warn("Caught error running data restore: "+e.getMessage(), e);
            }
            finally {
                storage.setDeferredWrites(false);
            }

            log.info("Existing data wiped and data restored from file "+backupFile);
        }
        else {
            errorMessage = server.getLocalizedMessage(HSPMessages.CMD_HSP_DATA_RESTORE_NO_FILE,
                    "file", backupFile);
        }

        return errorMessage;
    }
    
    @Override
    public File getBackupFile() {
        return new File(plugin.getDataFolder(), "backup.yml");
    }
}
