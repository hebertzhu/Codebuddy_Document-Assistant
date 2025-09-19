package com.literature.assistant.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@FunctionalInterface
public interface SSEHandler {
    void handle(SseEmitter emitter, String data);
}