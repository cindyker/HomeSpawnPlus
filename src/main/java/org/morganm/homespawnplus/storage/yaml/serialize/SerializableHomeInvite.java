/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml.serialize;

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
	
	private final HomeSpawnPlus plugin;
	
	public SerializableHomeInvite(HomeSpawnPlus plugin) {
		super();
		this.plugin = plugin;
	}
	public SerializableHomeInvite(HomeSpawnPlus plugin, HomeInvite homeInvite) {
		super(homeInvite);
		this.plugin = plugin;
	}
	
	public HomeInvite deserialize(Map<String, Object> map) {
		super.deserialize(map);
		
		Object o = map.get(ATTR_HOME);
		if( o instanceof Integer ) {
			Home h = plugin.getStorage().getHomeDAO().findHomeById((Integer) o);
			if( h != null )
				getObject().setHome(h);
		}
		o = map.get(ATTR_INVITED_PLAYER);
		if( o instanceof String )
			getObject().setInvitedPlayer((String) o);
		
		return getObject();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put(ATTR_HOME, getObject().getHome().getId());
		map.put(ATTR_INVITED_PLAYER, getObject().getInvitedPlayer());
		return map;
	}

	@Override
	protected HomeInvite newEntity() {
		return new HomeInvite();
	}

}
