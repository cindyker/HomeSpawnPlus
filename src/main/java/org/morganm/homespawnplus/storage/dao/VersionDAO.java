/**
 * 
 */
package org.morganm.homespawnplus.storage.dao;

import org.morganm.homespawnplus.entity.Version;

/**
 * @author morganm
 *
 */
public interface VersionDAO {
	/** There is only a single version object, this method will return it.
	 * 
	 * @return
	 */
	public Version getVersionObject();
}
