package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.Bolt;
import com.crionuke.bolts.Dispatcher;
import com.crionuke.bolts.example.udpechoserver.events.IncomingDatagramEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ReceiverServiceTest extends Assert {
    static private final Logger logger = LoggerFactory.getLogger(ReceiverServiceTest.class);

    private ThreadPool threadPool;
    private Dispatcher dispatcher;
    private Channel serverChannel;
    private ReceiverService receiverService;
    private StubService stubService;

    private BlockingQueue<IncomingDatagramEvent> incomingDatagramEvents;

    @Before
    public void before() throws IOException {
        threadPool = new ThreadPool();
        dispatcher = new Dispatcher();
        serverChannel = new Channel();
        receiverService = new ReceiverService(threadPool, dispatcher, serverChannel);
        receiverService.postConstruct();
        stubService = new StubService();
        stubService.postConstruct();
        incomingDatagramEvents = new LinkedBlockingQueue<>(32);
    }

    @After
    public void after() throws IOException {
        receiverService.finish();
        stubService.finish();
        serverChannel.close();
    }

    @Test
    public void testReceiver() throws IOException, InterruptedException {
        // Connect to server
        DatagramChannel clientChannel = DatagramChannel.open();
        clientChannel.connect(serverChannel.getAddress());
        logger.info("Client connected to {}", serverChannel.getAddress());
        // Create datagram
        String outgoingPayload = "helloworld";
        ByteBuffer outgoingByteBuffer = ByteBuffer.allocate(outgoingPayload.length() + Short.BYTES);
        outgoingByteBuffer.putShort((short) outgoingPayload.length());
        outgoingByteBuffer.put(outgoingPayload.getBytes());
        outgoingByteBuffer.flip();
        clientChannel.write(outgoingByteBuffer);
        // Waiting incomingEvent
        IncomingDatagramEvent incomingEvent = incomingDatagramEvents.poll(1000, TimeUnit.MILLISECONDS);
        ByteBuffer incomingByteBuffer = incomingEvent.getByteBuffer();
        short length = incomingByteBuffer.getShort();
        byte[] bytes = new byte[length];
        incomingByteBuffer.get(bytes);
        String incomingPayload = new String(bytes);
        SocketAddress sourceAddress = incomingEvent.getSourceAddress();
        logger.info("Got '{}' from address {}", incomingPayload, sourceAddress);
        assertEquals(incomingPayload, outgoingPayload);
        assertEquals(sourceAddress, clientChannel.getLocalAddress());
    }

    private class StubService extends Bolt implements IncomingDatagramEvent.Handler {
        private final Logger logger = LoggerFactory.getLogger(StubService.class);

        public StubService() {
            super("stub", 32);
        }

        @Override
        public void handleIncomingDatagram(IncomingDatagramEvent event) throws InterruptedException {
            if (logger.isTraceEnabled()) {
                logger.trace("Handle {}", event);
            }
            incomingDatagramEvents.put(event);
        }

        public void postConstruct() {
            threadPool.execute(this);
            dispatcher.subscribe(this, IncomingDatagramEvent.class);
        }
    }
}
