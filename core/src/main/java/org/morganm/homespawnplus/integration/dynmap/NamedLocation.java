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
package org.morganm.homespawnplus.integration.dynmap;

import org.morganm.homespawnplus.server.api.ConfigurationSection;

/** Interface for HSP locations.
 * 
 * @author morganm
 *
 */
public interface NamedLocation {
	public Location getLocation();
	public String getName();

	/** If the object is owned by a player, this method should return
	 * the player name of the owner.
	 * 
	 * @return
	 */
	public String getPlayerName();
	
	/** Determine whether this NamedLocation is enabled and should be
	 * shown. The ConfigurationSection is expected to be relevant
	 * to the object type, so that it can look up any configuration
	 * options to make decisions about whether it is enabled or not.
	 * 
	 * @return
	 */
	public boolean isEnabled(ConfigurationSection section);
}
