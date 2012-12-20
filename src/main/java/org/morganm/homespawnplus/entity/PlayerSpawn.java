/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.morganm.homespawnplus.entity;

import java.sql.Timestamp;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.morganm.homespawnplus.server.api.Location;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

/** Players can have a "personal" spawn, either pointing to another
 * real spawn or a random location. For example, if you have a world
 * with a random spawn point, you might want to record where the
 * player spawns and that's where they always spawn on that world.
 * 
 * @author morganm
 *
 */
@Entity()
@Table(name="hsp_playerspawn",
		uniqueConstraints={
			@UniqueConstraint(columnNames={"world", "player_name"})
		}
)
public class PlayerSpawn implements EntityWithLocation
{
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    
    @NotEmpty
    @Length(max=32)
    @Column(name="player_name")
    private String playerName;
    
    @NotEmpty
    @Length(max=32)
	private String world;
    
    @NotNull
    private Double x;
    @NotNull
    private Double y;
    @NotNull
    private Double z;
    
    private Float pitch;
	private Float yaw;
	
	@Version
	private Timestamp lastModified;
	
	@CreatedTimestamp
	private Timestamp dateCreated;
	
	@ManyToOne
	@Nullable
	private Spawn spawn;
	
    @Transient
    private transient Location location;
    
    public PlayerSpawn() {}

    /** If this PlayerSpawn is mapped to a real spawn, then this will return
     * that Spawn object.
     * 
     * @return
     */
	public Spawn getSpawn() {
		return spawn;
	}
	public void setSpawn(Spawn spawn) {
		this.spawn = spawn;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getWorld() {
		if( spawn != null )
			return spawn.getWorld();
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public Double getX() {
		if( spawn != null )
			return spawn.getX();
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		if( spawn != null )
			return spawn.getY();
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public Double getZ() {
		if( spawn != null )
			return spawn.getZ();
		return z;
	}

	public void setZ(Double z) {
		this.z = z;
	}

	public Float getPitch() {
		if( spawn != null )
			return spawn.getPitch();
		return pitch;
	}

	public void setPitch(Float pitch) {
		this.pitch = pitch;
	}

	public Float getYaw() {
		if( spawn != null )
			return spawn.getYaw();
		return yaw;
	}

	public void setYaw(Float yaw) {
		this.yaw = yaw;
	}

	public Timestamp getLastModified() {
		return lastModified;
	}

	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Location getLocation() {
		if( spawn != null )
			return spawn.getLocation();
		
    	if( location == null ) {
            location = ObjectFactory.newLocation(world, x, y, z, yaw, pitch);
    	}
		return location;
	}

	public void setLocation(Location location) {
    	setWorld(location.getWorld().getName());
		setX(location.getX());
		setY(location.getY());
		setZ(location.getZ());
		setYaw(location.getYaw());
		setPitch(location.getPitch());
		
		this.location = location;
	}
	
}
