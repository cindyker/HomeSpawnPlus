package org.morganm.homespawnplus.storage.ebean;

import com.avaje.ebean.EbeanServer;
import org.morganm.homespawnplus.entity.UUIDHistory;
import org.morganm.homespawnplus.storage.dao.UUIDHistoryDAO;

import java.util.Set;

/**
 * @author andune
 */
public class UUIDHistoryDAOEbean implements UUIDHistoryDAO {
    private EbeanServer ebean;

    public UUIDHistoryDAOEbean(final EbeanServer ebean) {
        setEbeanServer(ebean);
    }

    public void setEbeanServer(final EbeanServer ebean) {
        this.ebean = ebean;
    }

    @Override
    public Set<UUIDHistory> findByUUID(java.util.UUID uuid) {
        return ebean.find(UUIDHistory.class).where().ieq("uuid", uuid.toString()).orderBy("dateCreated").findSet();
    }

    @Override
    public Set<UUIDHistory> findByName(String name) {
        return ebean.find(UUIDHistory.class).where().ieq("name", name).orderBy("dateCreated").findSet();
    }

    @Override
    public Set<UUIDHistory> findAll() {
        return ebean.find(UUIDHistory.class).orderBy("dateCreated").findSet();
    }

    @Override
    public void save(UUIDHistory uuid) {
        ebean.save(uuid);
    }
}
