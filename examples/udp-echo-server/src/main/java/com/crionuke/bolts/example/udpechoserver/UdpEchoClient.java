package com.crionuke.bolts.example.udpechoserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpEchoClient {
    static private final Logger logger = LoggerFactory.getLogger(UdpEchoClient.class);

    public static void main(String[] args) throws IOException {
        DatagramChannel clientChannel = DatagramChannel.open();
        clientChannel.connect(new InetSocketAddress("127.0.0.1", 10000));
        logger.info("Connected");
        // Requests
        String payload = String.valueOf(100 + (int) Math.floor(Math.random() * 899));
        ByteBuffer outgoinBytebuffer = NetworkUtils.createByteBuffer(payload);
        clientChannel.write(outgoinBytebuffer);
        logger.info("Send {}", payload);
        // Response
        ByteBuffer incomingByteByffer = ByteBuffer.allocate(1024);
        clientChannel.receive(incomingByteByffer);
        incomingByteByffer.flip();
        logger.info("Got {}", NetworkUtils.extractPayload(incomingByteByffer));
    }
}
