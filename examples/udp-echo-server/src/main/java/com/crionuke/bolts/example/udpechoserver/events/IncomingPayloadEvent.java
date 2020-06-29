package com.crionuke.bolts.example.udpechoserver.events;

import com.crionuke.bolts.Event;

import java.net.SocketAddress;

public final class IncomingPayloadEvent extends Event<IncomingPayloadEvent.Handler> {

    private final String payload;
    private final SocketAddress sourceAddress;

    public IncomingPayloadEvent(String payload, SocketAddress socketAddress) {
        if (payload == null) {
            throw new NullPointerException("payload is null");
        }
        if (socketAddress == null) {
            throw new NullPointerException("socketAddress is null");
        }
        this.payload = payload;
        this.sourceAddress = socketAddress;
    }

    @Override
    public void handle(Handler handler) throws InterruptedException {
        handler.handleIncomingPayload(this);
    }

    public String getPayload() {
        return payload;
    }

    public SocketAddress getSourceAddress() {
        return sourceAddress;
    }

    public interface Handler {
        void handleIncomingPayload(IncomingPayloadEvent event) throws InterruptedException;
    }
}
