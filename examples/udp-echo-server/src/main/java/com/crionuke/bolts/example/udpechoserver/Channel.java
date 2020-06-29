package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.example.udpechoserver.events.IncomingDatagramEvent;
import com.crionuke.bolts.example.udpechoserver.events.OutgoingDatagramEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

@Component
class Channel {
    static private final Logger logger = LoggerFactory.getLogger(Channel.class);

    private final DatagramChannel datagramChannel;
    private final Receiver receiver;
    private final Sender sender;

    Channel() throws IOException {
        datagramChannel = DatagramChannel.open();
        datagramChannel.bind(new InetSocketAddress(InetAddress.getByName("localhost"), 10000));
        logger.info("Datagram channel binded to {}", datagramChannel.getLocalAddress());
        receiver = new Receiver(datagramChannel);
        sender = new Sender(datagramChannel);
    }

    Receiver getReceiver() {
        return receiver;
    }

    Sender getSender() {
        return sender;
    }

    SocketAddress getAddress() throws IOException {
        return datagramChannel.getLocalAddress();
    }

    int getPort() throws IOException {
        return ((InetSocketAddress) datagramChannel.getLocalAddress()).getPort();
    }

    void close() throws IOException {
        datagramChannel.close();
    }

    class Receiver {
        private final DatagramChannel datagramChannel;

        Receiver(DatagramChannel datagramChannel) {
            this.datagramChannel = datagramChannel;
        }

        IncomingDatagramEvent receive() throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            SocketAddress sourceAddress = datagramChannel.receive(byteBuffer);
            byteBuffer.flip();
            return new IncomingDatagramEvent(byteBuffer, sourceAddress);
        }
    }

    class Sender {
        private final DatagramChannel datagramChannel;

        Sender(DatagramChannel datagramChannel) {
            this.datagramChannel = datagramChannel;
        }

        void send(OutgoingDatagramEvent outgoingDatagramEvent) throws IOException {
            datagramChannel.send(outgoingDatagramEvent.getByteBuffer(), outgoingDatagramEvent.getTargetAddress());
        }
    }
}
