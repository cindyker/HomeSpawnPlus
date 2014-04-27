package org.morganm.homespawnplus.util;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.HomeInvite;
import org.morganm.homespawnplus.entity.Player;
import org.morganm.homespawnplus.entity.PlayerLastLocation;
import org.morganm.homespawnplus.entity.PlayerSpawn;
import org.morganm.homespawnplus.entity.UUIDHistory;
import org.morganm.homespawnplus.storage.StorageException;

import java.util.Set;
import java.util.UUID;

/**
 * Class for updating player names and UUIDs in the database.
 *
 * @author andune
 */
public class UUIDNameUpdater {
    private final Logger log;
    private final HomeSpawnPlus plugin;

    public UUIDNameUpdater(HomeSpawnPlus plugin) {
        this.plugin = plugin;
        this.log = plugin.getLog();
    }

    /**
     * Look for a name change for the given UUID.
     *
     * @param uuid the UUID to check
     * @param name the new name for the given UUID
     */
    public void updateUUID(UUID uuid, String name) throws StorageException {
        Player player = plugin.getStorage().getPlayerDAO().findPlayerByUUID(uuid);
        if( player != null ) {
            if( !player.getName().equals(name) ) {
                nameChange(player.getName(), name);
                addUUIDHistory(uuid, name);
            }
        }
        else {
            // no player found by UUID. Try their name instead.
            player = plugin.getStorage().getPlayerDAO().findPlayerByName(name);
            if( player != null ) {
                // we found a player object missing UUID; add it
                player.setUuid(uuid);
            }
            // it's a new player we've never seen before
            else {
                player = new Player();
                player.setName(name);
                player.setUuid(uuid);
            }
            plugin.getStorage().getPlayerDAO().savePlayer(player);

            addUUIDHistory(uuid, name);
        }
    }

    /**
     * When a player's name changes, either automatically detected or by
     * an admin command, this method will update all objects from oldName
     * to newName.
     *
     * @param oldName
     * @param newName
     */
    public void nameChange(String oldName, String newName) throws StorageException {
        Player p = plugin.getStorage().getPlayerDAO().findPlayerByName(oldName);
        if( p != null ) {
            p.setName(newName);
            plugin.getStorage().getPlayerDAO().savePlayer(p);
        }

        Set<Home> homeSet = plugin.getStorage().getHomeDAO().findHomesByPlayer(oldName);
        if( homeSet != null ) {
            for(Home h: homeSet) {
                h.setName(newName);
                plugin.getStorage().getHomeDAO().saveHome(h);
            }
        }

        Set<PlayerLastLocation> pllSet = plugin.getStorage().getPlayerLastLocationDAO().findByPlayerName(oldName);
        if( pllSet != null ) {
            for(PlayerLastLocation pll: pllSet) {
                pll.setPlayerName(newName);
                plugin.getStorage().getPlayerLastLocationDAO().save(pll);
            }
        }

        Set<HomeInvite> hiSet = plugin.getStorage().getHomeInviteDAO().findAllAvailableInvites(oldName);
        if( hiSet != null ) {
            for(HomeInvite hi: hiSet) {
                hi.setInvitedPlayer(newName);
                plugin.getStorage().getHomeInviteDAO().saveHomeInvite(hi);
            }
        }

        Set<PlayerSpawn> psSet = plugin.getStorage().getPlayerSpawnDAO().findByPlayerName(oldName);
        if( psSet != null ) {
            for(PlayerSpawn ps: psSet) {
                ps.setPlayerName(newName);
                plugin.getStorage().getPlayerSpawnDAO().save(ps);
            }
        }
    }

    /**
     * Store a name change for a given UUID into the database.
     *
     * @param uuid
     * @param newName
     */
    private void addUUIDHistory(UUID uuid, String newName) {
        UUIDHistory uuidHistory = new UUIDHistory(uuid, newName);
        try {
            plugin.getStorage().getUUIDHistoryDAO().save(uuidHistory);
        } catch (StorageException e) {
            log.severe("Caught exception while storing UUID History change event for player " + newName, e);
        }
    }
}
