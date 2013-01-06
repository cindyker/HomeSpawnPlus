package org.morganm.homespawnplus.entity;

import java.sql.Timestamp;

import org.morganm.homespawnplus.server.api.Location;

public interface Spawn extends EntityWithLocation {

	public abstract void setLocation(Location l);

	public abstract Location getLocation();

	/**
	 * Return true if this spawn is the new player spawn.
	 * 
	 * @return
	 */
	public abstract boolean isNewPlayerSpawn();

	/**
	 * Return true if this is the default spawn for the world it is in.
	 * 
	 * @return
	 */
	public abstract boolean isDefaultSpawn();

	public abstract int getId();

	public abstract void setId(int id);

	public abstract String getWorld();

	public abstract void setWorld(String world);

	public abstract Double getX();

	public abstract void setX(Double x);

	public abstract Double getY();

	public abstract void setY(Double y);

	public abstract Double getZ();

	public abstract void setZ(Double z);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract String getGroup();

	public abstract void setGroup(String group);

	public abstract String getUpdatedBy();

	public abstract void setUpdatedBy(String updatedBy);

	public abstract Float getPitch();

	public abstract void setPitch(Float pitch);

	public abstract Float getYaw();

	public abstract void setYaw(Float yaw);

	public abstract Timestamp getLastModified();

	public abstract void setLastModified(Timestamp lastModified);

	public abstract Timestamp getDateCreated();

	public abstract void setDateCreated(Timestamp dateCreated);

}