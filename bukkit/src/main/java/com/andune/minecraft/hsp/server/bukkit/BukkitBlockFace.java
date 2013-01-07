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

import com.andune.minecraft.hsp.server.api.BlockFace;

/** Mapping between abstract API BlockFace and Bukkit BlockFace.
 * 
 * @author morganm
 *
 */
public enum BukkitBlockFace {
    NORTH(BlockFace.NORTH, org.bukkit.block.BlockFace.NORTH),
    EAST(BlockFace.EAST, org.bukkit.block.BlockFace.EAST),
    SOUTH(BlockFace.SOUTH, org.bukkit.block.BlockFace.SOUTH),
    WEST(BlockFace.WEST, org.bukkit.block.BlockFace.WEST),
    UP(BlockFace.UP, org.bukkit.block.BlockFace.UP),
    DOWN(BlockFace.DOWN, org.bukkit.block.BlockFace.DOWN);
    
    private BlockFace blockFace;
    private org.bukkit.block.BlockFace bukkitBlockFace;
    
    private BukkitBlockFace(BlockFace bf, org.bukkit.block.BlockFace bbf) {
        this.blockFace = bf;
        this.bukkitBlockFace = bbf;
    }
    
    public static org.bukkit.block.BlockFace getBukkitBlockFace(BlockFace bf) {
        switch(bf) {
        case NORTH:
            return NORTH.bukkitBlockFace;
        case SOUTH:
            return SOUTH.bukkitBlockFace;
        case EAST:
            return EAST.bukkitBlockFace;
        case WEST:
            return WEST.bukkitBlockFace;
        case UP:
            return UP.bukkitBlockFace;
        case DOWN:
            return DOWN.bukkitBlockFace;
        default:
                return null;
        }
    }

    public BlockFace getBlockFace() { return blockFace; }
    public org.bukkit.block.BlockFace getBukkitBlockFace() { return bukkitBlockFace; }
}
