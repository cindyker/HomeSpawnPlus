/**
 * 
 */
package org.morganm.homespawnplus.entity;

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
import org.morganm.homespawnplus.HomeSpawnPlus;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

/**
 * @author morganm
 *
 */
@Entity()
@Table(name="hsp_spawn",
		uniqueConstraints={
			@UniqueConstraint(columnNames={"name"}),
			@UniqueConstraint(columnNames={"world", "group_name"})
		}
)
public class Spawn {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    
    @NotNull
    @Length(max=32)
	private String world;
    
    @Length(max=32)
	private String name;
    
    @NotNull
    @Length(max=32)
    private String updatedBy;
    
    /* Optional group associated with this spawn.
     */
    @Length(max=32)
    @Column(name="group_name")
	private String group;
    
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
    
    public Spawn() {}
    
    /** Create a new spawn object.
     * 
     * @param l
     * @param updatedBy
     * @param group the Group this spawn represents. Can be null to represent global spawn for the given world.
     */
    public Spawn(Location l, String updatedBy) {
    	setLocation(l);
    	setUpdatedBy(updatedBy);
//    	setGroup(group);
    }
    
    public void setLocation(Location l) {
    	setWorld(l.getWorld().getName());
		setX(l.getX());
		setY(l.getY());
		setZ(l.getZ());
		setYaw(l.getYaw());
		setPitch(l.getPitch());
		
		location = l;
    }
    
    public Location getLocation() {
    	if( location == null ) {
	    	World w = HomeSpawnPlus.getInstance().getServer().getWorld(world);
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
	public String getWorld() {
		return world;
	}
	public void setWorld(String world) {
		this.world = world;
		location = null;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
		location = null;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
		location = null;
	}
	public double getZ() {
		return z;
	}
	public void setZ(double z) {
		this.z = z;
		location = null;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	
    public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
		location = null;
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
		location = null;
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
}
