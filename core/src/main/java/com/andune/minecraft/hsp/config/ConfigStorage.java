/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
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
package com.andune.minecraft.hsp.config;


/**
 * @author andune
 */
public interface ConfigStorage {
    public enum Type {
        EBEANS,
        CACHED_EBEANS,      // NOT USED
        YAML,
        YAML_SINGLE_FILE,
        PERSISTANCE_REIMPLEMENTED_EBEANS,
        UNKNOWN;

        /**
         * Ordinarily this is BAD to expose enum ordinal values. Sadly, these
         * values started life as static ints and were exposed in the config
         * directly that way, so many existing configs have the int values in
         * them and so backwards compatibility requires we allow the int values
         * to still work.
         */
        static public Type getType(int intType) {
            Type[] types = Type.values();
            for (int i = 0; i < types.length; i++) {
                if (types[i].ordinal() == intType)
                    return types[i];
            }

            return Type.UNKNOWN;
        }

        static public Type getType(String stringType) {
            Type[] types = Type.values();
            for (int i = 0; i < types.length; i++) {
                if (types[i].toString().equalsIgnoreCase(stringType))
                    return types[i];
            }

            return Type.UNKNOWN;
        }
    }

    public Type getStorageType();

    public boolean useInMemoryCache();
}
