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
