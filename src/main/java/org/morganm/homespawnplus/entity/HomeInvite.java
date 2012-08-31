/**
 * 
 */
package org.morganm.homespawnplus.entity;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

/**
 * @author morganm
 *
 */
@Entity()
@Table(name="hsp_homeinvite",
	uniqueConstraints={
			@UniqueConstraint(columnNames={"home_id", "invited_player"})
		}
)
public class HomeInvite implements BasicEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;

    @NotNull
    @OneToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @Column(name="home_id")
    private Home home;
    
    @NotNull
    @Length(max=32)
    @Column(name="invited_player")
    private String invitedPlayer;

    /* If this invite is temporary, the expiration time is recorded here. If the
     * invite is permanent, this will be null.
     * 
     */
	private Date expires;
	
	@Version
	private Timestamp lastModified;
	
	@CreatedTimestamp
	private Timestamp dateCreated;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Home getHome() {
		return home;
	}

	public void setHome(Home home) {
		this.home = home;
	}

	public String getInvitedPlayer() {
		return invitedPlayer;
	}

	public void setInvitedPlayer(String invitedPlayer) {
		this.invitedPlayer = invitedPlayer;
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

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}
}
