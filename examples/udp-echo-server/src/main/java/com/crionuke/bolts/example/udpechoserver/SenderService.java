package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.Bolt;
import com.crionuke.bolts.Dispatcher;
import com.crionuke.bolts.example.udpechoserver.events.OutgoingDatagramEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
class SenderService extends Bolt implements OutgoingDatagramEvent.Handler {
    static private final Logger logger = LoggerFactory.getLogger(SenderService.class);

    private final ThreadPool threadPool;
    private final Dispatcher dispatcher;
    private final Channel.Sender sender;

    SenderService(ThreadPool threadPool, Dispatcher dispatcher, Channel channel) {
        super("sender", 32);
        this.threadPool = threadPool;
        this.dispatcher = dispatcher;
        this.sender = channel.getSender();
    }

    @Override
    public void handleOutgoingDatagram(OutgoingDatagramEvent event) throws InterruptedException {
        if (logger.isTraceEnabled()) {
            logger.trace("Handle {}", event);
        }
        try {
            sender.send(event);
        } catch (IOException ioe) {
            logger.info("Sender exception {}", ioe);
        }
    }

    @PostConstruct
    void postConstruct() {
        threadPool.execute(this);
        dispatcher.subscribe(this, OutgoingDatagramEvent.class);
    }
}
