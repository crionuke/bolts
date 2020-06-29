package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.Dispatcher;
import com.crionuke.bolts.Worker;
import com.crionuke.bolts.example.udpechoserver.events.IncomingDatagramEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
class ReceiverService extends Worker {
    static private final Logger logger = LoggerFactory.getLogger(ReceiverService.class);

    private final ThreadPool threadPool;
    private final Dispatcher dispatcher;
    private final Channel.Receiver receiver;

    ReceiverService(ThreadPool threadPool, Dispatcher dispatcher, Channel channel) {
        this.threadPool = threadPool;
        this.dispatcher = dispatcher;
        receiver = channel.getReceiver();
    }

    @Override
    public void run() {
        String oldThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName("receiver");
        logger.debug("{} started", this);
        try {
            looping = true;
            while (looping) {
                IncomingDatagramEvent incomingDatagramEvent = receiver.receive();
                dispatcher.dispatch(incomingDatagramEvent);
            }
        } catch (InterruptedException e) {
            logger.debug("{} interrupted", this);
            looping = false;
        } catch (IOException ioe) {
            logger.warn("{}", ioe);
            looping = false;
        }
        logger.debug("{} finished", this);
        Thread.currentThread().setName(oldThreadName);
    }

    @PostConstruct
    void postConstruct() {
        threadPool.execute(this);
    }
}
