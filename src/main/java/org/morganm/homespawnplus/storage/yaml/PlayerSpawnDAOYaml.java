/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.entity.PlayerSpawn;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializablePlayerSpawn;

/**
 * @author morganm
 *
 */
public class PlayerSpawnDAOYaml extends AbstractDAOYaml<PlayerSpawn, SerializablePlayerSpawn>
implements PlayerSpawnDAO
{
	private static final String CONFIG_SECTION = "playerSpawns";

	public PlayerSpawnDAOYaml(final File file, final YamlConfiguration yaml) {
		super(CONFIG_SECTION);
		this.yaml = yaml;
		this.file = file;
	}
	public PlayerSpawnDAOYaml(final File file) {
		this(file, null);
	}

	@Override
	public PlayerSpawn findById(int id) {
		PlayerSpawn playerSpawn = null;
		
		Set<PlayerSpawn> playerSpawns = findAll();
		if( playerSpawns != null && playerSpawns.size() > 0 ) {
			for(PlayerSpawn s: playerSpawns) {
				if( id == s.getId() ) {
					playerSpawn = s;
					break;
				}
			}
		}
		
		return playerSpawn;
	}

	@Override
	public PlayerSpawn findByWorldAndPlayerName(String world, String playerName) {
		PlayerSpawn playerSpawn = null;
		
		Set<PlayerSpawn> playerSpawns = findAll();
		if( playerSpawns != null && playerSpawns.size() > 0 ) {
			for(PlayerSpawn s: playerSpawns) {
				if( world.equals(s.getWorld()) && playerName.equals(s.getPlayerName()) ) {
					playerSpawn = s;
					break;
				}
			}
		}
		
		return playerSpawn;
	}

	@Override
	public Set<PlayerSpawn> findByPlayerName(String playerName) {
		Set<PlayerSpawn> set = new HashSet<PlayerSpawn>();
		
		Set<PlayerSpawn> playerSpawns = findAll();
		if( playerSpawns != null && playerSpawns.size() > 0 ) {
			for(PlayerSpawn s: playerSpawns) {
				if( playerName.equals(s.getPlayerName()) ) {
					set.add(s);
				}
			}
		}
		
		return set;
	}

	@Override
	public Set<PlayerSpawn> findAll() {
		return super.findAllObjects();
	}

	@Override
	public void save(PlayerSpawn playerSpawn) throws StorageException {
		super.saveObject(playerSpawn);
	}

	@Override
	public void delete(PlayerSpawn playerSpawn) throws StorageException {
		super.deleteObject(playerSpawn);
	}

	@Override
	protected SerializablePlayerSpawn newSerializable(PlayerSpawn object) {
		return new SerializablePlayerSpawn(object);
	}
}
