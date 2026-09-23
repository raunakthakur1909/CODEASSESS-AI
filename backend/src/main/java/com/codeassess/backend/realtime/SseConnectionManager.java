package com.codeassess.backend.realtime;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(SseConnectionManager.class);
    private static final long DEFAULT_TIMEOUT = 3600_000L; // 1 hour timeout

    private final Map<String, List<SseEmitter>> connections = new ConcurrentHashMap<>();
    private final Counter eventsSentCounter;

    public SseConnectionManager(MeterRegistry meterRegistry) {
        this.eventsSentCounter = meterRegistry.counter("codeassess.sse.events_sent");
        meterRegistry.gauge("codeassess.sse.active_connections", connections,
                map -> map.values().stream().mapToInt(List::size).sum());
    }

    /**
     * Creates and registers a new SSE connection for a specific channel.
     */
    public SseEmitter connect(String channelId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        connections.computeIfAbsent(channelId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // Register lifecycle callbacks
        emitter.onCompletion(() -> remove(channelId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            remove(channelId, emitter);
        });
        emitter.onError((e) -> remove(channelId, emitter));

        logger.debug("SSE connection established for channel {}", channelId);
        return emitter;
    }

    /**
     * Publishes an event to all connected clients for the given channel.
     */
    public void publish(String channelId, String eventName, Object event) {
        List<SseEmitter> emitters = connections.get(channelId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(event));
                eventsSentCounter.increment();
            } catch (IOException e) {
                logger.warn("Failed to send SSE to emitter for channel {}, removing connection. Error: {}", channelId, e.getMessage());
                remove(channelId, emitter);
            }
        }
    }

    /**
     * Sends a lightweight heartbeat comment to all active connections to prevent timeouts.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 25000)
    public void sendHeartbeat() {
        if (connections.isEmpty()) {
            return;
        }
        connections.forEach((channelId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    // Send an empty comment line as a heartbeat
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (IOException e) {
                    remove(channelId, emitter);
                }
            }
        });
    }

    /**
     * Safely removes a dead or completed emitter.
     */
    public void remove(String channelId, SseEmitter emitter) {
        List<SseEmitter> emitters = connections.get(channelId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                connections.remove(channelId);
            }
        }
    }

    /**
     * Used for testing: returns the number of active connections for a channel.
     */
    int getConnectionCount(String channelId) {
        List<SseEmitter> emitters = connections.get(channelId);
        return emitters != null ? emitters.size() : 0;
    }
}
