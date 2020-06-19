package com.crionuke.bolts;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BoltTest extends Assert {

    private final int CONSUMERS = 16;
    private final int FIRE = 1024;
    private final int COUNT = FIRE * CONSUMERS;
    private final int TIMEOUT = 500;

    private ExecutorService executorService;
    private Dispatcher dispatcher;
    private BlockingQueue<PayloadEvent> queue;
    private List<Consumer> consumers;

    @Before
    public void before() {
        executorService = Executors.newFixedThreadPool(CONSUMERS);
        dispatcher = new Dispatcher();
        queue = new SynchronousQueue<>();
        consumers = new ArrayList<>();
        for (int consumerIndex = 1; consumerIndex <= CONSUMERS; consumerIndex++) {
            Consumer consumer = new Consumer(consumerIndex, queue);
            consumers.add(consumer);
            executorService.execute(consumer);
            dispatcher.subscribe(consumer, PayloadEvent.class);
        }
    }

    @After
    public void after() {
        for (Consumer consumer : consumers) {
            consumer.finish();
        }
    }

    @Test
    public void test() throws InterruptedException {
        for (int payloadIndex = 1; payloadIndex <= FIRE; payloadIndex++) {
            dispatcher.dispatch(new PayloadEvent(payloadIndex));
        }
        int count = 0;
        while (true) {
            if (queue.poll(TIMEOUT, TimeUnit.MILLISECONDS) != null) {
                count++;
            } else {
                break;
            }
        }
        assertEquals(count, COUNT);
    }

    class Consumer extends Bolt implements PayloadHandler {

        private final BlockingQueue<PayloadEvent> queue;

        Consumer(int index, BlockingQueue<PayloadEvent> queue) {
            super("consumer-" + index, FIRE);
            this.queue = queue;
        }

        @Override
        public void handlePayload(PayloadEvent event) throws InterruptedException {
            queue.put(event);
        }
    }

    final class PayloadEvent extends Event<PayloadHandler> {

        private final int payload;

        public PayloadEvent(int payload) {
            super();
            this.payload = payload;
        }

        @Override
        public void handle(PayloadHandler handler) throws InterruptedException {
            handler.handlePayload(this);
        }
    }

    interface PayloadHandler {
        void handlePayload(PayloadEvent event) throws InterruptedException;
    }
}
