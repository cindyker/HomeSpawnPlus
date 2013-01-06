package org.morganm.homespawnplus.entity;

import java.sql.Timestamp;

import org.morganm.homespawnplus.server.api.Location;

public interface Home extends EntityWithLocation {

	public abstract void setLocation(Location l);

	public abstract Location getLocation();

	public abstract int getId();

	public abstract void setId(int id);

	public abstract String getPlayerName();

	public abstract void setPlayerName(String playerName);

	public abstract String getWorld();

	public abstract void setWorld(String world);

	public abstract Double getX();

	public abstract void setX(Double x);

	public abstract Double getY();

	public abstract void setY(Double y);

	public abstract Double getZ();

	public abstract void setZ(Double z);

	public abstract String getUpdatedBy();

	public abstract void setUpdatedBy(String updatedBy);

	public abstract Timestamp getLastModified();

	public abstract void setLastModified(Timestamp lastModified);

	public abstract Timestamp getDateCreated();

	public abstract void setDateCreated(Timestamp dateCreated);

	public abstract Float getPitch();

	public abstract void setPitch(Float pitch);

	public abstract Float getYaw();

	public abstract void setYaw(Float yaw);

	public abstract boolean isBedHome();

	public abstract void setBedHome(boolean bedHome);

	public abstract boolean isDefaultHome();

	public abstract void setDefaultHome(boolean defaultHome);

	public abstract String getName();

	public abstract void setName(String name);

}