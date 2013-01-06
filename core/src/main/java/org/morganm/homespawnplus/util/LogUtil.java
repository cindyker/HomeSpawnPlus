/**
 * 
 */
package org.morganm.homespawnplus.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Specifing to JUL at the moment even though HSP leverages SLF4J and
 * can therefore use other loggers. At some point this could be modified
 * to detect the logging platform and do whatever is appropriate for that
 * given platform.
 * 
 * @author morganm
 *
 */
public class LogUtil {
    private static boolean debugEnabled = false;
    private static Level previousLevel = null;

    /**
     * If debug is enabled, set it up so that it logs to the appropriate file.
     */
    public static void enableDebug() {
        if( !debugEnabled ) {
            debugEnabled = true;
            previousLevel = Logger.getLogger("org.morganm.homespawnplus").getLevel();
            Logger.getLogger("org.morganm.homespawnplus").setLevel(Level.ALL);
        }
    }
    
    public static void disableDebug() {
        if( debugEnabled ) {
            debugEnabled = false;
            Logger.getLogger("org.morganm.homespawnplus").setLevel(previousLevel);
            previousLevel = null;
        }
    }
}
