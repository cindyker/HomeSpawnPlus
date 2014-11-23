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

import java.util.Calendar;

/**
 * WatchDog class to make sure AsyncWriter never dies and if it does, it logs
 * it as an error and tries to restart it.
 *
 * @author andune
 */
public class WatchDog implements Runnable {
    private final Logger log = LoggerFactory.getLogger(WatchDog.class);
    private long lastTickle = System.currentTimeMillis();
    private boolean shuttingDown = false;
    private Thread runningThread;
    private AsyncWriter writer;

    public WatchDog() {
    }

    /**
     * The asyncWriter should be started through the watchDog. This makes sure
     * the watchDog has all the necessary information to watch the thread as
     * well as restart it if it dies. This method starts both the watchDog
     * thread and the asyncWriter thread.
     */
    public void start(AsyncWriter writer) {
        this.writer = writer;

        // first we tickle ourselves to avoid a watchdog alert on startup
        tickle();
        startRunningThread();
        new Thread(this, "HSP_WatchDog").start();
    }

    private void startRunningThread() {
        runningThread = new Thread(writer, "HSP_AsyncWriter");
        runningThread.start();
        log.info("Async writer thread started.");
    }

    /**
     * Must be invoked regularly by the watched process to flag that it is
     * still alive and working. The watchDog not being tickled regularly will
     * result it in it assuming the watched thread has died.
     */
    public void tickle() {
        lastTickle = System.currentTimeMillis();
    }

    /**
     * Invoked to shutdown the watchdog gracefully.
     */
    public void shutdown() {
        shuttingDown = true;
    }

    public void run() {
        try {
            log.info("Watchdog thread started.");
            while (!shuttingDown) {
                try {
                    final long graceTime = writer.getSleepTime() * 3;

                    // we sleep first and then check, this gives time for the
                    // system to warmup the first time we start running.
                    try {
                        Thread.sleep(graceTime);
                    } catch (InterruptedException e) {
                    }

                    if (lastTickle + graceTime < System.currentTimeMillis()) {
                        if (!runningThread.isAlive()) {
                            log.error("Watchdog noticed thread death. This is bad. Attempting to restart thread.");
                            startRunningThread();
                        } else {
                            log.error("Watchdog hasn't seen an update from AsyncWriter in too long. Thread is still alive, but possibly hung. YOU MIGHT LOSE DATA!");
                        }
                    }

                    // why? because it's debug-only and it's fun
                    if (log.isDebugEnabled()) {
                        Calendar calendar = Calendar.getInstance();
                        int hour = calendar.get(Calendar.HOUR);
                        if (hour == 0) hour = 12;
                        log.debug("Watchdog debug: {} o'clock and all's well!", hour);
                    }
                } catch (Exception e) {
                    log.error("Watchdog caught exception. Watchdog is still running but possibly broken", e);
                }
            }
        } finally {
            if (!shuttingDown) {
                log.error("Watchdog terminated unexpectedly. YOU MAY LOSE DATA!");
            }
        }
    }
}
