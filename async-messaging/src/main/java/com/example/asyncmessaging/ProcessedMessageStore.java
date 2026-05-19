package com.example.asyncmessaging;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
class ProcessedMessageStore {

    private final Set<String> processedKeys = ConcurrentHashMap.newKeySet();

    boolean markIfFirst(String consumerName, String eventId) {
        return processedKeys.add(consumerName + ":" + eventId);
    }
}
