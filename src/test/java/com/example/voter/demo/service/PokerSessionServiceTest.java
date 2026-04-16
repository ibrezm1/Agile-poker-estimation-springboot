package com.example.voter.demo.service;

import com.example.voter.demo.model.PokerSession;
import com.example.voter.demo.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PokerSessionServiceTest {

    private PokerSessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new PokerSessionService();
    }

    @Test
    void testCreateSession() {
        PokerSession session = sessionService.createSession("Test Session", "127.0.0.1");

        assertNotNull(session);
        assertNotNull(session.getId());
        assertEquals("Test Session", session.getName());
    }

    @Test
    void testGetSession() {
        PokerSession created = sessionService.createSession("Test Session", "127.0.0.1");
        PokerSession fetched = sessionService.getSession(created.getId());
        assertNotNull(fetched);
        assertEquals(created.getId(), fetched.getId());
    }

    @Test
    void testListSessions() {
        sessionService.createSession("S1", "ip1");
        sessionService.createSession("S2", "ip2");
        List<PokerSession> sessions = sessionService.listSessions();
        assertEquals(2, sessions.size());
    }

    @Test
    void testSubscribe() {
        PokerSession session = sessionService.createSession("Session", "ip");
        SseEmitter emitter = sessionService.subscribe(session.getId());
        assertNotNull(emitter);
    }

    @Test
    void testBroadcastSessionState() {
        PokerSession session = sessionService.createSession("Broadcast", "ip");
        sessionService.subscribe(session.getId());
        assertDoesNotThrow(() -> sessionService.broadcastSessionState(session.getId()));
    }
}
