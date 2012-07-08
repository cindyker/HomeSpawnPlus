/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyException;
import org.morganm.homespawnplus.strategy.StrategyResult;
import org.morganm.homespawnplus.util.Debug;
import org.morganm.homespawnplus.util.Teleport;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.WorldBorder;

/**
 * @author morganm
 *
 */
public class SpawnWorldRandom extends BaseStrategy {
	// most commonly will find a safe location in the first few tries, but we
	// will try up to 20 times just in case we get a random run of bad
	// locations
	private static final int MAX_TRIES = 20;
	private String world;
	
	public SpawnWorldRandom() {}
	public SpawnWorldRandom(final String arg) {
		this.world = arg;
	}
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		World w = null;
		if( world != null ) { 
			w = Bukkit.getWorld(world);
			if( w == null ) {
				log.warning(logPrefix+" found null value when looking for world: "+world);
				return null;
			}
		}
		else
			w = context.getLocation().getWorld();
		
		final Teleport.Bounds yBounds = context.getModeBounds();
		Debug.getInstance().devDebug("SpawnWorldRandom() minY=",yBounds.minY,", maxY=",yBounds.maxY);
		
		Location result = null;
		Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldBorder");
		if( p != null ) {
			WorldBorder worldBorder = (WorldBorder) p;
			BorderData border = worldBorder.GetWorldBorder(w.getName());
			
			double x = border.getX();
			double z = border.getZ();
			int radius = border.getRadius();
			
			Location min = new Location(w,x-radius, yBounds.minY, z-radius);
			Location max = new Location(w,x+radius, yBounds.maxY, z+radius);
			
			// we loop and try multiple times, because it's possible we randomly select
			// a location outside of the border. If so, we just loop and guess again.
			// This is because our random routine finds a random location within a
			// square region, while WorldBorder might have defined a circular region.
			int tries = 0;
			while( result == null && tries < MAX_TRIES ) {
				tries++;
				Debug.getInstance().devDebug("SpawnWorldRandom: try=",tries);
				result = plugin.getUtil().findRandomSafeLocation(min, max, yBounds, context.getModeSafeTeleportFlags());
				Debug.getInstance().devDebug("SpawnWorldRandom: try=",tries,", result=",result);
				if( result != null && !border.insideBorder(result) )
					result = null;
			}
			
			if( tries == MAX_TRIES )
				log.warning(logPrefix+" "+getStrategyConfigName()+" exceeded "+MAX_TRIES+" tries trying to find random location, likely indicates a problem with your configuration");
		}
		// no WorldBorder? just assume default min/max of +/- 1000
		else {
			Location min = new Location(w, -1000, yBounds.minY, -1000);
			Location max = new Location(w, 1000, yBounds.maxY, 1000);
			int tries = 0;
			while( result == null && tries < MAX_TRIES ) {
				tries++;
				Debug.getInstance().devDebug("SpawnWorldRandom: try=",tries);
				result = plugin.getUtil().findRandomSafeLocation(min, max, yBounds, context.getModeSafeTeleportFlags());
				Debug.getInstance().devDebug("SpawnWorldRandom: try=",tries,", result=",result);
			}

			if( tries == MAX_TRIES )
				log.warning(logPrefix+" "+getStrategyConfigName()+" exceeded "+MAX_TRIES+" tries trying to find random location, likely indicates a problem with your configuration");
		}
		
		if( result ==  null )
			return null;
		
		return new StrategyResult(result);
	}
	
	@Override
	public void validate() throws StrategyException {
		if( world != null ) {
			World w = Bukkit.getWorld(world);
			if( w == null )
				throw new StrategyException(getStrategyConfigName()+" tried to reference world \""+world+"\", which doesn't exist");
		}
		
		Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldBorder");
		if( p == null )
			log.info(logPrefix+" Using "+getStrategyConfigName()+" strategy but WorldBorder is not installed; assuming maximum of 1000");
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnWorldRandom";
	}

}
