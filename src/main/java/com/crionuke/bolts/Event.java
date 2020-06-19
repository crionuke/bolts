package com.crionuke.bolts;

/**
 * Base class for events
 *
 * @author Kirill Byvshev (k@byv.sh)
 * @since 1.0.0
 */
public abstract class Event<T> {

    public Event() {
        super();
    }

    abstract public void handle(T handler) throws InterruptedException;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
