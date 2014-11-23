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
package com.andune.minecraft.hsp.storage.cache;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author andune
 */
public class AsyncWriter implements Runnable {
    private final Logger log = LoggerFactory.getLogger(AsyncWriter.class);
    private final ConcurrentLinkedQueue<EntityCommitter> queue;
    private final WatchDog watchDog;
    private long sleepTime = 5000;  // default 5 seconds
    private boolean running = false;

    public AsyncWriter(WatchDog watchDog) {
        this.watchDog = watchDog;
        queue = new ConcurrentLinkedQueue<EntityCommitter>();
    }

    public void push(EntityCommitter ec) {
        queue.add(ec);
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void stop() {
        running = false;
    }

    /**
     * Flush out the queue by committing all pending transactions.
     */
    public synchronized void flush() {
        int flushedCommits = 0;

        EntityCommitter ec = null;
        while ((ec = queue.poll()) != null) {
            try {
                ec.commit();
                flushedCommits++;
            } catch (Exception e) {
                log.error("Caught exception when committing data to backing store", e);
            }
            watchDog.tickle();
        }
        watchDog.tickle();

        log.debug("AsyncWriter debug: flushed {} commits", flushedCommits);
    }

    public void run() {
        running = true;

        while (running) {
            flush();

            if (running) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
