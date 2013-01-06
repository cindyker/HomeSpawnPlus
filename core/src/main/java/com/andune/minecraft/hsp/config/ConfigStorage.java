/**
 * 
 */
package com.andune.minecraft.hsp.config;


/**
 * @author morganm
 *
 */
public interface ConfigStorage {
    public enum Type {
        EBEANS,
        CACHED_EBEANS,      // NOT USED
        YAML,
        YAML_SINGLE_FILE,
        PERSISTANCE_REIMPLEMENTED_EBEANS,
        UNKNOWN;
    };
    
    public Type getStorageType();
}
