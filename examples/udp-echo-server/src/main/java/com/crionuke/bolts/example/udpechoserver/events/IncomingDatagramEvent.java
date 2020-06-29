package com.crionuke.bolts.example.udpechoserver.events;

import com.crionuke.bolts.Event;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public final class IncomingDatagramEvent extends Event<IncomingDatagramEvent.Handler> {

    private final ByteBuffer byteBuffer;
    private final SocketAddress sourceAddress;

    public IncomingDatagramEvent(ByteBuffer byteBuffer, SocketAddress socketAddress) {
        if (byteBuffer == null) {
            throw new NullPointerException("byteBuffer is null");
        }
        if (socketAddress == null) {
            throw new NullPointerException("socketAddress is null");
        }
        this.byteBuffer = byteBuffer;
        this.sourceAddress = socketAddress;
    }

    @Override
    public void handle(Handler handler) throws InterruptedException {
        handler.handleIncomingDatagram(this);
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public SocketAddress getSourceAddress() {
        return sourceAddress;
    }

    public interface Handler {
        void handleIncomingDatagram(IncomingDatagramEvent event) throws InterruptedException;
    }
}
