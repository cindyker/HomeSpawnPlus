/**
 * 
 */
package com.andune.minecraft.hsp.storage.cache;

import java.util.Calendar;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;

/**
 * WatchDog class to make sure AsyncWriter never dies and if it does, it logs
 * it as an error and tries to restart it.
 * 
 * @author andune
 *
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
            while(!shuttingDown) {
                try {
                    final long graceTime = writer.getSleepTime() * 3;

                    // we sleep first and then check, this gives time for the
                    // system to warmup the first time we start running.
                    try {
                        Thread.sleep(graceTime);
                    }
                    catch(InterruptedException e) {}
                    
                    if( lastTickle + graceTime < System.currentTimeMillis() ) {
                        if( !runningThread.isAlive() ) {
                            log.error("Watchdog noticed thread death. This is bad. Attempting to restart thread.");
                            startRunningThread();
                        }
                        else {
                            log.error("Watchdog hasn't seen an update from AsyncWriter in too long. Thread is still alive, but possibly hung. YOU MIGHT LOSE DATA!");
                        }
                    }

                    // why? because it's debug-only and it's fun
                    if( log.isDebugEnabled() ) {
                        Calendar calendar = Calendar.getInstance();
                        int hour = calendar.get(Calendar.HOUR);
                        if( hour == 0 ) hour = 12;
                        log.debug("Watchdog debug: {} o'clock and all's well!", hour);
                    }
                }
                catch(Throwable t) {
                    log.error("Watchdog caught exception. Watchdog is still running but possibly broken", t);
                }
            }
        }
        finally {
            if( !shuttingDown ) {
                log.error("Watchdog terminated unexpectedly. YOU MAY LOSE DATA!");
            }
        }
    }
}
