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
 * This package contains an implementation of HSP's storage DAOs that keep
 * everything in memory for a performance gain. This means all reads are
 * done via an in-memory cache for a cache hit, or fall through to the
 * database on a cache miss. Since the intent of these classes is to
 * keep the entire data set in memory (which is a small amount of data
 * even for a large HSP site), cache misses should be rare once the cache
 * is warmed up.
 * 
 * For writes, they are also done to the in-memory cache and then flagged
 * in a queue to be asynchronously committed to the backing store at some
 * regular interval. This offloads writes from the main server thread onto
 * a spare thread at the minor risk of losing a few transactions in the
 * event of a server crash.
 */

package com.andune.minecraft.hsp.storage.cache;

