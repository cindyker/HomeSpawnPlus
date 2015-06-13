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

import com.andune.minecraft.commonlib.FeatureNotImplemented;
import com.andune.minecraft.commonlib.server.api.Block;
import com.andune.minecraft.commonlib.server.api.BlockFace;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.World;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.world.extent.Extent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author andune
 *
 */
public class SpongeBlock implements Block {
    @Inject
    private Game game;
    @Inject
    private SpongeServer server;

    private final org.spongepowered.api.world.Location spongeLocation;

    public SpongeBlock(org.spongepowered.api.world.Location spongeLocation) {
        checkNotNull(spongeLocation);
        this.spongeLocation = spongeLocation;
    }

    public SpongeBlock(Location l) {
        checkNotNull(l);
        Optional<org.spongepowered.api.world.World> optional = game.getServer().getWorld(l.getWorld().getName());
        org.spongepowered.api.world.World w = optional.get();
        spongeLocation = new org.spongepowered.api.world.Location(w, l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }
    public SpongeBlock(Block b) {
        this(b.getLocation());
    }
    
    public org.spongepowered.api.world.Location getSpongeLocation() {
        return spongeLocation;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Block#getLocation()
     */
    @Override
    public Location getLocation() {
        return new SpongeLocation(game, spongeLocation);
    }

    /**
     * This is no longer appropriate and should be replaced in all HSP
     * implementations with BlockTypes instead.
     */
    @Override
    public com.andune.minecraft.commonlib.server.api.BlockType getType() {
        return SpongeBlockType.getBlockType(spongeLocation.getType());
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Block#getRelative(com.andune.minecraft.hsp.server.api.BlockFace)
     * TODO: implement me
     */
    @Override
    public Block getRelative(BlockFace face) {
        throw new FeatureNotImplemented();

        /*
        org.bukkit.block.BlockFace blockFace = BukkitBlockFace.getBukkitBlockFace(face);
        org.bukkit.block.Block relativeBlock = spongeLocation.getRelative(blockFace);
        
        return new BukkitBlock(relativeBlock);
        */
    }

    @Override
    public World getWorld() {
        checkNotNull(spongeLocation);

        final Extent extent = spongeLocation.getExtent();
        final org.spongepowered.api.world.World world = (org.spongepowered.api.world.World) extent;

        return server.getWorld(world.getName());
    }
}
