/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.entity.Player;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.PlayerDAO;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializablePlayer;

/**
 * @author morganm
 *
 */
public class PlayerDAOYaml extends AbstractDAOYaml<Player, SerializablePlayer> implements PlayerDAO {
	private static final String CONFIG_SECTION = "players";

	public PlayerDAOYaml(final File file, final YamlConfiguration yaml) {
		super(CONFIG_SECTION);
		this.yaml = yaml;
		this.file = file;
	}
	public PlayerDAOYaml(final File file) {
		this(file, null);
	}

	@Override
	public Player findPlayerByName(String name) {
		Player player = null;
		
		Set<Player> players = findAllPlayers();
		if( players != null && players.size() > 0 ) {
			for(Player p: players) {
				if( name.equals(p.getName()) ) {
					player = p;
					break;
				}
			}
		}
		
		return player;
	}

	@Override
	public Set<Player> findAllPlayers() {
		return super.findAllObjects();
	}

	@Override
	public void savePlayer(Player player) throws StorageException {
		super.saveObject(player);
	}

	@Override
	protected SerializablePlayer newSerializable(Player object) {
		return new SerializablePlayer(object);
	}
}
