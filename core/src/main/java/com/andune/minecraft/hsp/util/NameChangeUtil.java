package com.andune.minecraft.hsp.util;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.entity.*;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.*;
import com.andune.minecraft.hsp.config.ConfigCore;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Set;

/**
 * Class to be used to change a players name, either when detected based
 * on UUID or by admin commands.
 *
 * @author andune
 */
public class NameChangeUtil {
    private static final Logger log = LoggerFactory.getLogger(NameChangeUtil.class);
    private final PlayerDAO playerDAO;
    private final HomeDAO homeDAO;
    private final HomeInviteDAO homeInviteDAO;
    private final PlayerLastLocationDAO playerLastLocationDAO;
    private final PlayerSpawnDAO playerSpawnDAO;
    private final ConfigCore configCore;

    @Inject
    public NameChangeUtil(Storage storage, ConfigCore configCore) {
        this.configCore = configCore;
        this.playerDAO = storage.getPlayerDAO();
        this.homeDAO = storage.getHomeDAO();
        this.homeInviteDAO = storage.getHomeInviteDAO();
        this.playerLastLocationDAO = storage.getPlayerLastLocationDAO();
        this.playerSpawnDAO = storage.getPlayerSpawnDAO();
    }

    /**
     * Change a player's name in all objects that reference the player.
     * Return a count of the number of rows affected.
     *
     * @param newName
     * @param oldName
     * @return
     */
    public int changeName(String newName, String oldName) {
        int rows = 0;

        Player player = playerDAO.findPlayerByName(oldName);
        if (player != null) {
            try {
                player.setName(newName);
                playerDAO.savePlayer(player);
                rows++;
            } catch(StorageException e) {
                log.warn("Caught exception when trying to change name for player object, oldName {}, newName {}, Error: {}", oldName, newName, e);
            }
        }

        Set<? extends Home> homes = homeDAO.findHomesByPlayer(oldName);
        if (homes != null && homes.size() > 0) {
            for(Home h : homes) {
                try {
                    h.setPlayerName(newName);
                    homeDAO.saveHome(h);
                    rows++;
                } catch(StorageException e) {
                    log.warn("Caught exception when trying to change name for Home object, oldName {}, newName {}, home {}, Error: {}", oldName, newName, h, e);
                }
            }
        }

        Set<HomeInvite> homeInvites = homeInviteDAO.findAllAvailableInvites(oldName);
        if (homeInvites != null && homeInvites.size() > 0) {
            for(HomeInvite hi : homeInvites) {
                try {
                    hi.setInvitedPlayer(newName);
                    homeInviteDAO.saveHomeInvite(hi);
                    rows++;
                } catch(StorageException e) {
                    log.warn("Caught exception when trying to change name for HomeInvite object, oldName {}, newName {}, hi {}, Error: {}", oldName, newName, hi, e);
                }
            }
        }
        // openInvites are tied to a Home object, which we already changed earlier

        Set<PlayerLastLocation> plls = playerLastLocationDAO.findByPlayerName(oldName);
        if (plls != null && plls.size() > 0) {
            for(PlayerLastLocation pll : plls) {
                try {
                    pll.setPlayerName(newName);
                    playerLastLocationDAO.save(pll);
                    rows++;
                } catch(StorageException e) {
                    log.warn("Caught exception when trying to change name for PlayerLastLocation object, oldName {}, newName {}, pll {}, Error: {}", oldName, newName, pll, e);
                }
            }
        }

        Set<PlayerSpawn> playerSpawns = playerSpawnDAO.findByPlayerName(oldName);
        if (playerSpawns != null && playerSpawns.size() > 0) {
            for(PlayerSpawn ps : playerSpawns) {
                try {
                    ps.setPlayerName(newName);
                    playerSpawnDAO.save(ps);
                    rows++;
                } catch(StorageException e) {
                    log.warn("Caught exception when trying to change name for PlayerSpawn object, oldName {}, newName {}, ps {}, Error: {}", oldName, newName, ps, e);
                }
            }
        }

        return rows;
    }

    public void cleanupDupUUIDs() {
        HashMap<String, Player> uuidMap = new HashMap<String, Player>();

        if (configCore.isUuidCleanupOnStartup()) {
            int dupsRemoved=0;

            Set<Player> players = playerDAO.findAllPlayers();
            if (players != null && players.size() > 0) {
                for(Player p : players) {
                    final String UUIDString = p.getUUIDString();

                    // this means we found a UUID collision
                    if (uuidMap.get(UUIDString) != null) {
                        Player toDelete = uuidMap.get(UUIDString);

                        // compare IDs. the newer one gets deleted, it's the dup
                        if (toDelete.getId() < p.getId()) {
                            toDelete = p;
                        }
                        else {
                            // if we're deleting the previous hash key, store the
                            // new player object for future comparisons, in case
                            // there are more UUID duplicates. This ensures even
                            // if there are 3 or more dups, this loop will keep
                            // the oldest entry out of all of them.
                            uuidMap.put(UUIDString, p);
                        }

                        dupsRemoved += playerDAO.purgePlayer(toDelete.getName());
                    }
                    else {
                        uuidMap.put(UUIDString, p);
                    }
                }
            }

            if (dupsRemoved > 0)
                log.info("Cleaned up {} duplicate UUID rows", dupsRemoved);
        }
    }
}
