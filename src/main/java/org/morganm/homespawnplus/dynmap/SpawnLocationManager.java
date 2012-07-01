/**
 * 
 */
package org.morganm.homespawnplus.dynmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.World;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.storage.dao.SpawnDAO;

/**
 * @author morganm
 *
 */
public class SpawnLocationManager implements LocationManager {
	private final HomeSpawnPlus plugin;

	public SpawnLocationManager(final HomeSpawnPlus plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<NamedLocation> getLocations(final World w) {
		List<NamedLocation> locations = new ArrayList<NamedLocation>();
		
		String world = w.getName();
		
		SpawnDAO dao = plugin.getStorage().getSpawnDAO();
		Set<Spawn> allSpawns = dao.findAllSpawns();
		if( allSpawns != null && allSpawns.size() > 0 ) {
			for(Spawn spawn : allSpawns) {
				// skip any spawns that aren't in the given world
				if( !spawn.getWorld().equals(world) )
					continue;
				
				locations.add(new SpawnNamedLocation(spawn));
			}
		}
		
		return locations;
	}

}
