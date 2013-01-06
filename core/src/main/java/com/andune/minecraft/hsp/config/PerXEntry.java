/**
 * 
 */
package com.andune.minecraft.hsp.config;


/** Abstract base class for per-world and per-permission config
 * entries.
 * 
 * @author morganm
 *
 */
public abstract class PerXEntry {
    /**
     * For any value that is found that isn't a property keyword, it will be
     * passed here.
     * 
     * @param key
     * @param o
     */
    abstract void setValue(String key, Object o);
    
    /**
     * This is called after processing for the entry is complete and no further
     * changes are expected. This is so the entry can make its data immutable
     * so that if data is shared externally, there's no danger of it being
     * inadvertently altered.
     */
    void finishedProcessing() {}
}
