package com.crionuke.bolts;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Kirill Byvshev (k@byv.sh)
 * @since 1.0.0
 */
public abstract class Worker implements Runnable {
    static private final AtomicLong uidCounter = new AtomicLong();

    protected final Long uid;
    protected volatile boolean looping;
    private String oldName;

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

    protected void setThreadNamePrefix(String prefix) {
        if (prefix == null) {
            throw new NullPointerException();
        }
        oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(prefix + "-" + uid);
    }

    protected void resetThreadName() {
        if (oldName != null) {
            Thread.currentThread().setName(oldName);
        }
    }
}
