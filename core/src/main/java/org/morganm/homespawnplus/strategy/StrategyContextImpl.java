/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.morganm.homespawnplus.integration.multiverse.MultiverseModule;
import org.morganm.homespawnplus.integration.worldguard.WorldGuardModule;
import org.morganm.homespawnplus.server.api.Factory;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.TeleportOptions;
import org.morganm.homespawnplus.strategies.ModeDefault;
import org.morganm.homespawnplus.strategies.ModeDistanceLimits;
import org.morganm.homespawnplus.strategies.ModeInRegion;
import org.morganm.homespawnplus.strategies.ModeMultiverseDestinationPortal;
import org.morganm.homespawnplus.strategies.ModeMultiverseSourcePortal;
import org.morganm.homespawnplus.strategies.ModeSourceWorld;
import org.morganm.homespawnplus.strategies.ModeYBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The context given to a strategy that is being evaluated.
 * 
 * @author morganm
 *
 */
public class StrategyContextImpl implements StrategyContext {
    private static final Logger log = LoggerFactory.getLogger(StrategyContextImpl.class);
    
	private final static ModeStrategyImpl defaultMode = new ModeDefault();
	private final Factory factory;
    private final MultiverseModule multiVerse;
    private final WorldGuardModule worldGuard;
	
	private String eventType;
	private Player player;
	private Location location;
	/* If there is a "fromLocation" for this action, it will be recorded here.
	 * 
	 */
	private Location fromLocation;
	private String arg;
	private transient boolean isDistanceCheckEnabled = false;
	
	/** As a strategy chain is being evaluated, the current mode might change. This
	 * is tracked here in the context object.
	 */
	private List<ModeStrategyImpl> currentModes;
	
	@Inject
	public StrategyContextImpl(Factory factory, MultiverseModule multiVerse, WorldGuardModule worldGuard) {
	    this.factory = factory;
	    this.multiVerse = multiVerse;
	    this.worldGuard = worldGuard;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.StrategyContext#getEventType()
	 */
	@Override
	public String getEventType() {
		return eventType;
	}
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.StrategyContext#setEventType(java.lang.String)
	 */
	@Override
	public void setEventType(String eventType) {
		this.eventType = eventType.toLowerCase();
	}

	@Override
	public Player getPlayer() {
		return player;
	}
	
	/** Return the "location" of the event, which might be a manually passed in location
	 * or the player location, depending on which data we have.
	 * 
	 * @return
	 */
	@Override
	public Location getEventLocation() {
		if( location != null )
			return location;
		else
			return player.getLocation();
	}
	
	/** The location the event is happening, which may be different than
	 * the player location.
	 * 
	 * @return
	 */
	@Override
	public Location getLocation() {
		return location;
	}
	@Override
	public void setLocation(Location location) {
		this.location = location;
	}
	
	@Override
	public Location getFromLocation() {
		return fromLocation;
	}
	@Override
	public void setFromLocation(Location fromLocation) {
		this.fromLocation = fromLocation;
	}

	@Override
	public void setPlayer(Player player) {
		this.player = player;
		
		// if player isn't null and location is, then automatically update
		// location to the player's location (this can be overridden later)
		if( player != null && getLocation() == null )
			setLocation(player.getLocation());
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.StrategyContext#getCurrentModes()
	 */
	@Override
	public List<ModeStrategyImpl> getCurrentModes() {
		return currentModes;
	}
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.StrategyContext#addMode(org.morganm.homespawnplus.strategy.ModeStrategy)
	 */
	@Override
	public void addMode(ModeStrategyImpl mode) {
        // if it's not an additive mode, then clear modes to "switch" to new mode
        if( !mode.isAdditive() )
            currentModes.clear();
        
        currentModes.add(mode);
        isDistanceCheckEnabled = isModeEnabled(StrategyMode.MODE_DISTANCE_LIMITS);
	}
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.StrategyContext#resetCurrentModes()
	 */
	@Override
	public void resetCurrentModes() {
		if( currentModes == null )
			currentModes = new ArrayList<ModeStrategyImpl>(2);
		else
			currentModes.clear();

		currentModes.add(defaultMode);
		isDistanceCheckEnabled = false;
	}
	
	private boolean isDefaultMode(final StrategyMode mode) {
		if( mode == StrategyMode.MODE_HOME_NORMAL || mode == StrategyMode.MODE_DEFAULT )
			return true;
		else
			return false;
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.StrategyContext#isInHomeDefaultMode()
	 */
	@Override
	public boolean isInHomeDefaultMode() {
		if( isDefaultModeEnabled() )
			return true;
		
		if( isModeEnabled(StrategyMode.MODE_HOME_ANY) )
			return false;
		if( isModeEnabled(StrategyMode.MODE_HOME_BED_ONLY) )
			return false;
		if( isModeEnabled(StrategyMode.MODE_HOME_NO_BED) )
			return false;
		if( isModeEnabled(StrategyMode.MODE_HOME_DEFAULT_ONLY) )
			return false;
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.StrategyContext#isDefaultModeEnabled()
	 */
	@Override
	public boolean isDefaultModeEnabled() {
		if( currentModes == null || currentModes.size() == 0 )
			return true;
		if( currentModes.size() >= 1 ) {
			StrategyMode mode = currentModes.get(0).getMode();
			return isDefaultMode(mode);
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.StrategyContext#isModeEnabled(org.morganm.homespawnplus.strategy.StrategyMode)
	 */
	@Override
	public boolean isModeEnabled(final StrategyMode mode) {
		boolean ret = getMode(mode) != null;
		log.debug("isModeEnabled() mode=",mode,", ret=",ret);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.StrategyContext#getMode(org.morganm.homespawnplus.strategy.StrategyMode)
	 */
	@Override
	public ModeStrategy getMode(final StrategyMode mode) {
		ModeStrategy ret = null;
		log.debug("getMode() check for mode ",mode);
		
		if( currentModes == null || currentModes.size() == 0 ) {
			if( isDefaultMode(mode) )
				ret = defaultMode;
			
			log.debug("getMode() No modes defined, returning ",ret);
			return ret;
		}
		
		for(ModeStrategy currentMode : currentModes) {
			StrategyMode modeType = currentMode.getMode();
			if( modeType == mode ) {
				ret = currentMode;
				break;
			}
		}
		
		log.debug("getMode() returning ",ret);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.StrategyContext#isStrategyProcessingAllowed()
	 */
	@Override
	public boolean isStrategyProcessingAllowed() {
		if( multiVerse.isEnabled() ) {
			ModeStrategy modeStrategy = getMode(StrategyMode.MODE_MULTIVERSE_SOURCE_PORTAL);
			if( modeStrategy != null && modeStrategy instanceof ModeMultiverseSourcePortal ) {
				ModeMultiverseSourcePortal mode = (ModeMultiverseSourcePortal) modeStrategy;
				String strategyPortalName = mode.getPortalName();
				String sourcePortalName = multiVerse.getSourcePortalName();
				if( !strategyPortalName.equals(sourcePortalName) ) {
					log.debug("isStrategyProcessingAllowed() returning false for source portal check. ",
							"strategyPortalName=",strategyPortalName,", sourcePortalName=",sourcePortalName);
					return false;
				}
			}
			
			modeStrategy = getMode(StrategyMode.MODE_MULTIVERSE_DESTINATION_PORTAL);
			if( modeStrategy != null && modeStrategy instanceof ModeMultiverseDestinationPortal ) {
				ModeMultiverseDestinationPortal mode = (ModeMultiverseDestinationPortal) modeStrategy;
				String strategyPortalName = mode.getPortalName();
				String destinationPortalName = multiVerse.getDestinationPortalName();
				if( !strategyPortalName.equals(destinationPortalName) ) {
					log.debug("isStrategyProcessingAllowed() returning false for destination portal check. ",
							"strategyPortalName=",strategyPortalName,", destinationPortalName=",destinationPortalName);
					return false;
				}
			}
		}
		
		if( worldGuard.isEnabled() ) {
			ModeStrategy modeStrategy = getMode(StrategyMode.MODE_IN_REGION);
			if( modeStrategy != null && modeStrategy instanceof ModeInRegion ) {
				ModeInRegion mode = (ModeInRegion) modeStrategy;
				String regionName = mode.getRegionName();
				
				if( !worldGuard.getWorldGuardInterface().isLocationInRegion(getEventLocation(), regionName) ) {
					log.debug("isStrategyProcessingAllowed() returning false for worldguard region check. ",
							"region=",regionName);
					return false;
				}
			}
		}
		
		ModeStrategy modeStrategy = getMode(StrategyMode.MODE_SOURCE_WORLD);
		if( modeStrategy instanceof ModeSourceWorld ) {
			ModeSourceWorld mode = (ModeSourceWorld) modeStrategy;
			String sourceWorld = mode.getWorldName();
			
			if( getFromLocation() == null || !getFromLocation().getWorld().getName().equals(sourceWorld) ) { 
				log.debug("isStrategyProcessingAllowed() returning false for sourceWorld. ",
						"sourceWorld=",sourceWorld,", fromLocation=",fromLocation);
				return false;
			}
		}

		log.debug("isStrategyProcessingAllowed() returning true");
		return true;
	}
	
	/** Using currently set modes, return the current bounds
	 * 
	 * @return current bounds, guaranteed not to be null
	 */
	@Override
	public TeleportOptions getTeleportOptions() {
        TeleportOptions options = factory.newTeleportOptions();

		List<ModeStrategyImpl> modes = getCurrentModes();
		for(ModeStrategy mode : modes) {
		    switch(mode.getMode()) {
		    case MODE_YBOUNDS:
                ModeYBounds modeYBounds = (ModeYBounds) mode;
                options.setMinY(modeYBounds.getMinY());
                options.setMaxY(modeYBounds.getMaxY());
		        break;
		        
		    case MODE_NO_WATER:
                options.setNoTeleportOverWater(true);
                break;
                
		    case MODE_NO_ICE:
                options.setNoTeleportOverIce(true);
                break;
                
		    case MODE_NO_LEAVES:
                options.setNoTeleportOverLeaves(true);
                break;
                
		    case MODE_NO_LILY_PAD:
                options.setNoTeleportOverLilyPad(true);
                break;
                
            default:
                // do nothing for any other modes
		    }
		}
		
		return options;
	}
	
    
    /** Validate the locations meet any distance limit criteria specified in the current
     * context. The context "getEventLocation()" is the anchor location (usually the
     * player location).
     * 
     * @param newLoation the location being compared
     * @return true if the location is within the distance bounds, false if not
     */
    @Override
	public boolean checkDistance(final Location newLocation) {
        // no ModeDistanceLimits specified, so check is true regardless of locations
        if( !isDistanceCheckEnabled )
            return true;
        
        final ModeStrategy mode = getMode(StrategyMode.MODE_DISTANCE_LIMITS);
        if( mode != null ) {
            final Location anchor = getEventLocation();
            // if either location is null, distance is infinite. Fail check.
            if( anchor == null || newLocation == null )
                return false;
            
            // different worlds? doesn't meet bounds limits, must be same-world. Also prevents
            // exception from Location.distance()
            if( anchor.getWorld() != newLocation.getWorld() )
                return false;
            
            ModeDistanceLimits limits = (ModeDistanceLimits) mode;
            double distance = anchor.distance(newLocation);
            if( distance >= limits.getMinDistance() && distance <= limits.getMaxDistance() )
                return true;
            else
                return false;
        }
        // shouldn't ever get here since we do isDistanceCheckEnabled above, but in case we
        // do, this is same effect; return true since distance check is not enabled.
        else {
            return true;
        }
    }

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.StrategyContext#getArg()
	 */
	@Override
	public String getArg() {
		return arg;
	}
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.StrategyContext#setArg(java.lang.String)
	 */
	@Override
	public void setArg(String arg) {
		this.arg = arg;
	}
	
	public String toString() {
		return "{eventType="+eventType
				+",player="+player
				+",player.location="+(player != null && player.getLocation() != null ? player.getLocation().shortLocationString() : null)
				+",location="+(location != null ? location.shortLocationString() : null)
				+",arg="+arg
				+"}";
	}
}
