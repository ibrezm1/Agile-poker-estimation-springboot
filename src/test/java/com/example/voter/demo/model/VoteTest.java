package com.example.voter.demo.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VoteTest {

    @Test
    void testVoteConstructorAndGetters() {
        Vote vote = new Vote("user1", "Option A", "127.0.0.1");

        assertEquals("user1", vote.getUsername());
        assertEquals("Option A", vote.getVoteValue());
        assertEquals("127.0.0.1", vote.getIpAddress());
    }

    @Test
    void testEmptyConstructor() {
        Vote vote = new Vote();
        assertNull(vote.getUsername());
        assertNull(vote.getVoteValue());
        assertNull(vote.getIpAddress());
    }
}
