/**
 * 
 */
package com.andune.minecraft.hsp.guice;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * Enable or disable Guice debug output
 * on the console.
 *
 */
public class GuiceDebug {
    /**
     * Enable or disable Guice debug output
     * on the console.
     */
    private static final Handler HANDLER;
    static {
        HANDLER = new StreamHandler(System.out, new Formatter() {
            public String format(LogRecord record) {
                return String.format("[Guice %s] %s%n",
                        record.getLevel().getName(),
                        record.getMessage());
            }
        });
        HANDLER.setLevel(Level.ALL);
    }

    private GuiceDebug() {}

    public static Logger getLogger() {
        return Logger.getLogger("com.google.inject");
    }

    public static void enable() {
        Logger guiceLogger = getLogger();
        guiceLogger.addHandler(GuiceDebug.HANDLER);
        guiceLogger.setLevel(Level.ALL);
    }

    public static void disable() {
        Logger guiceLogger = getLogger();
        guiceLogger.setLevel(Level.OFF);
        guiceLogger.removeHandler(GuiceDebug.HANDLER);
    }
}
