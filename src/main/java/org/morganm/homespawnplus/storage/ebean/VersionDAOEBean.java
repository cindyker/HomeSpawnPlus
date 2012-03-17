/**
 * 
 */
package org.morganm.homespawnplus.storage.ebean;

import org.morganm.homespawnplus.entity.Version;
import org.morganm.homespawnplus.storage.dao.VersionDAO;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;

/**
 * @author morganm
 *
 */
public class VersionDAOEBean implements VersionDAO {
	private EbeanServer ebean;
	
	public VersionDAOEBean(final EbeanServer ebean) {
		setEbeanServer(ebean);
	}
	
	public void setEbeanServer(final EbeanServer ebean) {
		this.ebean = ebean;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.VersionDAO#getVersionObject()
	 */
	@Override
	public Version getVersionObject() {
		String q = "find version where id = 1";
		Query<Version> versionQuery = ebean.createQuery(Version.class, q);
		return versionQuery.findUnique();
	}
}
