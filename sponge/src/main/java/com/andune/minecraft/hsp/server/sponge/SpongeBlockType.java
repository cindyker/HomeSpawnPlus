package com.andune.minecraft.hsp.server.sponge;

import com.andune.minecraft.commonlib.server.api.BlockType;
import com.andune.minecraft.commonlib.server.api.BlockTypes;

import java.util.HashMap;

/**
 * @author andune
 */
public class SpongeBlockType implements BlockType {
    private final static HashMap<BlockTypes, SpongeBlockType> forwardLookup = new HashMap<BlockTypes, SpongeBlockType>();
    private final static HashMap<org.spongepowered.api.block.BlockType, SpongeBlockType> reverseLookup = new HashMap<org.spongepowered.api.block.BlockType, SpongeBlockType>();
    static {
        for(BlockTypes type : BlockTypes.values()) {
            // TODO: figure out implementation for this in Sponge
            final org.spongepowered.api.block.BlockType material = null;
                    // org.spongepowered.api.block.BlockType.getMaterial(type.toString());
            final SpongeBlockType sbt = new SpongeBlockType(type, material);

            forwardLookup.put(type, sbt);
            reverseLookup.put(material, sbt);
        }
    }

    private final org.spongepowered.api.block.BlockType spongeType;
    private final BlockTypes blockType;

    /**
     * For internal use during initialization only.
     *
     * @param blockType
     * @param spongeType
     */
    private SpongeBlockType(BlockTypes blockType, org.spongepowered.api.block.BlockType spongeType) {
        this.blockType = blockType;
        this.spongeType = spongeType;
    }

    public boolean equals(Object o) {
        if(o == null) return false;
        if((o instanceof SpongeBlockType) == false) return false;
        final SpongeBlockType sbt = (SpongeBlockType) o;
        return sbt.blockType == blockType;
    }
    public int hashCode() {
        return blockType.hashCode();
    }

    @Override
    public BlockTypes getBlockType() {
        return blockType;
    }

    // Sponge-specific method
    public org.spongepowered.api.block.BlockType getSpongeBlockType() {
        return spongeType;
    }

    public static BlockType getBlockType(org.spongepowered.api.block.BlockType spongeBlockType) {
        return reverseLookup.get(spongeBlockType);
    }
    public static org.spongepowered.api.block.BlockType getSpongeBlockType(BlockTypes blockType) {
        return forwardLookup.get(blockType).spongeType;
    }
}
