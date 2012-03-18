/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.HomeDAO;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableHome;

/**
 * @author morganm
 *
 */
public class HomeDAOYaml extends AbstractDAOYaml<Home, SerializableHome> implements HomeDAO {
	private static final String CONFIG_SECTION = "homes";
	
	public HomeDAOYaml(final File file, final YamlConfiguration yaml) {
		super(CONFIG_SECTION);
		this.yaml = yaml;
		this.file = file;
	}
	public HomeDAOYaml(final File file) {
		this(file, null);
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findHomeById(int)
	 */
	@Override
	public Home findHomeById(int id) {
		Home home = null;
		
		Set<Home> homes = findAllHomes();
		if( homes != null && homes.size() > 0 ) {
			for(Home h: homes) {
				if( id == h.getId() ) {
					home = h;
					break;
				}
			}
		}
		
		return home;
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findDefaultHome(java.lang.String, java.lang.String)
	 */
	@Override
	public Home findDefaultHome(String world, String playerName) {
		Home home = null;
		
		Set<Home> homes = findAllHomes();
		if( homes != null && homes.size() > 0 ) {
			for(Home h: homes) {
				if( h.isDefaultHome() && playerName.equals(h.getPlayerName()) && world.equals(h.getWorld()) ) {
					home = h;
					break;
				}
			}
		}
		
		return home;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findBedHome(java.lang.String, java.lang.String)
	 */
	@Override
	public Home findBedHome(String world, String playerName) {
		Home home = null;
		
		Set<Home> homes = findAllHomes();
		if( homes != null && homes.size() > 0 ) {
			for(Home h: homes) {
				if( h.isBedHome() && playerName.equals(h.getPlayerName()) && world.equals(h.getWorld()) ) {
					home = h;
					break;
				}
			}
		}
		
		return home;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findHomeByNameAndPlayer(java.lang.String, java.lang.String)
	 */
	@Override
	public Home findHomeByNameAndPlayer(String homeName, String playerName) {
		Home home = null;
		
		Set<Home> homes = findAllHomes();
		if( homes != null && homes.size() > 0 ) {
			for(Home h: homes) {
				if( homeName.equals(h.getName()) && playerName.equals(h.getPlayerName()) ) {
					home = h;
					break;
				}
			}
		}
		
		return home;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findHomesByWorldAndPlayer(java.lang.String, java.lang.String)
	 */
	@Override
	public Set<Home> findHomesByWorldAndPlayer(String world, String playerName) {
		Set<Home> homes = new HashSet<Home>(4);
		
		Set<Home> allHomes = findAllHomes();
		if( allHomes != null && allHomes.size() > 0 ) {
			for(Home h: allHomes) {
				if( playerName.equals(h.getPlayerName()) && world.equals(h.getWorld()) ) {
					homes.add(h);
				}
			}
		}
		
		return homes;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findHomesByPlayer(java.lang.String)
	 */
	@Override
	public Set<Home> findHomesByPlayer(String playerName) {
		Set<Home> homes = new HashSet<Home>(4);
		
		Set<Home> allHomes = findAllHomes();
		if( allHomes != null && allHomes.size() > 0 ) {
			for(Home h: allHomes) {
				if( playerName.equals(h.getPlayerName()) ) {
					homes.add(h);
				}
			}
		}
		
		return homes;
	}

	@Override
	public Set<Home> findAllHomes() {
		return super.findAllObjects();
	}
	
	@Override
	public void saveHome(Home home) throws StorageException {
		super.saveObject(home);
	}

	@Override
	public void deleteHome(Home home) throws StorageException {
		super.deleteObject(home);
	}
	
	@Override
	protected SerializableHome newSerializable(Home object) {
		return new SerializableHome(object);
	}
}
