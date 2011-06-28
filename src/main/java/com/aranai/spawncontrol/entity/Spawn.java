/**
 * 
 */
package com.aranai.spawncontrol.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.bukkit.Location;
import org.bukkit.World;

import com.aranai.spawncontrol.SpawnControl;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;

/**
 * @author morganm
 *
 */
@Entity()
@Table(name="MSC_Spawn",
		uniqueConstraints={
			@UniqueConstraint(columnNames={"name"}),
			@UniqueConstraint(columnNames={"world", "group"})
		}
)
public class Spawn {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    
    @NotEmpty
    @Length(max=32)
	private String world;
    
    // NOTUSED: TODO add spawn by name in the future
    @Length(max=32)
	private String name;
    
    @NotEmpty
    @Length(max=32)
    private String updatedBy;
    
    /* Optional group associated with this spawn.
     */
    @Length(max=32)
	private String group;
    
    @NotEmpty
    private double x;
    @NotEmpty
    private double y;
    @NotEmpty
    private double z;
    
    @Transient
    private transient Location location;
    
    public Spawn() {}
    
    /** Create a new spawn object.
     * 
     * @param l
     * @param updatedBy
     * @param group the Group this spawn represents. Can be null to represent global spawn for the given world.
     */
    public Spawn(Location l, String updatedBy, String group) {
    	setWorld(l.getWorld().getName());
    	setX(l.getX());
    	setY(l.getY());
    	setZ(l.getZ());
    	setUpdatedBy(updatedBy);
    	setGroup(group);
    }
    
    public Location getLocation() {
    	if( location == null ) {
	    	World w = SpawnControl.getInstance().getServer().getWorld(world);
	    	location = new Location(w, x, y, z);
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
}
