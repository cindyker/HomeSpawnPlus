package org.morganm.homespawnplus.storage.ebean;

import com.avaje.ebean.EbeanServer;
import org.morganm.homespawnplus.entity.UUID;
import org.morganm.homespawnplus.storage.dao.UUIDDAO;

import java.util.Set;

/**
 * @author andune
 */
public class UUIDDAOEbean implements UUIDDAO {
    private EbeanServer ebean;

    public UUIDDAOEbean(final EbeanServer ebean) {
        setEbeanServer(ebean);
    }

    public void setEbeanServer(final EbeanServer ebean) {
        this.ebean = ebean;
    }

    @Override
    public UUID findByUUID(java.util.UUID uuid) {
        return ebean.find(UUID.class).where().ieq("uuidString", uuid.toString()).findUnique();
    }

    @Override
    public UUID findByName(String name) {
        return ebean.find(UUID.class).where().ieq("name", name).findUnique();
    }

    @Override
    public Set<UUID> findAll() {
        return ebean.find(UUID.class).findSet();
    }

    @Override
    public void save(UUID uuid) {
        ebean.save(uuid);
    }

}
