/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml.serialize;

import java.util.Map;

import org.bukkit.configuration.serialization.SerializableAs;
import org.morganm.homespawnplus.entity.PlayerSpawn;
import org.morganm.homespawnplus.util.Debug;

/**
 * @author morganm
 *
 */
@SerializableAs("PlayerSpawn")
public class SerializablePlayerSpawn extends AbstractSerializableEntityWithLocation<PlayerSpawn>
implements SerializableYamlObject<PlayerSpawn>
{
	private final static String ATTR_PLAYER_NAME = "player_name";
	
	public SerializablePlayerSpawn(PlayerSpawn playerSpawn) {
		super(playerSpawn);
	}

	public SerializablePlayerSpawn(Map<String, Object> map) {
		super(map);
		
		Debug.getInstance().devDebug("SerializablePlayerSpawn constructor, map=",map);

		Object o = map.get(ATTR_PLAYER_NAME);
		if( o instanceof String )
			getObject().setPlayerName((String) o);
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put(ATTR_PLAYER_NAME, getObject().getPlayerName());
		return map;
	}

	@Override
	protected PlayerSpawn newEntity() {
		return new PlayerSpawn();
	}
}
