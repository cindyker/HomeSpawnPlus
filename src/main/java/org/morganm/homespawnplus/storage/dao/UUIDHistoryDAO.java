package org.morganm.homespawnplus.storage.dao;

import org.morganm.homespawnplus.entity.UUIDHistory;
import org.morganm.homespawnplus.storage.StorageException;

import java.util.Set;

/**
 * @author andune
 */
public interface UUIDHistoryDAO {
    /**
     * Given a UUID, return all UUID objects (UUID:playerName mappings) that have
     * ever been seen.
     *
     * @param uuid the UUID to search for
     * @return the Set of UUIDs that match, possibly none. Guaranteed to not be null
     */
    Set<UUIDHistory> findByUUID(java.util.UUID uuid);
    /**
     * Given a playerName, return all UUID objects (UUID:playerName mappings) that have
     * ever been seen.
     *
     * @param name the playerName to search for
     * @return the Set of UUIDs that match, possibly none. Guaranteed to not be null
     */
    Set<UUIDHistory> findByName(String name);

    /**
     * Find the list of all known UUIDs ever seen by HSP on this server.
     *
     * @return
     */
    Set<UUIDHistory> findAll();

    /**
     * By contract, UUIDHistory should never be changed once created, so never use
     * this method to change a UUIDHistory object. Only use it to save a newly created
     * object after first confirming an existing UUID:Player history entry doesn't
     * exist.
     *
     * Implementations of this interface are free to enforce this contract and throw
     * an exception if it is violated.
     *
     * @param uuid
     * @throws StorageException
     */
    void save(UUIDHistory uuid) throws StorageException;
}
