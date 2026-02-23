package com.example.voter.demo.model;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Poll {
    private String id;
    private String name;
    private String creatorIp;
    private Instant createdAt;
    private Map<String, Vote> votes;

    public Poll(String id, String name, String creatorIp) {
        this.id = id;
        this.name = name;
        this.creatorIp = creatorIp;
        this.createdAt = Instant.now();
        this.votes = new ConcurrentHashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreatorIp() {
        return creatorIp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Map<String, Vote> getVotes() {
        return votes;
    }

    public void addVote(String username, String voteValue, String ipAddress) {
        votes.put(username, new Vote(username, voteValue, ipAddress));
    }
}
