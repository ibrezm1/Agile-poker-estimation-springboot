package com.example.voter.demo.model;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Topic {
    private String id;
    private String name;
    private boolean revealed;
    private Map<String, Vote> votes;

    public Topic() {
    }

    public Topic(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.revealed = false;
        this.votes = new ConcurrentHashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public Map<String, Vote> getVotes() {
        return votes;
    }

    public void addVote(String username, String voteValue, String ipAddress) {
        this.votes.put(username, new Vote(username, voteValue, ipAddress));
    }
}
