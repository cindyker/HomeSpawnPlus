/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.entity.PlayerLastLocation;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializablePlayerLastLocation;

/**
 * @author morganm
 *
 */
public class PlayerLastLocationDAOYaml extends AbstractDAOYaml<PlayerLastLocation, SerializablePlayerLastLocation>
implements PlayerLastLocationDAO
{
	private static final String CONFIG_SECTION = "playerLastLocation";

	public PlayerLastLocationDAOYaml(final File file, final YamlConfiguration yaml) {
		super(CONFIG_SECTION);
		this.yaml = yaml;
		this.file = file;
	}
	public PlayerLastLocationDAOYaml(final File file) {
		this(file, null);
	}

	@Override
	public PlayerLastLocation findById(int id) {
		PlayerLastLocation playerLastLocation = null;
		
		Set<PlayerLastLocation> playerLastLocations = findAll();
		if( playerLastLocations != null && playerLastLocations.size() > 0 ) {
			for(PlayerLastLocation pll: playerLastLocations) {
				if( id == pll.getId() ) {
					playerLastLocation = pll;
					break;
				}
			}
		}
		
		return playerLastLocation;
	}

	@Override
	public PlayerLastLocation findByWorldAndPlayerName(String world, String playerName) {
		PlayerLastLocation playerLastLocation = null;
		
		Set<PlayerLastLocation> playerLastLocations = findAll();
		if( playerLastLocations != null && playerLastLocations.size() > 0 ) {
			for(PlayerLastLocation pll: playerLastLocations) {
				if( world.equals(pll.getWorld()) && playerName.equals(pll.getPlayerName()) ) {
					playerLastLocation = pll;
					break;
				}
			}
		}
		
		return playerLastLocation;
	}

	@Override
	public Set<PlayerLastLocation> findByPlayerName(String playerName) {
		Set<PlayerLastLocation> set = new HashSet<PlayerLastLocation>();
		
		Set<PlayerLastLocation> playerLastLocations = findAll();
		if( playerLastLocations != null && playerLastLocations.size() > 0 ) {
			for(PlayerLastLocation pll: playerLastLocations) {
				if( playerName.equals(pll.getPlayerName()) ) {
					set.add(pll);
				}
			}
		}
		
		return set;
	}

	@Override
	public Set<PlayerLastLocation> findAll() {
		return super.findAllObjects();
	}

	@Override
	public void save(PlayerLastLocation playerLastLocation) throws StorageException {
		super.saveObject(playerLastLocation);
	}

	@Override
	protected SerializablePlayerLastLocation newSerializable(PlayerLastLocation object) {
		return new SerializablePlayerLastLocation(object);
	}
}
