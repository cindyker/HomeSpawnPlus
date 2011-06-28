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
@Table(name="MSC_Home",
		uniqueConstraints=
			@UniqueConstraint(columnNames={"world", "playerName"})
)
public class Home {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    
    @NotEmpty
    @Length(max=32)
    private String playerName;
    
    @NotEmpty
    @Length(max=32)
	private String world;
    
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
}
