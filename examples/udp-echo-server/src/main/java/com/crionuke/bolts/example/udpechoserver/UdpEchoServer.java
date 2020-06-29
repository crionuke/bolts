package com.crionuke.bolts.example.udpechoserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.crionuke")
public class UdpEchoServer {

    public static void main(String[] args) {
        SpringApplication.run(UdpEchoServer.class, args);
    }
}