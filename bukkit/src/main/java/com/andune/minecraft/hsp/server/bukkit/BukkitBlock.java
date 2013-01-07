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
package com.andune.minecraft.hsp.server.bukkit;

import javax.inject.Inject;

import org.bukkit.Bukkit;

import com.andune.minecraft.hsp.server.api.Block;
import com.andune.minecraft.hsp.server.api.BlockFace;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.server.api.World;

/**
 * @author morganm
 *
 */
public class BukkitBlock implements Block {
    @Inject private BukkitServer server;
    private final org.bukkit.block.Block bukkitBlock;

    public BukkitBlock(org.bukkit.block.Block bukkitBlock) {
        this.bukkitBlock = bukkitBlock;
    }
    
    public BukkitBlock(Location l) {
        org.bukkit.World w = Bukkit.getWorld(l.getWorld().getName());
        bukkitBlock = w.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }
    public BukkitBlock(Block b) {
        this(b.getLocation());
    }
    
    public org.bukkit.block.Block getBukkitBlock() {
        return bukkitBlock;
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Block#getLocation()
     */
    @Override
    public Location getLocation() {
        return new BukkitLocation(bukkitBlock.getLocation());
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Block#getTypeId()
     */
    @Override
    public int getTypeId() {
        return bukkitBlock.getTypeId();
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Block#getRelative(org.morganm.homespawnplus.server.api.BlockFace)
     */
    @Override
    public Block getRelative(BlockFace face) {
        org.bukkit.block.BlockFace blockFace = BukkitBlockFace.getBukkitBlockFace(face);
        org.bukkit.block.Block relativeBlock = bukkitBlock.getRelative(blockFace);
        
        return new BukkitBlock(relativeBlock);
    }

    @Override
    public World getWorld() {
        return server.getWorld(bukkitBlock.getWorld().getName());
    }

}
