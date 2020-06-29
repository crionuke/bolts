package com.crionuke.bolts.example.udpechoserver.events;

import com.crionuke.bolts.Event;

import java.net.SocketAddress;

public final class OutgoingPayloadEvent extends Event<OutgoingPayloadEvent.Handler> {

    private final String payload;
    private final SocketAddress targetAddress;

    public OutgoingPayloadEvent(String payload, SocketAddress targetAddress) {
        if (payload == null) {
            throw new NullPointerException("payload is null");
        }
        if (targetAddress == null) {
            throw new NullPointerException("targetAddress is null");
        }
        this.payload = payload;
        this.targetAddress = targetAddress;
    }

    @Override
    public void handle(Handler handler) throws InterruptedException {
        handler.handleOutgoingPayload(this);
    }

    public String getPayload() {
        return payload;
    }

    public SocketAddress getTargetAddress() {
        return targetAddress;
    }

    public interface Handler {
        void handleOutgoingPayload(OutgoingPayloadEvent event) throws InterruptedException;
    }
}
