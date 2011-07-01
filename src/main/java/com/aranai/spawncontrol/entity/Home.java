/**
 * 
 */
package com.aranai.spawncontrol.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.bukkit.Location;
import org.bukkit.World;

import com.aranai.spawncontrol.SpawnControl;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

/**
 * @author morganm
 *
 */
@Entity()
@Table(name="MSC_Home",
		uniqueConstraints=
			@UniqueConstraint(columnNames={"world", "player_name"})
)
public class Home {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    
    @NotEmpty
    @Length(max=32)
    @Column(name="player_name")
    private String playerName;
    
    @NotEmpty
    @Length(max=32)
    private String updatedBy;
    
    @NotEmpty
    @Length(max=32)
	private String world;
    
    @NotNull
    private double x;
    @NotNull
    private double y;
    @NotNull
    private double z;
    
    private float pitch;
	private float yaw;
	
	@Version
	private Timestamp lastModified;
	
	@CreatedTimestamp
	private Timestamp dateCreated;
	
    @Transient
    private transient Location location;
    
    public Home() {}
    
    /** Create a new Home object given the player and location.
     * 
     * @param playerName
     * @param l
     */
    public Home(String playerName, Location l, String updatedBy) {
    	setPlayerName(playerName);
    	setLocation(l);
    	setUpdatedBy(updatedBy);
    }
    
    public void setLocation(Location l) {
    	setWorld(l.getWorld().getName());
		setX(l.getX());
		setY(l.getY());
		setZ(l.getZ());
		setYaw(l.getYaw());
		setPitch(l.getPitch());
    }
    
    public Location getLocation() {
    	if( location == null ) {
	    	World w = SpawnControl.getInstance().getServer().getWorld(world);
	    	location = new Location(w, x, y, z, yaw, pitch);
    	}
    	
    	return location;
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
		return world;
	}
	public void setWorld(String world) {
		this.world = world;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getZ() {
		return z;
	}
	public void setZ(double z) {
		this.z = z;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
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

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}
}
