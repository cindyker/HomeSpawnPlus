/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml.serialize;

import java.util.Map;

import org.bukkit.configuration.serialization.SerializableAs;
import org.morganm.homespawnplus.entity.PlayerLastLocation;
import org.morganm.homespawnplus.util.Debug;

/**
 * @author morganm
 *
 */
@SerializableAs("PlayerLastLocation")
public class SerializablePlayerLastLocation extends AbstractSerializableEntityWithLocation<PlayerLastLocation>
implements SerializableYamlObject<PlayerLastLocation>
{
	private final static String ATTR_PLAYER_NAME = "player_name";
	
	public SerializablePlayerLastLocation(PlayerLastLocation playerLastLocation) {
		super(playerLastLocation);
	}

	public SerializablePlayerLastLocation(Map<String, Object> map) {
		super(map);
		
		Debug.getInstance().devDebug("SerializablePlayerLastLocation constructor, map=",map);

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
	protected PlayerLastLocation newEntity() {
		return new PlayerLastLocation();
	}

}
