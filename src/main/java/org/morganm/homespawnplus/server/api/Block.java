/**
 * 
 */
package org.morganm.homespawnplus.server.api;

/** API for manipulating blocks.
 * 
 * @author morganm
 *
 */
public interface Block {
    /**
     * Gets the Location of the block
     *
     * @return Location of block
     */
    Location getLocation();

    /**
     * Gets the type-id of this block
     *
     * @return block type-id
     */
    int getTypeId();

    /**
     * Gets the block at the given face
     * <p />
     * This method is equal to getRelative(face, 1)
     *
     * @param face Face of this block to return
     * @return Block at the given face
     * @see #getRelative(BlockFace, int)
     */
    Block getRelative(BlockFace face);
}
