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

    public void subscribe(Bolt bolt, Object topic) {
        List<Bolt> bolts = subscriptions.get(topic);
        if (bolts == null) {
            bolts = new CopyOnWriteArrayList<>();
            subscriptions.put(topic, bolts);
        }
        bolts.add(bolt);
    }

    public boolean unsubscribe(Bolt bolt, Object topic) {
        List<Bolt> bolts = subscriptions.get(topic);
        if (bolts != null) {
            return bolts.remove(bolt);
        } else {
            return false;
        }
    }

    public void dispatch(Event event) throws InterruptedException {
        dispatch(event, event.getClass());
    }

    public void dispatch(Event event, Object topic) throws InterruptedException {
        List<Bolt> bolts = subscriptions.get(topic);
        if (bolts != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Dispatch {} to {}", event, bolts);
            }
            for (Bolt bolt : bolts) {
                bolt.fireEvent(event);
            }
        }
    }
}
