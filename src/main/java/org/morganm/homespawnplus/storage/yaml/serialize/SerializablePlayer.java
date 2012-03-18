/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml.serialize;

import java.util.Map;

import org.bukkit.configuration.serialization.SerializableAs;
import org.morganm.homespawnplus.entity.Player;

/**
 * @author morganm
 *
 */
@SerializableAs("Player")
public class SerializablePlayer extends AbstractSerializableEntityWithLocation<Player>
implements SerializableYamlObject<Player>
{
	private final static String ATTR_NAME = "name";
	
	public SerializablePlayer(Player player) {
		super(player);
	}
	
	public Player deserialize(Map<String, Object> map) {
		super.deserialize(map);
		
		Object o = map.get(ATTR_NAME);
		if( o instanceof String )
			getObject().setName((String) o);
		
		return getObject();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put(ATTR_NAME, getObject().getName());
		return map;
	}

	@Override
	protected Player newEntity() {
		return new Player();
	}

}
