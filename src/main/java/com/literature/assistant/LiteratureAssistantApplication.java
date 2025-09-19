package com.literature.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class LiteratureAssistantApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiteratureAssistantApplication.class, args);
    }
}