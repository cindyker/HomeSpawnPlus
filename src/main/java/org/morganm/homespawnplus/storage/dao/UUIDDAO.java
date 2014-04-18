package org.morganm.homespawnplus.storage.dao;

import org.morganm.homespawnplus.entity.Player;
import org.morganm.homespawnplus.entity.UUID;
import org.morganm.homespawnplus.storage.StorageException;

import java.util.Set;

/**
 * @author andune
 */
public interface UUIDDAO {
    UUID findByUUID(java.util.UUID uuid);
    UUID findByName(String name);

    /**
     * Find the list of all known current UUID mappings on this server.
     *
     * @return
     */
    Set<UUID> findAll();

    void save(UUID uuid) throws StorageException;
}
