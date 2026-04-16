package com.example.voter.demo.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PokerSessionTest {

    @Test
    void testPokerSessionCreation() {
        PokerSession session = new PokerSession("Test Session", "192.168.1.1");

        assertNotNull(session.getId());
        assertEquals("Test Session", session.getName());
        assertEquals("192.168.1.1", session.getCreatorIp());
        assertNotNull(session.getCreatedAt());
        assertEquals(3, session.getPmCode().length());

        // Sessions start with NO topics — PM must create them
        assertEquals(0, session.getTopics().size());
        assertNull(session.getActiveTopicId());
        assertNull(session.getActiveTopic());
    }

    @Test
    void testCreateNewTopic() {
        PokerSession session = new PokerSession("S1", "ip");
        Topic t2 = session.createNewTopic("Topic 2");
        
        assertEquals(2, session.getTopics().size());
        assertEquals("Topic 2", t2.getName());
        assertEquals(t2.getId(), session.getActiveTopicId());
    }

    @Test
    void testGetActiveTopic() {
        PokerSession session = new PokerSession("S1", "ip");
        // No topics yet
        assertNull(session.getActiveTopic());

        // After PM creates one, it should be retrievable
        Topic t = session.createNewTopic("First Topic");
        assertNotNull(session.getActiveTopic());
        assertEquals("First Topic", session.getActiveTopic().getName());
    }
}
