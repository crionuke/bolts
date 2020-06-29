package com.crionuke.bolts.example.udpechoserver.events;

import com.crionuke.bolts.Event;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public final class OutgoingDatagramEvent extends Event<OutgoingDatagramEvent.Handler> {

    private ByteBuffer byteBuffer;
    private SocketAddress targetAddress;

    public OutgoingDatagramEvent(ByteBuffer byteBuffer, SocketAddress targetAddress) {
        if (byteBuffer == null) {
            throw new NullPointerException("byteBuffer is null");
        }
        if (targetAddress == null) {
            throw new NullPointerException("targetAddress is null");
        }
        this.byteBuffer = byteBuffer;
        this.targetAddress = targetAddress;
    }

    @Override
    public void handle(Handler handler) throws InterruptedException {
        handler.handleOutgoingDatagram(this);
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public SocketAddress getTargetAddress() {
        return targetAddress;
    }

    public interface Handler {
        void handleOutgoingDatagram(OutgoingDatagramEvent event) throws InterruptedException;
    }
}
