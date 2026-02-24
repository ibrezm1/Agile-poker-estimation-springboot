package com.example.voter.demo.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PollTest {

    @Test
    void testPollCreation() {
        Poll poll = new Poll("poll-123", "My Poll", "192.168.1.1");

        assertEquals("poll-123", poll.getId());
        assertEquals("My Poll", poll.getName());
        assertEquals("192.168.1.1", poll.getCreatorIp());
        assertNotNull(poll.getCreatedAt());
        assertNotNull(poll.getVotes());
        assertTrue(poll.getVotes().isEmpty());
        assertNotNull(poll.getPmCode());
        assertEquals(2, poll.getPmCode().length()); // PM Code format "%02d"
    }

    @Test
    void testAddVote() {
        Poll poll = new Poll("poll-123", "My Poll", "192.168.1.1");

        poll.addVote("alice", "Option X", "10.0.0.1");

        assertEquals(1, poll.getVotes().size());
        assertTrue(poll.getVotes().containsKey("alice"));

        Vote vote = poll.getVotes().get("alice");
        assertEquals("alice", vote.getUsername());
        assertEquals("Option X", vote.getVoteValue());
        assertEquals("10.0.0.1", vote.getIpAddress());
    }

    @Test
    void testUpdateVote() {
        Poll poll = new Poll("poll-123", "My Poll", "192.168.1.1");

        poll.addVote("alice", "Option X", "10.0.0.1");
        poll.addVote("alice", "Option Y", "10.0.0.1"); // Voted again

        assertEquals(1, poll.getVotes().size());
        assertEquals("Option Y", poll.getVotes().get("alice").getVoteValue());
    }
}
