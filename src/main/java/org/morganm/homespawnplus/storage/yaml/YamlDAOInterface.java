/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml;

import org.morganm.homespawnplus.storage.StorageException;

/** Interface all YAML DAOs adhere to, so they can be generically processed
 * together.
 * 
 * @author morganm
 *
 */
public interface YamlDAOInterface {
	public void invalidateCache();
	public void setDeferredWrite(boolean deferred);
	public void flush() throws StorageException;
	public void deleteAllData() throws StorageException;
}
