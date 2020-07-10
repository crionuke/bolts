package com.crionuke.bolts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Dispatcher collect subscriptions and used to delivery events
 *
 * @author Kirill Byvshev (k@byv.sh)
 * @since 1.0.0
 */
public class Dispatcher {
    static private final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final Map<Object, List<Bolt>> subscriptions;

    public Dispatcher() {
        subscriptions = new ConcurrentHashMap<>();
    }

    public boolean subscribe(Bolt bolt) {
        return subscribe(bolt, bolt);
    }

    public boolean subscribe(Bolt bolt, Object topic) {
        List<Bolt> bolts = subscriptions.get(topic);
        if (bolts == null) {
            bolts = new CopyOnWriteArrayList<>();
            subscriptions.put(topic, bolts);
        }
        if (!bolts.contains(bolt)) {
            return bolts.add(bolt);
        } else {
            return false;
        }
    }

    public boolean dispatch(Event event) throws InterruptedException {
        return dispatch(event, event.getClass());
    }

    public boolean dispatch(Event event, Object topic) throws InterruptedException {
        boolean changed = false;
        List<Bolt> bolts = subscriptions.get(topic);
        if (bolts != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Dispatch {} to {}", event, bolts);
            }
            for (Bolt bolt : bolts) {
                changed |= bolt.fireEvent(event);
            }
        }
        return changed;
    }
}
