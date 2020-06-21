package com.crionuke.bolts;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DispatcherTest extends Assert {

    Dispatcher dispatcher;
    TestBolt testBolt;

    @Before
    public void before() {
        dispatcher = new Dispatcher();
        testBolt = new TestBolt();
    }

    @Test
    public void testSubscription() {
        boolean subscribeResult = dispatcher.subscribe(testBolt, TestEvent.class);
        assertTrue(subscribeResult);
    }

    @Test
    public void testDoubleSubscription() {
        boolean subscribeResult1 = dispatcher.subscribe(testBolt, TestEvent.class);
        boolean subscribeResult2 = dispatcher.subscribe(testBolt, TestEvent.class);
        assertTrue(subscribeResult1);
        assertFalse(subscribeResult2);
    }

    @Test
    public void testSubscribeAndDispatch() throws InterruptedException {
        boolean subscribeResult = dispatcher.subscribe(testBolt, TestEvent.class);
        assertTrue(subscribeResult);
        boolean dispatchResult = dispatcher.dispatch(new TestEvent());
        assertTrue(dispatchResult);
    }

    @Test
    public void testNoSubscription() throws InterruptedException {
        boolean result = dispatcher.dispatch(new TestEvent());
        assertFalse(result);
    }

    class TestBolt extends Bolt implements TestHandler {

        public TestBolt() {
            super("test-bolt",  16);
        }

        @Override
        public void handleTest(TestEvent event) throws InterruptedException {

        }
    }

    final class TestEvent extends Event<TestHandler> {

        @Override
        public void handle(TestHandler handler) throws InterruptedException {
            handler.handleTest(this);
        }
    }

    interface TestHandler {
        void handleTest(TestEvent event) throws InterruptedException;
    }
}
