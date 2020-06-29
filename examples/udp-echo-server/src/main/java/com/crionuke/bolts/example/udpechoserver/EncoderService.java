package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.Bolt;
import com.crionuke.bolts.Dispatcher;
import com.crionuke.bolts.example.udpechoserver.events.OutgoingDatagramEvent;
import com.crionuke.bolts.example.udpechoserver.events.OutgoingPayloadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;

@Service
class EncoderService extends Bolt implements OutgoingPayloadEvent.Handler {
    static private final Logger logger = LoggerFactory.getLogger(EncoderService.class);

    private final ThreadPool threadPool;
    private final Dispatcher dispatcher;

    EncoderService(ThreadPool threadPool, Dispatcher dispatcher) {
        super("encoder", 32);
        this.threadPool = threadPool;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleOutgoingPayload(OutgoingPayloadEvent event) throws InterruptedException {
        if (logger.isTraceEnabled()) {
            logger.trace("Handle {}", event);
        }
        String payload = event.getPayload();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.putShort((short) payload.length());
        byteBuffer.put(payload.getBytes());
        byteBuffer.flip();
        dispatcher.dispatch(new OutgoingDatagramEvent(byteBuffer, event.getTargetAddress()));
    }

    @PostConstruct
    void postConstruct() {
        threadPool.execute(this);
        dispatcher.subscribe(this, OutgoingPayloadEvent.class);
    }
}
