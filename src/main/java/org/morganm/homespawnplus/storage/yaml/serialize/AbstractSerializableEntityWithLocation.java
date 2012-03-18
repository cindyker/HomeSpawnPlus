/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml.serialize;

import java.util.Map;

import org.morganm.homespawnplus.entity.EntityWithLocation;

/** Abstract entity location data so the same code doesn't have to be repeated
 * for each entity.
 * 
 * @author morganm
 *
 */
public abstract class AbstractSerializableEntityWithLocation<T extends EntityWithLocation> extends
		AbstractSerializableBasicEntity<T> {
	private final static String ATTR_WORLD = "world";
	private final static String ATTR_X = "x";
	private final static String ATTR_Y = "y";
	private final static String ATTR_Z = "z";
	private final static String ATTR_PITCH = "pitch";
	private final static String ATTR_YAW = "yaw";
	
	public AbstractSerializableEntityWithLocation() {
		super();
	}
	public AbstractSerializableEntityWithLocation(T object) {
		super(object);
	}

	public T deserialize(Map<String, Object> map) {
		super.deserialize(map);
		
		Object o = map.get(ATTR_WORLD);
		if( o instanceof String )
			object.setWorld((String) o);
		o = map.get(ATTR_X);
		if( o instanceof Double )
			object.setX((Double) o);
		o = map.get(ATTR_Y);
		if( o instanceof Double )
			object.setY((Double) o);
		o = map.get(ATTR_Z);
		if( o instanceof Double )
			object.setZ((Double) o);
		o = map.get(ATTR_PITCH);
		if( o instanceof Double )
			object.setPitch(((Double) o).floatValue());
		o = map.get(ATTR_YAW);
		if( o instanceof Double )
			object.setYaw(((Double) o).floatValue());
		
		return getObject();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put(ATTR_WORLD, getObject().getWorld());
		map.put(ATTR_X, getObject().getX());
		map.put(ATTR_Y, getObject().getY());
		map.put(ATTR_Z, getObject().getZ());
		map.put(ATTR_PITCH, getObject().getPitch());
		map.put(ATTR_YAW, getObject().getYaw());
		return map;
	}
}
