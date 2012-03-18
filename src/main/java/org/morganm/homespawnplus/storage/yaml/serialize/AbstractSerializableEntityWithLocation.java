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
	
	public AbstractSerializableEntityWithLocation(T object) {
		super(object);
	}

	public AbstractSerializableEntityWithLocation(Map<String, Object> map) {
		super(map);
		
		Object o = map.get(ATTR_WORLD);
		if( o instanceof String )
			getObject().setWorld((String) o);
		o = map.get(ATTR_X);
		if( o instanceof Double )
			getObject().setX((Double) o);
		o = map.get(ATTR_Y);
		if( o instanceof Double )
			getObject().setY((Double) o);
		o = map.get(ATTR_Z);
		if( o instanceof Double )
			getObject().setZ((Double) o);
		o = map.get(ATTR_PITCH);
		if( o instanceof Double )
			getObject().setPitch(((Double) o).floatValue());
		o = map.get(ATTR_YAW);
		if( o instanceof Double )
			getObject().setYaw(((Double) o).floatValue());
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
