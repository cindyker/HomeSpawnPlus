/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.HomeInvite;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.HomeInviteDAO;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableHomeInvite;

/**
 * @author morganm
 *
 */
public class HomeInviteDAOYaml extends AbstractDAOYaml<HomeInvite, SerializableHomeInvite> implements HomeInviteDAO {
	private static final String CONFIG_SECTION = "homeInvites";
	
	private final HomeSpawnPlus plugin;
	
	public HomeInviteDAOYaml(final HomeSpawnPlus plugin, final File file, final YamlConfiguration yaml) throws IOException, InvalidConfigurationException {
		super(CONFIG_SECTION);
		this.plugin = plugin;
		this.yaml = yaml;
		this.file = file;
		load();
	}
	public HomeInviteDAOYaml(final HomeSpawnPlus plugin, final File file) throws IOException, InvalidConfigurationException {
		this(plugin, file, null);
	}
	
	@Override
	public HomeInvite findInviteByHomeAndInvitee(Home home, String invitee) {
		HomeInvite homeInvite = null;
		
		Set<HomeInvite> homeInvites = findAllObjects();
		if( homeInvites != null && homeInvites.size() > 0 ) {
			for(HomeInvite h: homeInvites) {
				if( home.equals(h) && invitee.equals(h.getInvitedPlayer())) {
					homeInvite = h;
					break;
				}
			}
		}
		
		return homeInvite;
	}
	@Override
	public Set<HomeInvite> findInvitesByHome(Home home) {
		Set<HomeInvite> set = new HashSet<HomeInvite>(5);
		
		Set<HomeInvite> homeInvites = findAllObjects();
		if( homeInvites != null && homeInvites.size() > 0 ) {
			for(HomeInvite h: homeInvites) {
				if( home.equals(h) ) {
					set.add(h);
				}
			}
		}
		
		return set;
	}
	@Override
	public Set<HomeInvite> findAllAvailableInvites(String invitee) {
		Set<HomeInvite> set = new HashSet<HomeInvite>(5);
		
		Set<HomeInvite> homeInvites = findAllObjects();
		if( homeInvites != null && homeInvites.size() > 0 ) {
			for(HomeInvite h: homeInvites) {
				if( invitee.equals(h.getInvitedPlayer()) ) {
					set.add(h);
				}
			}
		}
		
		return set;
	}
	
	@Override
	public Set<HomeInvite> findAllOpenInvites(String player) {
		Set<HomeInvite> set = new HashSet<HomeInvite>(5);
		
		Set<HomeInvite> homeInvites = findAllObjects();
		if( homeInvites != null && homeInvites.size() > 0 ) {
			for(HomeInvite h: homeInvites) {
				if( player.equals(h.getHome().getPlayerName()) ) {
					set.add(h);
				}
			}
		}
		
		return set;
	}

	@Override
	public Set<HomeInvite> findAllHomeInvites() {
		return super.findAllObjects();
	}

	@Override
	public void saveHomeInvite(HomeInvite homeInvite) throws StorageException {
		super.saveObject(homeInvite);
	}
	@Override
	public void deleteHomeInvite(HomeInvite homeInvite) throws StorageException {
		super.deleteObject(homeInvite);
	}
	@Override
	protected SerializableHomeInvite newSerializable(HomeInvite object) {
		return new SerializableHomeInvite(plugin);
	}
}
