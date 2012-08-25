/**
 * 
 */
package org.morganm.homespawnplus.entity;

import java.sql.Timestamp;
import java.text.DecimalFormat;

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
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

/**
 * @author morganm
 *
 */
@Entity()
@Table(name="hsp_home",
	uniqueConstraints={
			@UniqueConstraint(columnNames={"player_name","name"})
		}
)
public class Home implements EntityWithLocation {
	static private final transient DecimalFormat decimalFormat = new DecimalFormat("#.##");
	
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    
    @NotEmpty
    @Length(max=32)
    @Column(name="player_name")
    private String playerName;
    
    @Length(max=32)
	private String name;
    
    @NotEmpty
    @Length(max=32)
    private String updatedBy;
    
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
	
	@NotNull
    @Column(name="bed_home")
	private boolean bedHome = false;
	@NotNull
    @Column(name="default_home")
	private boolean defaultHome = false;

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
		
		location = l;
    }
    
    public Location getLocation() {
    	if( location == null ) {
	    	World w = HomeSpawnPlus.getInstance().getServer().getWorld(world);
	    	location = new Location(w, getX(), getY(), getZ(), getYaw(), getPitch());
    	}
    	
    	return location;
    }
    
    public boolean equals(Object o) {
    	if( o == null )
    		return false;
    	if( !(o instanceof Home) )
    		return false;
    	if( o == this )			// java object equality check
    		return true;
    	
    	if( ((Home) o).getId() == getId() )
    		return true;
    	else
    		return false;
    }
    
    public String toString() {
    	return "{id="+getId()
    			+",name="+getName()
    			+",playerName="+getPlayerName()
    			+",world="+getWorld()
    			+",x="+decimalFormat.format(getX())
    			+",y="+decimalFormat.format(getY())
    			+",z="+decimalFormat.format(getZ())
    			+",yaw="+decimalFormat.format(getYaw())
    			+",pitch="+decimalFormat.format(getPitch())
    			+",bedHome="+isBedHome()
    			+",defaultHome="+isDefaultHome()
    			+"}";
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
		location = null;
	}
	public Double getX() {
		return x;
	}
	public void setX(Double x) {
		this.x = x;
		location = null;
	}
	public Double getY() {
		return y;
	}
	public void setY(Double y) {
		this.y = y;
		location = null;
	}
	public Double getZ() {
		return z;
	}
	public void setZ(Double z) {
		this.z = z;
		location = null;
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

	public Float getPitch() {
		return pitch;
	}

	public void setPitch(Float pitch) {
		this.pitch = pitch;
		location = null;
	}

	public Float getYaw() {
		return yaw;
	}

	public void setYaw(Float yaw) {
		this.yaw = yaw;
		location = null;
	}

	public boolean isBedHome() {
		return bedHome;
	}

	public void setBedHome(boolean bedHome) {
		this.bedHome = bedHome;
	}

	public boolean isDefaultHome() {
		return defaultHome;
	}

	public void setDefaultHome(boolean defaultHome) {
		this.defaultHome = defaultHome;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
