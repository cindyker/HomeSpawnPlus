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
/**
 * 
 */
package com.andune.minecraft.hsp.server.sponge;

import com.andune.minecraft.commonlib.i18n.Colors;
import com.andune.minecraft.commonlib.server.api.*;

/**
 * TODO: need to map to Sponge equivalent
 *
 * @author andune
 *
 */
public class SpongePlayer extends SpongeCommandSender implements CommandSender, Player {
    private final org.spongepowered.api.entity.player.Player spongePlayer;
    private final PermissionSystem perm;
    private final Colors colors;
    private final SpongeFactory factory;
    private final Server server;

    /** Protected constructor, should only be invoked from BukkitFactory.
     *
     */
    protected SpongePlayer(PermissionSystem perm, Colors colors,
                           org.spongepowered.api.entity.player.Player spongePlayer,
                           SpongeFactory factory, com.andune.minecraft.hsp.server.api.Server server) {
        super(spongePlayer, server, colors);
        this.perm = perm;
        this.colors = colors;
        this.spongePlayer = spongePlayer;
        this.factory = factory;
        this.server = server;
    }

    /**
     *  Return the Sponge Player object represented by this object.
     *  
     * @return
     */
    public void getSpongePlayer() {
        return;
    }

    // TODO
    @Override
    public boolean isNewPlayer() {
        return false;
    }

    @Override
    public boolean hasPlayedBefore() {
        // TODO: not sure this is possible with SpongeAPI as of v2.0
        return true;
    }

    @Override
    public String getName() {
        return spongePlayer.getName();
    }

    // TODO
    @Override
    public java.util.UUID getUUID() { return java.util.UUID.randomUUID(); }

    // TODO
    @Override
    public Location getLocation() {
        return factory.newLocation(spongePlayer.getLocation());
    }

    @Override
    public boolean hasPermission(String permission) {
        return perm.has(this, permission);
    }

    @Override
    public Location getBedSpawnLocation() {
        // TODO: figure out how to do this in Sponge
        return null;
    }

    @Override
    public World getWorld() {
        return server.getWorld(spongePlayer.getWorld().getName());
    }

    @Override
    public void setBedSpawnLocation(Location location) {
        // TODO: figure out how to implement on Sponge
    }

    @Override
    public void teleport(Location location) {
        // if SpongePlayer is in use, it's because we're running on a Sponge Server so
        // we can safely assume the incoming object is a SpongeLocation
        spongePlayer.setLocation(((SpongeLocation) location).getSpongeLocation());
    }

    @Override
    public void setVelocity(Vector velocity) {
        // TODO: not sure if this API Exists on Sponge yet
    }
    
    public boolean equals(Object o) {
        if( o == null )
            return false;
        if( !(o instanceof Player) )
            return false;
        String name = ((Player) o).getName();
        return getName().equals(name);
    }

    @Override
    public boolean isSneaking() {
        // TODO: not sure this is possible with SpongeAPI as of v2.0
        return false;
    }

    @Override
    public boolean isOnline() {
        return spongePlayer.isOnline();
    }
    
    @Override
    public long getLastPlayed() {
        return spongePlayer.getJoinData().getLastPlayed().getTime();
    }

    @Override
    public String toString() {
        return "{SpongePlayer:"+getName()+"}";
    }
}
