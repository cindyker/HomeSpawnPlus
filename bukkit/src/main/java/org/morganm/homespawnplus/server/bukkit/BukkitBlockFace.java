/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import org.morganm.homespawnplus.server.api.BlockFace;

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
