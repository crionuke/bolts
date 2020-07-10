package com.crionuke.bolts;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.*;

public class BoltsTest extends Assert {

    private final int QUEUE_SIZE = 32;
    private final int POLL_TIMEOUT = 1000;

    private Dispatcher dispatcher;
    private TestBolt testBolt;
    private BlockingQueue<TestEvent> testEvents;
    private ExecutorService executorService;

    @Before
    public void before() {
        dispatcher = new Dispatcher();
        testBolt = new TestBolt();
        testEvents = new LinkedBlockingQueue<>(QUEUE_SIZE);
        executorService = Executors.newSingleThreadExecutor();
    }

    @After
    public void after() {
        testBolt.finish();
    }

    @Test
    public void testSubscribeWithEventClass() throws InterruptedException {
        // Init
        executorService.execute(testBolt);
        boolean subscribeResult = dispatcher.subscribe(testBolt, TestEvent.class);
        // Touch
        TestEvent testEvent = createTestEvent();
        boolean dispatchResult = dispatcher.dispatch(testEvent);
        TestEvent resultEvent = testEvents.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS);
        // Check
        assertTrue(subscribeResult);
        assertTrue(dispatchResult);
        assertNotNull(resultEvent);
        assertEquals(testEvent.getId(), resultEvent.getId());
    }

    @Test
    public void testDoubleSubscription() {
        // Touch
        boolean subscribeResult1 = dispatcher.subscribe(testBolt, TestEvent.class);
        boolean subscribeResult2 = dispatcher.subscribe(testBolt, TestEvent.class);
        // Check
        assertTrue(subscribeResult1);
        assertFalse(subscribeResult2);
    }

    @Test
    public void testNoSubscription() throws InterruptedException {
        TestEvent testEvent = createTestEvent();
        boolean result = dispatcher.dispatch(testEvent);
        assertFalse(result);
    }

    @Test
    public void testSubscribeWithTopic() throws InterruptedException {
        // Init
        executorService.execute(testBolt);
        boolean subscribeResult = dispatcher.subscribe(testBolt, "topic");
        // Touch
        TestEvent testEvent = createTestEvent();
        boolean dispatchResult = dispatcher.dispatch(testEvent, "topic");
        TestEvent resultEvent = testEvents.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS);
        // Check
        assertTrue(subscribeResult);
        assertTrue(dispatchResult);
        assertNotNull(resultEvent);
        assertEquals(testEvent.getId(), resultEvent.getId());
    }

    @Test
    public void testSubscribeWithNoTopic() throws InterruptedException {
        // Init
        executorService.execute(testBolt);
        boolean subscribeResult = dispatcher.subscribe(testBolt);
        // Touch
        TestEvent testEvent = createTestEvent();
        boolean dispatchResult = dispatcher.dispatch(testEvent, testBolt);
        TestEvent resultEvent = testEvents.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS);
        // Check
        assertTrue(subscribeResult);
        assertTrue(dispatchResult);
        assertNotNull(resultEvent);
        assertEquals(testEvent.getId(), resultEvent.getId());
    }

    private TestEvent createTestEvent() {
        int testId = (int) Math.random() * 899 + 100;
        return new TestEvent(testId);
    }

    class TestBolt extends Bolt implements TestHandler {

        public TestBolt() {
            super("test-bolt",  QUEUE_SIZE);
        }

        @Override
        public void handleTest(TestEvent event) throws InterruptedException {
            testEvents.put(event);
        }
    }

    final class TestEvent extends Event<TestHandler> {

        private final int id;

        TestEvent(int id) {
            this.id = id;
        }

        int getId() {
            return id;
        }

        @Override
        public void handle(TestHandler handler) throws InterruptedException {
            handler.handleTest(this);
        }
    }

    interface TestHandler {
        void handleTest(TestEvent event) throws InterruptedException;
    }
}
