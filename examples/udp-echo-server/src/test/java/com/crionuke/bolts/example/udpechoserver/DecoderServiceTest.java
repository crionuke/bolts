package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.Bolt;
import com.crionuke.bolts.Dispatcher;
import com.crionuke.bolts.example.udpechoserver.events.IncomingDatagramEvent;
import com.crionuke.bolts.example.udpechoserver.events.IncomingPayloadEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DecoderServiceTest extends Assert {
    static private final Logger logger = LoggerFactory.getLogger(DecoderServiceTest.class);

    private ThreadPool threadPool;
    private Dispatcher dispatcher;
    private DecoderService decoderService;
    private StubService stubService;

    private BlockingQueue<IncomingPayloadEvent> incomingPayloadEvents;

    @Before
    public void before() throws IOException {
        threadPool = new ThreadPool();
        dispatcher = new Dispatcher();
        decoderService = new DecoderService(threadPool, dispatcher);
        decoderService.postConstruct();
        stubService = new StubService();
        stubService.postConstruct();
        incomingPayloadEvents = new LinkedBlockingQueue<>(32);
    }

    @After
    public void after() {
        decoderService.finish();
        stubService.finish();
    }

    @Test
    public void testDecoder() throws InterruptedException {
        // Create datagram
        String testPayload = "helloworld";
        SocketAddress testSourceAddress = NetworkUtils.generateSocketAddress();
        ByteBuffer testByteBuffer = NetworkUtils.createByteBuffer(testPayload);
        logger.info("Test with payload='{}' and sourceAddress={}", testPayload, testSourceAddress);
        dispatcher.dispatch(new IncomingDatagramEvent(testByteBuffer, testSourceAddress));
        // Waiting event
        IncomingPayloadEvent incomingPayloadEvent = incomingPayloadEvents.poll(1000, TimeUnit.MILLISECONDS);
        String payload = incomingPayloadEvent.getPayload();
        SocketAddress sourceAddress = incomingPayloadEvent.getSourceAddress();
        logger.info("Decoded payload '{}' from address {}", payload, sourceAddress);
        assertEquals(testPayload, payload);
        assertEquals(testSourceAddress, sourceAddress);
    }

    private class StubService extends Bolt implements IncomingPayloadEvent.Handler {
        private final Logger logger = LoggerFactory.getLogger(StubService.class);

        public StubService() {
            super("stub", 32);
        }

        @Override
        public void handleIncomingPayload(IncomingPayloadEvent event) throws InterruptedException {
            if (logger.isTraceEnabled()) {
                logger.trace("Handle {}", event);
            }
            incomingPayloadEvents.put(event);
        }

        public void postConstruct() {
            threadPool.execute(this);
            dispatcher.subscribe(this, IncomingPayloadEvent.class);
        }
    }
}
