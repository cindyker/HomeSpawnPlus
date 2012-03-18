/**
 * 
 */
package org.morganm.homespawnplus.entity;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

/** Class to keep track of players we've seen before, so we can tell if it's a new player or not.
 * 
 * @author morganm
 *
 */
@Entity()
@Table(name="hsp_player",
		uniqueConstraints={
			@UniqueConstraint(columnNames={"name"})
		}
)
public class Player implements EntityWithLocation {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    
    @Length(max=32)
    @NotNull
	private String name;

    @Length(max=32)
	private String world;
    private Double x;
    private Double y;
    private Double z;
    
    private Float pitch;
	private Float yaw;
	
	@Version
	private Timestamp lastModified;
	
	@CreatedTimestamp
	private Timestamp dateCreated;
	
    public Player() {}
    public Player(org.bukkit.entity.Player player) {
    	this.name = player.getName();
    }
    
    /** Update last logout location to the given location.
     * 
     * @param p
     */
    public void updateLastLogoutLocation(Location l) {
    	setWorld(l.getWorld().getName());
		setX(l.getX());
		setY(l.getY());
		setZ(l.getZ());
		setYaw(l.getYaw());
		setPitch(l.getPitch());
    }
    
    public Location getLastLogoutLocation() {
    	if( getWorld() == null )
    		return null;
    	
    	World w = Bukkit.getWorld(getWorld());
    	return new Location(w, getX(), getY(), getZ(), getYaw(), getPitch());
    }
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	public String getWorld() {
		return world;
	}
	public void setWorld(String world) {
		this.world = world;
	}
	public Double getX() {
		return x;
	}
	public void setX(Double x) {
		this.x = x;
	}
	public Double getY() {
		return y;
	}
	public void setY(Double y) {
		this.y = y;
	}
	public Double getZ() {
		return z;
	}
	public void setZ(Double z) {
		this.z = z;
	}
	public Float getPitch() {
		return pitch;
	}
	public void setPitch(Float pitch) {
		this.pitch = pitch;
	}
	public Float getYaw() {
		return yaw;
	}
	public void setYaw(Float yaw) {
		this.yaw = yaw;
	}
}
