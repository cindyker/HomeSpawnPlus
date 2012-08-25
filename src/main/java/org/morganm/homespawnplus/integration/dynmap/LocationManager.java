/**
 * 
 */
package org.morganm.homespawnplus.integration.dynmap;

import java.util.List;

import org.bukkit.World;

/** In the original Dynmap-CommandBook plugin, the code uses CommandBook's location
 * manager concept, which is a nice generic interface for querying locations. Since
 * HSP doesn't have a similar abstract location paradigm, we create one here to
 * minimize code change on the original Dynmap-CommandBook algorithms.
 * 
 * @author morganm
 *
 */
public interface LocationManager {
	List<NamedLocation> getLocations(World world);
}
