/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml.serialize;

import java.sql.Timestamp;
import java.util.Map;

import org.bukkit.configuration.serialization.SerializableAs;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.HomeInvite;

/**
 * @author morganm
 *
 */
@SerializableAs("HomeInvite")
public class SerializableHomeInvite extends AbstractSerializableBasicEntity<HomeInvite>
implements SerializableYamlObject<HomeInvite>
{
	private final static String ATTR_HOME = "home";
	private final static String ATTR_INVITED_PLAYER = "invitedPlayer";
	private final static String ATTR_EXPIRES = "expires";
	
	public SerializableHomeInvite(HomeInvite homeInvite) {
		super(homeInvite);
	}
	
	public SerializableHomeInvite(Map<String, Object> map) {
		super(map);
		
		Object o = map.get(ATTR_HOME);
		if( o instanceof Integer ) {
			Home h = HomeSpawnPlus.getInstance().getStorage().getHomeDAO().findHomeById((Integer) o);
			if( h != null )
				getObject().setHome(h);
		}
		o = map.get(ATTR_INVITED_PLAYER);
		if( o instanceof String )
			getObject().setInvitedPlayer((String) o);
		o = map.get(ATTR_EXPIRES);
		if( o instanceof Long )
			getObject().setExpires(new Timestamp((Long) o));
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put(ATTR_HOME, getObject().getHome().getId());
		map.put(ATTR_INVITED_PLAYER, getObject().getInvitedPlayer());
		map.put(ATTR_EXPIRES, getObject().getExpires());
		return map;
	}

	@Override
	protected HomeInvite newEntity() {
		return new HomeInvite();
	}

}
