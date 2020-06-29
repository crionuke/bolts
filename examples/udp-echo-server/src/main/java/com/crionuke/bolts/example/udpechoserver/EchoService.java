package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.Bolt;
import com.crionuke.bolts.Dispatcher;
import com.crionuke.bolts.example.udpechoserver.events.IncomingPayloadEvent;
import com.crionuke.bolts.example.udpechoserver.events.OutgoingPayloadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
class EchoService extends Bolt implements IncomingPayloadEvent.Handler {
    static private final Logger logger = LoggerFactory.getLogger(EchoService.class);

    private final ThreadPool threadPool;
    private final Dispatcher dispatcher;

    EchoService(ThreadPool threadPool, Dispatcher dispatcher) {
        super("echo", 32);
        this.threadPool = threadPool;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleIncomingPayload(IncomingPayloadEvent event) throws InterruptedException {
        if (logger.isTraceEnabled()) {
            logger.trace("Handle {}", event);
        }
        // Echo
        dispatcher.dispatch(new OutgoingPayloadEvent(event.getPayload(), event.getSourceAddress()));
    }

    @PostConstruct
    void postConstruct() {
        threadPool.execute(this);
        dispatcher.subscribe(this, IncomingPayloadEvent.class);
    }
}
