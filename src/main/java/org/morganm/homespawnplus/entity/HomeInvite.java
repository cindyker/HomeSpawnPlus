/**
 * 
 */
package org.morganm.homespawnplus.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;

/**
 * @author morganm
 *
 */
@Entity()
@Table(name="hsp_homeinvite",
	uniqueConstraints={
			@UniqueConstraint(columnNames={"home", "invited_player"})
		}
)
public class HomeInvite {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;

    @OneToOne
    @Column(name="home")
    private Home home;
    
    @NotEmpty
    @Length(max=32)
    @Column(name="invited_player")
    private String invitedPlayer;

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
}
