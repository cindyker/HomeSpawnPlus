/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml.serialize;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.morganm.homespawnplus.entity.BasicEntity;

/** Abstract basic entity data members so the same code doesn't have to be repeated
 * for each entity.
 * 
 * @author morganm
 *
 */
public abstract class AbstractSerializableBasicEntity<T extends BasicEntity> implements ConfigurationSerializable {
	private final static String ATTR_ID = "id";
	private final static String ATTR_LAST_MODIFIED = "lastModified";
	private final static String ATTR_DATE_CREATED = "dateCreated";
	
	protected T object;
	
	public T getObject() { return object; }
	protected void setObject(T object) { this.object = object; } 
	
	public AbstractSerializableBasicEntity(T object) {
		setObject(object);
	}

	public AbstractSerializableBasicEntity(Map<String, Object> map) {
		setObject(newEntity());
		
		Object o = map.get(ATTR_ID);
		if( o instanceof Integer )
			object.setId((Integer) o);
		o = map.get(ATTR_LAST_MODIFIED);
		if( o instanceof Long )
			object.setLastModified(new Timestamp((Long) o));
		o = map.get(ATTR_DATE_CREATED);
		if( o instanceof Long )
			object.setDateCreated(new Timestamp((Long) o));
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new LinkedHashMap<String, Object>(15);
		map.put(ATTR_ID, getObject().getId());
		map.put(ATTR_LAST_MODIFIED, getObject().getLastModified().getTime());
		map.put(ATTR_DATE_CREATED, getObject().getDateCreated().getTime());
		return map;
	}

	protected abstract T newEntity();
}
