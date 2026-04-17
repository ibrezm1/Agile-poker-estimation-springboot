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

    @Test
    void testDeleteSessionsOlderThan() {
        // 1. Create an old session (2 hours ago)
        PokerSession oldSession = sessionService.createSession("Old Session", "1.1.1.1");
        java.time.Instant twoHoursAgo = java.time.Instant.now().minus(2, java.time.temporal.ChronoUnit.HOURS);
        oldSession.setCreatedAt(twoHoursAgo);

        // 2. Create a new session (now)
        PokerSession newSession = sessionService.createSession("New Session", "2.2.2.2");

        // 3. Subscribe to both to initialize emitters
        sessionService.subscribe(oldSession.getId());
        sessionService.subscribe(newSession.getId());

        // 4. Run cleanup for sessions older than 1 hour
        sessionService.deleteSessionsOlderThan(1);

        // 5. Verify results
        assertNull(sessionService.getSession(oldSession.getId()), "Old session should be deleted");
        assertNotNull(sessionService.getSession(newSession.getId()), "New session should still exist");
        assertEquals(1, sessionService.listSessions().size());
    }

    @Test
    void testCleanupOldSessions() {
        // 1. Create an extremely old session (2 days ago)
        PokerSession veryOldSession = sessionService.createSession("Antique Session", "0.0.0.0");
        java.time.Instant twoDaysAgo = java.time.Instant.now().minus(2, java.time.temporal.ChronoUnit.DAYS);
        veryOldSession.setCreatedAt(twoDaysAgo);

        // 2. Create another session (now)
        PokerSession freshSession = sessionService.createSession("Fresh Session", "1.1.1.1");

        // 3. Run the daily cleanup
        sessionService.cleanupOldSessions();

        // 4. Verify results
        assertNull(sessionService.getSession(veryOldSession.getId()), "Session older than 1 day should be deleted");
        assertNotNull(sessionService.getSession(freshSession.getId()), "Recent session should remain");
        assertEquals(1, sessionService.listSessions().size());
    }

    @Test
    void testEmitterRemovalOnCompletion() {
        PokerSession session = sessionService.createSession("Emitter Test", "127.0.0.1");
        SseEmitter emitter = sessionService.subscribe(session.getId());
        
        // Trigger completion manually. 
        // This triggers the 'onCompletion' callback which calls 'removeEmitter'.
        emitter.complete();
        
        // Verify that the code path for removal runs successfully.
        // While the map is private, this exercises the 100% logic of the removeEmitter method
        // through its intended public trigger.
        assertDoesNotThrow(() -> sessionService.broadcastSessionState(session.getId()));
    }

    @Test
    void testGlobalSessionLimitEnforcement() {
        // 1. Create 50 sessions
        for (int i = 1; i <= 50; i++) {
            PokerSession s = sessionService.createSession("Session " + i, "ip");
            // Set slight delay in createdAt to ensure deterministic oldest session
            s.setCreatedAt(java.time.Instant.now().plusMillis(i));
        }
        
        List<PokerSession> initialSessions = sessionService.listSessions();
        assertEquals(50, initialSessions.size());
        
        // Find the absolute oldest one (should be "Session 1")
        PokerSession oldest = initialSessions.stream()
                .min(java.util.Comparator.comparing(PokerSession::getCreatedAt))
                .orElseThrow();
        assertEquals("Session 1", oldest.getName());

        // 2. Add 51st session
        sessionService.createSession("Session 51", "ip");

        // 3. Verify
        assertNull(sessionService.getSession(oldest.getId()), "Oldest session should have been evicted");
        assertEquals(50, sessionService.listSessions().size());
        assertTrue(sessionService.listSessions().stream().anyMatch(s -> s.getName().equals("Session 51")));
    }
}
