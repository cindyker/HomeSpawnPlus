/**
 * 
 */
package com.andune.minecraft.hsp.storage.cache;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;

/**
 * @author andune
 *
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
        while((ec = queue.poll()) != null) {
            try {
                ec.commit();
                flushedCommits++;
            }
            catch(Exception e) {
                log.error("Caught exception when committing data to backing store", e);
            }
            watchDog.tickle();
        }
        watchDog.tickle();
        
        log.debug("AsyncWriter debug: flushed {} commits", flushedCommits);
    }
    
    public void run() {
        running = true;

        while(running) {
            flush();
            
            if( running ) {
                try {
                    Thread.sleep(sleepTime);
                }
                catch(InterruptedException e) {}
            }
        }
    }
}
