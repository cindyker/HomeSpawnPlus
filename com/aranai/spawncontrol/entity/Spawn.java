/**
 * 
 */
package com.aranai.spawncontrol.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
    
    @Length(max=32)
	private String name;
    
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
}
