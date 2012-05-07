/**
 * 
 */
package org.morganm.homespawnplus.dynmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.World;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.storage.dao.HomeDAO;

/**
 * @author morganm
 *
 */
public class HomeLocationManager implements LocationManager {
	private final HomeSpawnPlus plugin;

	public HomeLocationManager(final HomeSpawnPlus plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public List<NamedLocation> getLocations(World w) {
		List<NamedLocation> locations = new ArrayList<NamedLocation>();
		
		String world = w.getName();
		
		HomeDAO dao = plugin.getStorage().getHomeDAO();
		Set<Home> allHomes = dao.findAllHomes();
		if( allHomes != null && allHomes.size() > 0 ) {
			for(Home home : allHomes) {
				// skip any homes that aren't in the given world
				if( !home.getWorld().equals(world) )
					continue;
				
				locations.add(new HomeNamedLocation(home));
			}
		}
		
		return locations;
	}
}
