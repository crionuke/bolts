package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.Dispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    Dispatcher dispatcher() {
        return new Dispatcher();
    }
}
