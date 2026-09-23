package com.codeassess.backend.realtime;

import com.codeassess.backend.submission.SubmissionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SseConnectionManagerTest {

    private SseConnectionManager manager;

    @BeforeEach
    void setUp() {
        manager = new SseConnectionManager(new io.micrometer.core.instrument.simple.SimpleMeterRegistry());
    }

    @Test
    void connect_ShouldRegisterEmitter() {
        SseEmitter emitter = manager.connect("submission:10");
        assertThat(emitter).isNotNull();
        assertThat(manager.getConnectionCount("submission:10")).isEqualTo(1);
    }

    @Test
    void connect_MultipleClients_ShouldRegisterAll() {
        manager.connect("submission:10");
        manager.connect("submission:10");
        manager.connect("submission:10");

        assertThat(manager.getConnectionCount("submission:10")).isEqualTo(3);
    }

    @Test
    void connect_Isolation_DifferentSubmissions() {
        manager.connect("submission:10");
        manager.connect("submission:20");

        assertThat(manager.getConnectionCount("submission:10")).isEqualTo(1);
        assertThat(manager.getConnectionCount("submission:20")).isEqualTo(1);
    }

    @Test
    void remove_ShouldUnregisterEmitter() {
        SseEmitter emitter = manager.connect("submission:10");
        assertThat(manager.getConnectionCount("submission:10")).isEqualTo(1);

        manager.remove("submission:10", emitter);
        assertThat(manager.getConnectionCount("submission:10")).isEqualTo(0);
    }

    @Test
    void publish_ShouldNotThrowIfNoEmitters() {
        SubmissionEvent event = new SubmissionEvent(
                10L, SubmissionStatus.RUNNING, null, null, null, null, null, Instant.now()
        );
        // Should execute safely without NPE or errors
        manager.publish("submission:10", "submission-status", event);
        assertThat(manager.getConnectionCount("submission:10")).isEqualTo(0);
    }
}
