package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.Bolt;
import com.crionuke.bolts.Dispatcher;
import com.crionuke.bolts.example.udpechoserver.events.IncomingPayloadEvent;
import com.crionuke.bolts.example.udpechoserver.events.OutgoingPayloadEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class EchoServiceTest extends Assert {
    static private final Logger logger = LoggerFactory.getLogger(DecoderServiceTest.class);

    private ThreadPool threadPool;
    private Dispatcher dispatcher;
    private EchoService echoService;
    private StubService stubService;

    private BlockingQueue<OutgoingPayloadEvent> outgoingPayloadEvents;

    @Before
    public void before() throws IOException {
        threadPool = new ThreadPool();
        dispatcher = new Dispatcher();
        echoService = new EchoService(threadPool, dispatcher);
        echoService.postConstruct();
        stubService = new StubService();
        stubService.postConstruct();
        outgoingPayloadEvents = new LinkedBlockingQueue<>(32);
    }

    @After
    public void after() {
        echoService.finish();
        stubService.finish();
    }

    @Test
    public void testEcho() throws InterruptedException {
        String testPayload = "helloworld";
        SocketAddress testSourceAddress = NetworkUtils.generateSocketAddress();
        logger.info("Test with payload='{}' and sourceAddress={}", testPayload, testSourceAddress);
        IncomingPayloadEvent incomingPayloadEvent = new IncomingPayloadEvent(testPayload, testSourceAddress);
        dispatcher.dispatch(incomingPayloadEvent);
        // Waiting event
        OutgoingPayloadEvent outgoingPayloadEvent = outgoingPayloadEvents.poll(1000, TimeUnit.MILLISECONDS);
        String payload = outgoingPayloadEvent.getPayload();
        SocketAddress targetAddress = outgoingPayloadEvent.getTargetAddress();
        logger.info("Outgoing payload '{}' to address {}", payload, targetAddress);
        assertEquals(testPayload, payload);
        assertEquals(testSourceAddress, targetAddress);
    }

    private class StubService extends Bolt implements OutgoingPayloadEvent.Handler {
        private final Logger logger = LoggerFactory.getLogger(OutgoingPayloadEvent.class);

        public StubService() {
            super("stub", 32);
        }

        @Override
        public void handleOutgoingPayload(OutgoingPayloadEvent event) throws InterruptedException {
            if (logger.isTraceEnabled()) {
                logger.trace("Handle {}", event);
            }
            outgoingPayloadEvents.put(event);
        }

        public void postConstruct() {
            threadPool.execute(this);
            dispatcher.subscribe(this, OutgoingPayloadEvent.class);
        }
    }
}

