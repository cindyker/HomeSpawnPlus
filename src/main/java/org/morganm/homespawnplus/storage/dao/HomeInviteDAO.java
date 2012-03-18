/**
 * 
 */
package org.morganm.homespawnplus.storage.dao;

import java.util.Set;

import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.HomeInvite;
import org.morganm.homespawnplus.storage.StorageException;

/**
 * @author morganm
 *
 */
public interface HomeInviteDAO {
	/** Given a specific home and a specific invitee, find the HomeInvite
	 * object. This will return null if no such relationship exists.
	 * 
	 * @param home
	 * @param invitee
	 * @return
	 */
	public HomeInvite findInviteByHomeAndInvitee(Home home, String invitee);
	
	/** Given a home, return all HomeInvite objects that might be attached to it.
	 * 
	 * @param home
	 * @return
	 */
	public Set<HomeInvite> findInvitesByHome(Home home);
	
	/** Given an invitee, return all available invites open to them.
	 * 
	 * @param invitee
	 * @return
	 */
	public Set<HomeInvite> findAllAvailableInvites(String invitee);
	
	/** Given a player, return all open invites that we have
	 * outstanding.
	 * 
	 * @param invitee
	 * @return
	 */
	public Set<HomeInvite> findAllOpenInvites(String player);
	
	public Set<HomeInvite> findAllHomeInvites();
	
	public void saveHomeInvite(HomeInvite homeInvite) throws StorageException;
	public void deleteHomeInvite(HomeInvite homeInvite) throws StorageException;
}
