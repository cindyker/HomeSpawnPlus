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

