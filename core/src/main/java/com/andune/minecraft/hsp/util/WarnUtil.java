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
package com.andune.minecraft.hsp.util;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.google.inject.Singleton;

import java.util.HashSet;

/**
 * Utility for issuing non-repeating warnings. For example, if some check
 * might print a warning, but we don't want to spam the admin with hundreds
 * of them if it might happen often, this utility can be used to just
 * print a single warning message and suppress all others.
 *
 * @author andune
 */
@Singleton
public class WarnUtil {
    private final Logger log = LoggerFactory.getLogger(WarnUtil.class);
    private final HashSet<String> warnings = new HashSet<String>();

    public void warnOnce(String warningName, String warningMessage) {
        if (!warnings.contains(warningName)) {
            log.warn(warningMessage);
        }
        warnings.add(warningName);
    }

    public void warnOnce(String warningName, String warningMessage, Object...args) {
        if (!warnings.contains(warningName)) {
            log.warn(warningMessage, args);
        }
        warnings.add(warningName);
    }

    /**
     * Some behavior as warnOnce, matching against the same warningName keys,
     * however this will print a log INFO message instead of WARN.
     *
     * @param warningName
     * @param warningMessage
     * @param args
     */
    public void infoOnce(String warningName, String warningMessage, Object...args) {
        if (!warnings.contains(warningName)) {
            log.info(warningMessage, args);
        }
        warnings.add(warningName);
    }
}
