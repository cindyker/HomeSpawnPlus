/**
 * 
 */
package com.andune.minecraft.hsp.util;

import java.io.File;

/**
 * @author andune
 *
 */
public interface BackupUtil {
    /**
     * Backup our datastore.
     * 
     * @return null on success. On error, a string describing the error. 
     */
    public String backup();

    /**
     * Restore backup into active datastore.
     * 
     * @return null on success. On error, a string describing the error. 
     */
    public String restore();
    
    /**
     * The implementation decides where the backup file is sent. This method
     * will return that file.
     * 
     * @return
     */
    public File getBackupFile();
}
