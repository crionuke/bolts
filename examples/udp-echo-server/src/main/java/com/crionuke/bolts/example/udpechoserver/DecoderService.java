package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.Bolt;
import com.crionuke.bolts.Dispatcher;
import com.crionuke.bolts.example.udpechoserver.events.IncomingDatagramEvent;
import com.crionuke.bolts.example.udpechoserver.events.IncomingPayloadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;

@Service
class DecoderService extends Bolt implements IncomingDatagramEvent.Handler {
    static private final Logger logger = LoggerFactory.getLogger(DecoderService.class);

    private final ThreadPool threadPool;
    private final Dispatcher dispatcher;

    DecoderService(ThreadPool threadPool, Dispatcher dispatcher) {
        super("decoder", 32);
        this.threadPool = threadPool;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleIncomingDatagram(IncomingDatagramEvent event) throws InterruptedException {
        if (logger.isTraceEnabled()) {
            logger.trace("Handle {}", event);
        }
        ByteBuffer byteBuffer = event.getByteBuffer();
        int length = byteBuffer.getShort();
        if (byteBuffer.remaining() == length) {
            byte[] bytes = new byte[length];
            byteBuffer.get(bytes);
            String payload = new String(bytes);
            dispatcher.dispatch(new IncomingPayloadEvent(payload, event.getSourceAddress()));
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("{} has wrong byteBuffer", event);
            }
        }
    }

    @PostConstruct
    void postConstruct() {
        threadPool.execute(this);
        dispatcher.subscribe(this, IncomingDatagramEvent.class);
    }
}
