/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml.serialize;

import java.util.Map;

import org.bukkit.configuration.serialization.SerializableAs;
import org.morganm.homespawnplus.entity.Spawn;

/**
 * @author morganm
 *
 */
@SerializableAs("Spawn")
public class SerializableSpawn extends AbstractSerializableEntityWithLocation<Spawn>
implements SerializableYamlObject<Spawn>
{
	private final static String ATTR_NAME = "name";
	private final static String ATTR_GROUP = "group";
	private final static String ATTR_UPDATED_BY = "updatedBy";
	
	public SerializableSpawn(Spawn spawn) {
		super(spawn);
	}
	
	public Spawn deserialize(Map<String, Object> map) {
		super.deserialize(map);
		
		Object o = map.get(ATTR_NAME);
		if( o instanceof String )
			getObject().setName((String) o);
		o = map.get(ATTR_GROUP);
		if( o instanceof String )
			getObject().setGroup((String) o);
		o = map.get(ATTR_UPDATED_BY);
		if( o instanceof String )
			getObject().setUpdatedBy((String) o);
		
		return getObject();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put(ATTR_NAME, getObject().getName());
		map.put(ATTR_GROUP, getObject().getGroup());
		map.put(ATTR_UPDATED_BY, getObject().getUpdatedBy());
		return map;
	}

	@Override
	protected Spawn newEntity() {
		return new Spawn();
	}
}
