/**
 * 
 */
package org.morganm.homespawnplus.util;

import java.util.HashSet;

import javax.inject.Singleton;

import org.morganm.homespawnplus.server.api.Block;
import org.morganm.homespawnplus.server.api.BlockFace;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility methods related to manipulating beds in the environment or
 * player.
 * 
 * @author morganm
 *
 */
@Singleton
public class BedUtils {
    private static final BlockFace[] cardinalFaces = new BlockFace[] {BlockFace.NORTH,
        BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    private static final BlockFace[] adjacentFaces = new BlockFace[] {BlockFace.NORTH,
            BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
            BlockFace.UP, BlockFace.DOWN
    };
    
    private final Logger log = LoggerFactory.getLogger(BedUtils.class);
    
    /** Find a bed starting at a given Block, up to maxDepth blocks away.
     * 
     * @param l the location to start the search
     * @param maxDepth maximum distance from original location to search
     * 
     * @return the location of the bed found or null if none found
     */
    public Location findBed(Block b, int maxDepth) {
        return findBedRecursive(b, new HashSet<Location>(50), 0, maxDepth);
    }
    
    /** Recursive method to efficiently search for a bed within a given distance.
     * 
     * @param b
     * @param checkedLocs
     * @param currentLevel
     * @param maxDepth
     * @return
     */
    private Location findBedRecursive(Block b, HashSet<Location> checkedLocs, int currentLevel, int maxDepth) {
        log.debug("findBed: b={} currentLevel={}", b, currentLevel);
        if( b.getTypeId() == 26 ) { // it's a bed! make sure the other half is there
            log.debug("findBed: Block ",b," is bed block");
            for(BlockFace bf : cardinalFaces) {
                Block nextBlock = b.getRelative(bf);
                if( nextBlock.getTypeId() == 26 ) {
                    log.debug("findBed: Block {} is second bed block", nextBlock);
                    return b.getLocation();
                }
            }
        }
        
        // first we check for a bed in all the adjacent blocks, before recursing to move out a level
        for(BlockFace bf : adjacentFaces) {
            Block nextBlock = b.getRelative(bf);
            if( checkedLocs.contains(nextBlock.getLocation()) ) // don't check the same block twice
                continue;
            
            if( nextBlock.getTypeId() == 26 ) { // it's a bed! make sure the other half is there
                log.debug("findBed: Block {} is bed block", nextBlock);
                for(BlockFace cardinal : cardinalFaces) {
                    Block possibleBedBlock = nextBlock.getRelative(cardinal);
                    if( possibleBedBlock.getTypeId() == 26 ) {
                        log.debug("findBed: Block {} is second bed block", possibleBedBlock);
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
            if( checkedLocs.contains(nextBlock.getLocation()) ) // don't recurse to the same block twice
                continue;
            checkedLocs.add(nextBlock.getLocation());
            
            l = findBedRecursive(nextBlock, checkedLocs, currentLevel+1, maxDepth);
            if( l != null )
                break;
        }
        
        return l;
    }

    /** Although HSP records the exact location that a player is when clicking
     * on a bed (which allows the player to respawn exactly where they were),
     * the latest version of Bukkit actually check to see if the bed exists
     * at the players Bed location and print an annoying "Your home bed was
     * missing or obstructed" if there is not a block at the location.
     * 
     * So this method takes a given location and finds the exact location of
     * the nearest bed and sets the Bukkit location there.
     * 
     * @param player
     * @param l
     */
    public void setBukkitBedHome(final Player player, final Location l) {
        if( l == null )
            return;
        
        // First check the existing bed location. If it exists and is within
        // 10 blocks already, do nothing.
        Location oldBedLoc = player.getBedSpawnLocation();
        if( oldBedLoc != null ) {
            double distance = oldBedLoc.distance(l);
            if( distance < 10 )
                return;
        }
        
        // look up to 10 blocks away for the bed
        Location bedLoc = findBed(l.getBlock(), 10);

        if( bedLoc != null )
            player.setBedSpawnLocation(bedLoc);
    }
}
