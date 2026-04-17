package com.example.voter.demo.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TopicTest {

    @Test
    void testTopicCreation() {
        Topic topic = new Topic("Demo Topic");
        assertNotNull(topic.getId());
        assertEquals("Demo Topic", topic.getName());
        assertFalse(topic.isRevealed());
        assertNotNull(topic.getVotes());
        assertTrue(topic.getVotes().isEmpty());
    }

    @Test
    void testAddVote() {
        Topic topic = new Topic("Voting Topic");
        topic.addVote("user1", "8", "192.168.1.10");

        assertEquals(1, topic.getVotes().size());
        Vote vote = topic.getVotes().get("user1");
        assertNotNull(vote);
        assertEquals("user1", vote.getUsername());
        assertEquals("8", vote.getVoteValue());
        assertEquals("192.168.1.10", vote.getIpAddress());
    }

    @Test
    void testTopicRevealed() {
        Topic topic = new Topic("Reveal Topic");
        assertFalse(topic.isRevealed());
        topic.setRevealed(true);
        assertTrue(topic.isRevealed());
    }
}
