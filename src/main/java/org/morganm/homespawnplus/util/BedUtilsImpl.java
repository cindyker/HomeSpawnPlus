/**
 * 
 */
package org.morganm.homespawnplus.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.Permissions;
import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.server.api.Block;
import org.morganm.homespawnplus.server.api.BlockFace;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility methods related to manipulating beds in the environment or
 * player.
 * 
 * @author morganm
 *
 */
@Singleton
public class BedUtilsImpl implements BedUtils {
    private static final BlockFace[] cardinalFaces = new BlockFace[] {BlockFace.NORTH,
        BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    private static final BlockFace[] adjacentFaces = new BlockFace[] {BlockFace.NORTH,
            BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
            BlockFace.UP, BlockFace.DOWN
    };
    
    private final Logger log = LoggerFactory.getLogger(BedUtilsImpl.class);
    private final Permissions permissions;
    private final ConfigCore configCore;
    private final Server server;
    private final HomeUtil homeUtil;
    
    // map sorted by PlayerName->Location->Time of event
    private final HashMap<String, ClickedEvent> bedClicks = new HashMap<String, ClickedEvent>();
    
    @Inject
    public BedUtilsImpl(Permissions permissions, ConfigCore configCore, Server server, HomeUtil homeUtil) {
        this.permissions = permissions;
        this.configCore = configCore;
        this.server = server;
        this.homeUtil = homeUtil;
    }
    
    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.util.BedUtils#findBed(org.morganm.homespawnplus.server.api.Block, int)
     */
    @Override
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

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.util.BedUtils#setBukkitBedHome(org.morganm.homespawnplus.server.api.Player, org.morganm.homespawnplus.server.api.Location)
     */
    @Override
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
        
        Location bedLoc = findBed(l.getBlock(), 5);
        return bedLoc != null;
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.util.BedUtils#doBedClick(org.morganm.homespawnplus.server.api.Player, org.morganm.homespawnplus.server.api.Block)
     */
    @Override
    public boolean doBedClick(final Player player, final Block bedBlock) {
        // someone clicked on a bed, good time to keep the 2-click hash clean
        cleanupBedClicks();

        // make sure player has permission
        if( !permissions.hasBedSetHome(player) ) {
            log.debug("onPlayerInteract(): player {} has no permission", player);
            return false;
        }

        final boolean require2Clicks = configCore.isBedHome2Clicks();

        ClickedEvent ce = bedClicks.get(player.getName());

        // if there is an event in the cache, then this is their second click - save their home
        if( ce != null || !require2Clicks ) {
            if( ce == null || bedBlock.getLocation().equals(ce.location) ) {
                boolean setDefaultHome = false;

                // we set the bed to be the default home only if there isn't another non-bed
                // default home that exists
                Home existingDefaultHome = homeUtil.getDefaultHome(player.getName(), player.getWorld().getName());
                if( existingDefaultHome == null || existingDefaultHome.isBedHome() )
                    setDefaultHome = true;

                // we update the Bukkit bed first as this avoids setHome() having to
                // guess which bed we clicked on. However, it's possible setHome() will
                // refuse to set the home for some reason, so we first record the
                // old location so we can restore it if the setHome() call fails.
                Location oldBedLoc = player.getBedSpawnLocation();
                player.setBedSpawnLocation(bedBlock.getLocation()); // update Bukkit bed
                
                String errorMsg = homeUtil.setHome(player.getName(), player.getLocation(), player.getName(), setDefaultHome, true); 
                if( errorMsg == null ) {        // success!
                    server.sendLocalizedMessage(player, HSPMessages.HOME_BED_SET);
                }
                else {
                    player.sendMessage(errorMsg);
                    player.setBedSpawnLocation(oldBedLoc);  // restore old bed if setHome() failed
                }

                bedClicks.remove(player.getName());
            }
        }
        // otherwise this is first click, tell them to click again to save their home
        else {
            bedClicks.put(player.getName(), new ClickedEvent(bedBlock.getLocation(), System.currentTimeMillis()));
            server.sendLocalizedMessage(player, HSPMessages.HOME_BED_ONE_MORE_CLICK);
            
            // cancel the first-click event if 2 clicks is required
            return require2Clicks;
        }
        
        return false;
    }
    
    private long lastCleanup = 0L;
    private void cleanupBedClicks() {
        // skip cleanup if nothing to do
        if( bedClicks.size() == 0 )
            return;
        
        // don't run a cleanup if we just ran one in the last 5 seconds
        if( System.currentTimeMillis() < lastCleanup+5000 )
            return;
        
        lastCleanup = System.currentTimeMillis();
        
        long currentTime = System.currentTimeMillis();
        
        Set<Entry<String, ClickedEvent>> set = bedClicks.entrySet();
        for(Iterator<Entry<String, ClickedEvent>> i = set.iterator(); i.hasNext();) {
            Entry<String, ClickedEvent> e = i.next();
            // if the click is older than 5 seconds, remove it
            if( currentTime > e.getValue().timestamp+5000 ) {
                i.remove();
            }
        }
    }

    private class ClickedEvent {
        public Location location;
        public long timestamp;
        
        public ClickedEvent(Location location, long timestamp) {
            this.location = location;
            this.timestamp = timestamp;
        }
    }
}
