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
public class Player {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    
    @Length(max=32)
    @NotNull
	private String name;

	@Version
	private Timestamp lastModified;
	
	@CreatedTimestamp
	private Timestamp dateCreated;
    
    public Player() {}
    public Player(org.bukkit.entity.Player player) {
    	this.name = player.getName();
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
}
