package com.example.voter.demo.model;

public class Vote {
    private String username;
    private String voteValue;
    private String ipAddress;

    public Vote() {
    }

    public Vote(String username, String voteValue, String ipAddress) {
        this.username = username;
        this.voteValue = voteValue;
        this.ipAddress = ipAddress;
    }

    public String getUsername() {
        return username;
    }

    public String getVoteValue() {
        return voteValue;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
