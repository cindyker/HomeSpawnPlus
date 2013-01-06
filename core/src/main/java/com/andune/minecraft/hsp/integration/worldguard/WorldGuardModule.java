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
package com.andune.minecraft.hsp.integration.worldguard;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.plugin.Plugin;

import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.server.bukkit.BukkitFactory;
import com.andune.minecraft.hsp.strategy.StrategyEngine;

/** Abstraction layer for WorldGuardInterface. This allows this class (and
 * therefore HSP) to load even if WorldGuard isn't installed on the server.
 * 
 * @author morganm
 *
 */
@Singleton
public class WorldGuardModule {
	private final Plugin plugin;
    private final BukkitFactory factory;
    private final StrategyEngine strategyEngine;
	private WorldGuardInterface worldGuardInterface;
	private WorldGuardRegion worldGuardRegion;
	private Server server;
	
	@Inject
	public WorldGuardModule(Plugin plugin, BukkitFactory factory,
	        StrategyEngine strategyEngine, Server server) {
		this.plugin = plugin;
		this.factory = factory;
		this.strategyEngine = strategyEngine;
	}
	
	public boolean isEnabled() {
		Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		if( p != null )
			return p.isEnabled();
		else
			return false;
	}
	
	public String getVersion() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if( p != null )
            return p.getDescription().getVersion();
        else
            return null;
	}
	
	public void init() {
		if( !isEnabled() )
			return;
	}

	public WorldGuardInterface getWorldGuardInterface() {
		if( !isEnabled() )
			return null;

		if( worldGuardInterface == null )
			worldGuardInterface = new WorldGuardInterface(plugin);
		return worldGuardInterface;
	}
	
	public WorldGuardRegion getWorldGuardRegion() {
		if( !isEnabled() )
			return null;

		if( worldGuardRegion == null )
			worldGuardRegion = new WorldGuardRegion(plugin, this, factory, strategyEngine, server);
		return worldGuardRegion;
	}
}
