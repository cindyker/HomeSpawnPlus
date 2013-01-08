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
package com.andune.minecraft.hsp.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Specifing to JUL at the moment even though HSP leverages SLF4J and
 * can therefore use other loggers. At some point this could be modified
 * to detect the logging platform and do whatever is appropriate for that
 * given platform.
 * 
 * @author andune
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
            previousLevel = Logger.getLogger("com.andune.minecraft.hsp").getLevel();
            Logger.getLogger("com.andune.minecraft.hsp").setLevel(Level.ALL);
        }
    }
    
    public static void disableDebug() {
        if( debugEnabled ) {
            debugEnabled = false;
            Logger.getLogger("com.andune.minecraft.hsp").setLevel(previousLevel);
            previousLevel = null;
        }
    }
}
