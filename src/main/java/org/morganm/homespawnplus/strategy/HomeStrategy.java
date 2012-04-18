/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.morganm.homespawnplus.entity.Home;

/** Methods that are useful for Home-related strategies.
 * 
 * @author morganm
 *
 */
public abstract class HomeStrategy extends BaseStrategy {
	/** Taking mode into account, find the default home on a given world. This may
	 * return just a bed home, or not a bed at all or even any home, all depending on the
	 * home mode that is set.
	 * 
	 * @return the home matching the current mode on the given world, or null
	 */
	protected Home getModeHome(final StrategyContext context, String worldName) {
		final List<HomeMode> modes = context.getCurrentModes();
		final String playerName = context.getPlayer().getName();

		// if worldName is null, attempt to set it from player context, if available
		if( worldName == null )
			if( context.getPlayer().getWorld() != null )
				worldName = context.getPlayer().getWorld().getName();
		
		Home home = null;
		
		// If the mode is NORMAL, DEFAULT_ONLY or NO_BED, then we start by grabbing the
		// default home and then apply the modes based on the status of that home
		if( isModeEnabled(modes, HomeMode.MODE_HOME_NORMAL)
				|| isModeEnabled(modes, HomeMode.MODE_HOME_DEFAULT_ONLY)
				|| isModeEnabled(modes, HomeMode.MODE_HOME_NO_BED) )
		{
			home = plugin.getUtil().getDefaultHome(playerName, worldName);
			
			// if mode is MODE_HOME_NO_BED and the default home is a bed, don't use it
			if( home != null && home.isBedHome() && isModeEnabled(modes, HomeMode.MODE_HOME_NO_BED) ) {
				logVerbose("Home ",home," skipped because MODE_HOME_NO_BED is true and home was set by bed");
				home = null;
			}
			
			if( isModeEnabled(modes, HomeMode.MODE_HOME_REQUIRES_BED) && !isBedNearby(home) ) {
				logVerbose("Home ",home," skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
				home = null;
			}
		}
		
		// If we haven't found a home by this point, check to see if we are in a NORMAL
		// or BED_ONLY mode and if so, look for a bedHome to satisfy this condition
		if( home == null && (isModeEnabled(modes, HomeMode.MODE_HOME_NORMAL) ||
				isModeEnabled(modes, HomeMode.MODE_HOME_BED_ONLY)) &&
				!isModeEnabled(modes, HomeMode.MODE_HOME_NO_BED) )
		{
			home = getBedHome(playerName, worldName);
			
			if( isModeEnabled(modes, HomeMode.MODE_HOME_REQUIRES_BED) && !isBedNearby(home) ) {
				logVerbose("Home ",home," skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
				home = null;
			}
		}
		
		// If we haven't found a home by this point and we are in MODE_ANY, then just
		// try to grab any home we can find.
		// TODO: 4/17/12: review of code appears that MODE_HOME_ANY implementation is not
		// functioning here as intended. Should be fixed and validated to be working.
		if( home == null && isModeEnabled(modes, HomeMode.MODE_HOME_ANY) ) {
			Set<Home> homes = plugin.getStorage().getHomeDAO().findHomesByWorldAndPlayer(worldName, playerName);
			// just grab the first one we find
			if( homes != null && homes.size() != 0 ) {
				home = homes.iterator().next();
				
				if( isModeEnabled(modes, HomeMode.MODE_HOME_REQUIRES_BED) && !isBedNearby(home) ) {
					logVerbose("Home ",home," skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
					home = null;
				}
			}
		}
		
		return home;
	}

	/** Loop through all existing modes that have been set to see if a given mode
	 * has been enabled.
	 * 
	 * @param modes
	 * @param mode
	 * @return
	 */
	protected boolean isModeEnabled(final List<HomeMode> modes, final HomeMode mode) {
		if( modes == null || modes.size() == 0 ) {
			if( mode == HomeMode.MODE_HOME_NORMAL )		// default mode is assumed true
				return true;
			else
				return false;
		}
		
		for(HomeMode currentMode : modes) {
			if( currentMode == mode )
				return true;
		}
		
		return false;
	}
	
	/** Given a player and world, return the home that is defined as the
	 * bedHome (if any).
	 * 
	 * @param playerName
	 * @param worldName
	 * @return
	 */
    private Home getBedHome(String playerName, String worldName) {
    	Home bedHome = null;
    	
		Set<Home> homes = plugin.getStorage().getHomeDAO().findHomesByWorldAndPlayer(worldName, playerName);
    	if( homes != null && homes.size() != 0 ) {
	    	for(Home home : homes) {
	    		if( home.isBedHome() ) {
	    			bedHome = home;
	    			break;
	    		}
	    	}
    	}
    	
    	return bedHome;
    }
    
	/** Look for a nearby bed to the given home.
	 * 
	 * @param home
	 * @return true if a bed is nearby, false if not
	 */
	public boolean isBedNearby(final Home home) {
		if( home == null )
			return false;
		
		Location l = home.getLocation();
		if( l == null )
			return false;
		
		HashSet<Location> checkedLocs = new HashSet<Location>(50);
		Location bedLoc = findBed(l.getBlock(), checkedLocs, 0, 5);
		
		return bedLoc != null;
	}
	
	private static final BlockFace[] cardinalFaces = new BlockFace[] {BlockFace.NORTH,
		BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
	private static final BlockFace[] adjacentFaces = new BlockFace[] {BlockFace.NORTH,
			BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
			BlockFace.UP, BlockFace.DOWN
	};
	
	/** Find a bed starting at a given Block, up to maxDepth blocks away.
	 * 
	 * @param l
	 * @param currentLevel
	 * @param maxDepth
	 * @return
	 */
	private Location findBed(Block b, HashSet<Location> checkedLocs, int currentLevel, int maxDepth) {
		debug.devDebug("findBed: b=",b," currentLevel=",currentLevel);
		if( b.getTypeId() == 26 ) {	// it's a bed! make sure the other half is there
			debug.devDebug("findBed: Block ",b," is bed block");
			for(BlockFace bf : cardinalFaces) {
				Block nextBlock = b.getRelative(bf);
				if( nextBlock.getTypeId() == 26 ) {
					debug.devDebug("findBed: Block ",nextBlock," is second bed block");
					return b.getLocation();
				}
			}
		}
		
		// first we check for a bed in all the adjacent blocks, before recursing to move out a level
		for(BlockFace bf : adjacentFaces) {
			Block nextBlock = b.getRelative(bf);
			if( checkedLocs.contains(nextBlock.getLocation()) )	// don't check the same block twice
				continue;
			
			if( nextBlock.getTypeId() == 26 ) {	// it's a bed! make sure the other half is there
				debug.devDebug("findBed: Block ",nextBlock," is bed block");
				for(BlockFace cardinal : cardinalFaces) {
					Block possibleBedBlock = nextBlock.getRelative(cardinal);
					if( possibleBedBlock.getTypeId() == 26 ) {
						debug.devDebug("findBed: Block ",possibleBedBlock," is second bed block");
						return nextBlock.getLocation();
					}
				}
			}
		}
		
		// don't recurse beyond the maxDepth
		if( currentLevel+1 > maxDepth )
			return null;
		
		// if we get here, there were no beds in the adjacent blocks, so now we recurse out one
		// level of blocks to check at the next depth.
		Location l = null;
		for(BlockFace bf : adjacentFaces) {
			Block nextBlock = b.getRelative(bf);
			if( checkedLocs.contains(nextBlock.getLocation()) )	// don't recurse to the same block twice
				continue;
			checkedLocs.add(nextBlock.getLocation());
			
			l = findBed(nextBlock, checkedLocs, currentLevel+1, maxDepth);
			if( l != null )
				break;
		}
		
		return l;
	}
}
