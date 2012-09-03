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
package org.morganm.homespawnplus.strategy;

/** The HomeSpawnPlus event types. Strategy chains are defined and
 * based on these event types.
 * 
 * @author morganm
 *
 */
public enum EventType {
	ON_JOIN("onJoin"),
	ON_DEATH("onDeath"),
	HOME_COMMAND("onHomeCommand"),
	NAMED_HOME_COMMAND("onNamedHomeCommand"),
	NAMED_SPAWN_COMMAND("onNamedSpawnCommand"),
	SPAWN_COMMAND("onSpawnCommand"),
	GROUPSPAWN_COMMAND("onGroupSpawnCommand"),
	CROSS_WORLD_TELEPORT("crossWorldTeleport"),
	MULTIVERSE_TELEPORT_CROSSWORLD("multiverseCrossWorldTeleport"),
	MULTIVERSE_TELEPORT("multiverseTeleport"),
	ENTER_REGION("onregionenter"),
	EXIT_REGION("onregionexit"),
	NEW_PLAYER("onNewPlayer");
	
	private String configOption;
	EventType(String configOption) {
		this.configOption = configOption;
	}
	public String getConfigOption() { return configOption; }
	public String toString() { return getConfigOption(); }
}
