/**
 * 
 */
package org.morganm.homespawnplus.entity;

import java.sql.Timestamp;

/**
 * @author morganm
 *
 */
public interface BasicEntity {
	public int getId();
	public void setId(int id);
	public Timestamp getLastModified();
	public void setLastModified(Timestamp lastModified);
	public Timestamp getDateCreated();
	public void setDateCreated(Timestamp dateCreated);
}
