package com.crionuke.bolts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Kirill Byvshev (k@byv.sh)
 * @since 1.0.0
 */
public abstract class Bolt extends Worker {
    static private final Logger logger = LoggerFactory.getLogger(Bolt.class);

    private final int POLL_TIMEOUT_MS = 1000;

    private final String name;
    private final BlockingQueue<Event> inputQueue;

    public Bolt(String name, int queueSize) {
        super();
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        inputQueue = new LinkedBlockingQueue<>(queueSize);
    }

    @Override
    public void run() {
        setThreadNamePrefix(name);
        logger.info("{} started", this);
        try {
            looping = true;
            while (looping) {
                Event event = inputQueue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (event != null) {
                    try {
                        event.handle(this);
                    } catch (ClassCastException cce) {
                        logger.warn("Unrecognized event {} for {}", event, this);
                    } catch (Exception e) {
                        logger.warn("Unhandled exception {}", e);
                    }
                }
            }
        } catch (InterruptedException ie) {
            looping = false;
            logger.debug("{} interrupted", this);
        }
        logger.info("{} finished", this);
        resetThreadName();
    }

    void fireEvent(Event event) throws InterruptedException {
        inputQueue.put(event);
    }
}
