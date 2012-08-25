/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml.serialize;

import java.util.Map;

import org.bukkit.configuration.serialization.SerializableAs;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.storage.yaml.StorageYaml;
import org.morganm.homespawnplus.util.Debug;

/** Class which maps Home entity to YAML using Bukkit's ConfigurationSerializable
 * interface.
 * 
 * @author morganm
 *
 */
@SerializableAs("Home")
public class SerializableHome extends AbstractSerializableEntityWithLocation<Home>
implements SerializableYamlObject<Home>
{
	private final static String ATTR_NAME = "name";
	private final static String ATTR_PLAYER_NAME = "player_name";
	private final static String ATTR_UPDATED_BY = "updatedBy";
	private final static String ATTR_DEFAULT_HOME = "defaultHome";
	private final static String ATTR_BED_HOME = "bedHome";
	
	public SerializableHome(Home home) {
		super(home);
	}
	
	public SerializableHome(Map<String, Object> map) {
		super(map);
		
		Debug.getInstance().devDebug("SerializeHome constructor, map=",map);
		
		Object o = map.get(ATTR_NAME);
		if( o instanceof String )
			getObject().setName((String) o);
		o = map.get(ATTR_PLAYER_NAME);
		if( o instanceof String )
			getObject().setPlayerName((String) o);
		o = map.get(ATTR_UPDATED_BY);
		if( o instanceof String )
			getObject().setUpdatedBy((String) o);
		o = map.get(ATTR_BED_HOME);
		if( o instanceof Boolean )
			getObject().setBedHome((Boolean) o);
		o = map.get(ATTR_DEFAULT_HOME);
		if( o instanceof Boolean )
			getObject().setDefaultHome((Boolean) o);
		
		// tell the currently loading StorageYaml that this object has been
		// loaded. @see org.morganm.homespawnplus.storage.yaml.HomeDAYYaml.temporaryAllObjects
		if( StorageYaml.getCurrentlyInitializingInstance() != null ) {
			StorageYaml.getCurrentlyInitializingInstance().homeLoaded(getObject());
		}
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put(ATTR_NAME, getObject().getName());
		map.put(ATTR_PLAYER_NAME, getObject().getPlayerName());
		map.put(ATTR_UPDATED_BY, getObject().getUpdatedBy());
		map.put(ATTR_BED_HOME, getObject().isBedHome());
		map.put(ATTR_DEFAULT_HOME, getObject().isDefaultHome());
		return map;
	}

	@Override
	protected Home newEntity() {
		return new Home();
	}
}
