package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.Bolt;
import com.crionuke.bolts.Dispatcher;
import com.crionuke.bolts.example.udpechoserver.events.OutgoingDatagramEvent;
import com.crionuke.bolts.example.udpechoserver.events.OutgoingPayloadEvent;
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

public class EncoderServiceTest extends Assert {
    static private final Logger logger = LoggerFactory.getLogger(DecoderServiceTest.class);

    private ThreadPool threadPool;
    private Dispatcher dispatcher;
    private EncoderService encoderService;
    private StubService stubService;

    private BlockingQueue<OutgoingDatagramEvent> outgoingDatagramEvents;

    @Before
    public void before() throws IOException {
        threadPool = new ThreadPool();
        dispatcher = new Dispatcher();
        encoderService = new EncoderService(threadPool, dispatcher);
        encoderService.postConstruct();
        stubService = new StubService();
        stubService.postConstruct();
        outgoingDatagramEvents = new LinkedBlockingQueue<>(32);
    }

    @After
    public void after() {
        encoderService.finish();
        stubService.finish();
    }

    @Test
    public void testEncoder() throws InterruptedException {
        // Create event
        String testPayload = "helloworld";
        SocketAddress testTargetAddress = NetworkUtils.generateSocketAddress();
        logger.info("Test with payload='{}' and targetAddress={}", testPayload, testTargetAddress);
        dispatcher.dispatch(new OutgoingPayloadEvent(testPayload, testTargetAddress));
        // Waiting event
        OutgoingDatagramEvent outgoingDatagramEvent = outgoingDatagramEvents.poll(1000, TimeUnit.MILLISECONDS);
        ByteBuffer byteBuffer = outgoingDatagramEvent.getByteBuffer();
        String payload = NetworkUtils.extractPayload(byteBuffer);
        SocketAddress targetAddress = outgoingDatagramEvent.getTargetAddress();
        logger.info("Outgoing payload '{}' to address {}", payload, targetAddress);
        assertEquals(testPayload, payload);
        assertEquals(testTargetAddress, targetAddress);
    }

    private class StubService extends Bolt implements OutgoingDatagramEvent.Handler {
        private final Logger logger = LoggerFactory.getLogger(StubService.class);

        public StubService() {
            super("stub", 32);
        }

        @Override
        public void handleOutgoingDatagram(OutgoingDatagramEvent event) throws InterruptedException {
            if (logger.isTraceEnabled()) {
                logger.trace("Handle {}", event);
            }
            outgoingDatagramEvents.put(event);
        }

        public void postConstruct() {
            threadPool.execute(this);
            dispatcher.subscribe(this, OutgoingDatagramEvent.class);
        }
    }
}
