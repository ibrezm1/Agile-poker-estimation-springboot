package com.example.voter.demo.service;

import com.example.voter.demo.model.Poll;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PollService {
    private final Map<String, Poll> polls = new ConcurrentHashMap<>();

    public Poll createPoll(String name, String creatorIp) {
        String id = UUID.randomUUID().toString();
        Poll poll = new Poll(id, name, creatorIp);
        polls.put(id, poll);
        return poll;
    }

    public Poll getPoll(String id) {
        return polls.get(id);
    }

    public List<Poll> listPolls() {
        return new ArrayList<>(polls.values());
    }

    public Poll vote(String pollId, String username, String voteValue, String ipAddress) {
        Poll poll = polls.get(pollId);
        if (poll != null) {
            poll.addVote(username, voteValue, ipAddress);
        }
        return poll;
    }

    // Run every hour to clean up polls older than 1 day
    @Scheduled(fixedRate = 3600000)
    public void cleanupOldPolls() {
        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        polls.entrySet().removeIf(entry -> entry.getValue().getCreatedAt().isBefore(oneDayAgo));
    }

    public void deletePollsOlderThan(long hours) {
        Instant threshold = Instant.now().minus(hours, ChronoUnit.HOURS);
        polls.values().removeIf(poll -> poll.getCreatedAt().isBefore(threshold));
    }
}
