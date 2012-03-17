/**
 * 
 */
package org.morganm.homespawnplus.storage.ebean;

import java.util.HashSet;
import java.util.Set;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.HomeInvite;
import org.morganm.homespawnplus.storage.dao.HomeInviteDAO;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;

/**
 * @author morganm
 *
 */
public class HomeInviteDAOEBean implements HomeInviteDAO {
	private EbeanServer ebean;
	private HomeSpawnPlus plugin;
	
	public HomeInviteDAOEBean(final EbeanServer ebean, final HomeSpawnPlus plugin) {
		this.plugin = plugin;
		setEbeanServer(ebean);
	}
	
	public void setEbeanServer(final EbeanServer ebean) {
		this.ebean = ebean;
	}

	@Override
	public HomeInvite findInviteByHomeAndInvitee(Home home, String invitee) {
		String q = "find homeInvite where home = :home and invitedPlayer = :invitee";
		
		Query<HomeInvite> query = ebean.createQuery(HomeInvite.class, q);
		query.setParameter("home", home.getId());
		query.setParameter("invitee", invitee);
		
		return query.findUnique();
	}

	@Override
	public Set<HomeInvite> findInvitesByHome(Home home) {
		String q = "find homeInvite where home = :home";
		Query<HomeInvite> query = ebean.createQuery(HomeInvite.class, q);
		query.setParameter("home", home.getId());
		
		return query.findSet();
	}

	@Override
	public Set<HomeInvite> findAllAvailableInvites(String invitee) {
		String q = "find homeInvite where invitedPlayer = :invitee";
		Query<HomeInvite> query = ebean.createQuery(HomeInvite.class, q);
		query.setParameter("invitee", invitee);
		
		return query.findSet();
	}

	@Override
	public Set<HomeInvite> findAllOpenInvites(String player) {
		Set<HomeInvite> invites = new HashSet<HomeInvite>(5);
		
		// first find all homes for this player
		Set<Home> homes = plugin.getStorage().getHomeDAO().findHomesByPlayer(player);
		if( homes == null || homes.size() == 0 )
			return invites;

		// then find all HomeInvites related to any of those homes
		for(Home home : homes) {
			Set<HomeInvite> homeInvites = findInvitesByHome(home);
			if( homeInvites != null )
				invites.addAll(homeInvites);
		}
		
		return invites;
	}

	@Override
	public void saveHomeInvite(HomeInvite homeInvite) {
        ebean.save(homeInvite);
	}


}
