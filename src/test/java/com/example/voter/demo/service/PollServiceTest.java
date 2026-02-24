package com.example.voter.demo.service;

import com.example.voter.demo.model.Poll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PollServiceTest {

    private PollService pollService;

    @BeforeEach
    void setUp() {
        pollService = new PollService();
    }

    @Test
    void testCreatePoll() {
        Poll poll = pollService.createPoll("Test Poll", "127.0.0.1");

        assertNotNull(poll);
        assertNotNull(poll.getId());
        assertEquals("Test Poll", poll.getName());
        assertEquals("127.0.0.1", poll.getCreatorIp());
    }

    @Test
    void testGetPoll() {
        Poll created = pollService.createPoll("Test Poll", "127.0.0.1");

        Poll fetched = pollService.getPoll(created.getId());
        assertNotNull(fetched);
        assertEquals(created.getId(), fetched.getId());

        Poll notFound = pollService.getPoll("nonexistent");
        assertNull(notFound);
    }

    @Test
    void testListPolls() {
        pollService.createPoll("Poll 1", "ip1");
        pollService.createPoll("Poll 2", "ip2");

        List<Poll> polls = pollService.listPolls();
        assertEquals(2, polls.size());
    }

    @Test
    void testVote() {
        Poll poll = pollService.createPoll("Vote Poll", "ip1");

        Poll updatedPoll = pollService.vote(poll.getId(), "user1", "yes", "user1_ip");
        assertNotNull(updatedPoll);
        assertEquals(1, updatedPoll.getVotes().size());
        assertEquals("yes", updatedPoll.getVotes().get("user1").getVoteValue());
    }

    @Test
    void testVoteNonExistentPoll() {
        Poll poll = pollService.vote("nonexistent", "user1", "yes", "user1_ip");
        assertNull(poll);
    }

    @Test
    void testSubscribe() {
        Poll poll = pollService.createPoll("Poll Subscribe", "ip");
        SseEmitter emitter = pollService.subscribe(poll.getId());
        assertNotNull(emitter);
        assertEquals(0L, emitter.getTimeout());
    }

    @Test
    void testDeletePollsOlderThan() throws Exception {
        Poll poll1 = pollService.createPoll("Old Poll", "ip");
        Poll poll2 = pollService.createPoll("New Poll", "ip");

        pollService.deletePollsOlderThan(-1); // delete polls older than -1 hours (in the future, so both deleted)
        assertTrue(pollService.listPolls().isEmpty());
    }

    @Test
    void testCleanupOldPolls() {
        // Since we can't easily mock Instant.now() here, we just call it to ensure no
        // exception.
        pollService.createPoll("Poll", "ip");
        pollService.cleanupOldPolls();
        // The newly created poll is not older than 1 day, so it should still be there
        assertEquals(1, pollService.listPolls().size());
    }
}
