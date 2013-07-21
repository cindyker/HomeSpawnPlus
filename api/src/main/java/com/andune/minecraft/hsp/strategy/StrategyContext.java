/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
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
 */
package com.andune.minecraft.hsp.strategy;

import java.util.List;

import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.TeleportOptions;

interface StrategyContext {

	String getEventType();

	void setEventType(String eventType);

	Player getPlayer();

	/** Return the "location" of the event, which might be a manually passed in location
	 * or the player location, depending on which data we have.
	 * 
	 * @return
	 */
	Location getEventLocation();

	/** The location the event is happening, which may be different than
	 * the player location.
	 * 
	 * @return
	 */
	Location getLocation();

	void setLocation(Location location);

	Location getFromLocation();

	void setFromLocation(Location fromLocation);

	void setPlayer(Player player);

	List<ModeStrategy> getCurrentModes();

	void addMode(ModeStrategy mode);

	void resetCurrentModes();

	/** Home default mode is true as long as no home exclusive modes are set
	 * that would cancel it out.
	 * 
	 * @return
	 */
	boolean isInHomeDefaultMode();

	boolean isDefaultModeEnabled();

	/** Loop through all existing modes that have been set to see if a given mode
	 * has been enabled.
	 * 
	 * @param mode
	 * @return
	 */
	boolean isModeEnabled(StrategyMode mode);

	/** If a mode is enabled, return the mode object.
	 * 
	 * @param mode
	 * @return
	 */
	ModeStrategy getMode(StrategyMode mode);

    /**
     * If a mode is enabled, return all instances of that mode. This is useful
     * for modes that can possibly be set multiple times, such as ModeEffect.
     * 
     * @param mode
     * @return
     */
    List<ModeStrategy> getModeList(StrategyMode mode);
    
    /**
     * This call clears any ModeEffect modes out of the mode list, typically
     * used after those modes have just been applied.
     */
    void clearEffectModes();

	/** Method for checking boolean states of any active modes to see if those
	 * modes allow strategy processing given the current context.
	 * 
	 * @return
	 */
	boolean isStrategyProcessingAllowed();

	/** Using currently set modes, return the current bounds
	 * 
	 * @return current bounds, guaranteed not to be null
	 */
	TeleportOptions getTeleportOptions();

	/** Validate the locations meet any distance limit criteria specified in the current
	 * context. The context "getEventLocation()" is the anchor location (usually the
	 * player location).
	 * 
	 * @param newLoation the location being compared
	 * @return true if the location is within the distance bounds, false if not
	 */
	boolean checkDistance(Location newLocation);

	String getArg();

	/** Optional argument that might be used by strategies to take input
	 * from user commands, for example. 
	 * 
	 * @param arg
	 */
	void setArg(String arg);

}