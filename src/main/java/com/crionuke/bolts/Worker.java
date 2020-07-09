package com.crionuke.bolts;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Base class for workers
 *
 * @author Kirill Byvshev (k@byv.sh)
 * @since 1.0.0
 */
public abstract class Worker implements Runnable {
    static private final AtomicLong uidCounter = new AtomicLong();

    protected final long uid;
    protected volatile boolean looping;

    public Worker() {
        uid = uidCounter.incrementAndGet();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "=(uid=" + uid + ")";
    }

    public void finish() {
        looping = false;
    }

    public long getUid() {
        return uid;
    }
}
