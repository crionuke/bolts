package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.Dispatcher;
import com.crionuke.bolts.example.udpechoserver.events.OutgoingDatagramEvent;
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

public class SenderServiceTest extends Assert {
    static private final Logger logger = LoggerFactory.getLogger(SenderServiceTest.class);

    private ThreadPool threadPool;
    private Dispatcher dispatcher;
    private Channel serverChannel;
    private SenderService senderService;

    @Before
    public void before() throws IOException {
        threadPool = new ThreadPool();
        dispatcher = new Dispatcher();
        serverChannel = new Channel();
        senderService = new SenderService(threadPool, dispatcher, serverChannel);
        senderService.postConstruct();
    }

    @After
    public void after() throws IOException {
        senderService.finish();
        serverChannel.close();
    }

    @Test
    public void testSender() throws IOException, InterruptedException {
        // Connect to server
        DatagramChannel clientChannel = DatagramChannel.open();
        clientChannel.connect(serverChannel.getAddress());
        logger.info("Client connected to {}", serverChannel.getAddress());
        // Create event
        String testPayload = "helloworld";
        SocketAddress testTargetAddress = clientChannel.getLocalAddress();
        logger.info("Test with payload='{}' and targetAddress={}", testPayload, testTargetAddress);
        dispatcher.dispatch(new OutgoingDatagramEvent(
                NetworkUtils.createByteBuffer("helloworld"), testTargetAddress));
        // Receive bytebuffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        clientChannel.receive(byteBuffer);
        byteBuffer.flip();
        String payload = NetworkUtils.extractPayload(byteBuffer);
        logger.info("Client got '{}'", payload);
        assertEquals(testPayload, payload);
    }
}