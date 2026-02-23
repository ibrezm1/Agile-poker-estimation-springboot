package com.example.voter.demo.service;

import com.example.voter.demo.model.Poll;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public Poll createPoll(String name, String creatorIp) {
        String id = UUID.randomUUID().toString();
        Poll poll = new Poll(id, name, creatorIp);
        polls.put(id, poll);
        emitters.put(id, new ArrayList<>());
        return poll;
    }

    public Poll getPoll(String id) {
        return polls.get(id);
    }

    public List<Poll> listPolls() {
        return new ArrayList<>(polls.values());
    }

    public SseEmitter subscribe(String pollId) {
        SseEmitter emitter = new SseEmitter(0L); // Infinite timeout
        emitters.computeIfAbsent(pollId, k -> new ArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(pollId, emitter));
        emitter.onTimeout(() -> removeEmitter(pollId, emitter));
        emitter.onError((e) -> removeEmitter(pollId, emitter));

        return emitter;
    }

    private void removeEmitter(String pollId, SseEmitter emitter) {
        List<SseEmitter> pollEmitters = emitters.get(pollId);
        if (pollEmitters != null) {
            pollEmitters.remove(emitter);
        }
    }

    public Poll vote(String pollId, String username, String voteValue, String ipAddress) {
        Poll poll = polls.get(pollId);
        if (poll != null) {
            poll.addVote(username, voteValue, ipAddress);
            notifySubscribers(pollId, poll.getVotes());
        }
        return poll;
    }

    private void notifySubscribers(String pollId, Map<String, com.example.voter.demo.model.Vote> votes) {
        List<SseEmitter> pollEmitters = emitters.get(pollId);
        if (pollEmitters != null) {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            pollEmitters.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event().data(votes));
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                }
            });
            pollEmitters.removeAll(deadEmitters);
        }
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
