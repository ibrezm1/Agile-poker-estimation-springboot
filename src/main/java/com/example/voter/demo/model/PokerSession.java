package com.example.voter.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.UUID;

public class PokerSession {
    private String id;
    private String name;
    private String creatorIp;
    private Instant createdAt;
    private String pmCode;
    
    private List<Topic> topics;
    private String activeTopicId;

    public PokerSession() {
    }

    public PokerSession(String name, String creatorIp) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.creatorIp = creatorIp;
        this.createdAt = Instant.now();
        this.pmCode = String.format("%03d", new java.util.Random().nextInt(1000));
        this.topics = new CopyOnWriteArrayList<>();
        this.activeTopicId = null; // PM must create the first topic
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

    public String getCreatorIp() {
        return creatorIp;
    }

    public void setCreatorIp(String creatorIp) {
        this.creatorIp = creatorIp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getPmCode() {
        return pmCode;
    }

    public void setPmCode(String pmCode) {
        this.pmCode = pmCode;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public String getActiveTopicId() {
        return activeTopicId;
    }

    public void setActiveTopicId(String activeTopicId) {
        this.activeTopicId = activeTopicId;
    }

    @JsonIgnore
    public Topic getActiveTopic() {
        if (topics == null || activeTopicId == null) return null;
        for (Topic t : topics) {
            if (activeTopicId.equals(t.getId())) {
                return t;
            }
        }
        return null;
    }

    public Topic createNewTopic(String topicName) {
        Topic newTopic = new Topic(topicName);
        this.topics.add(newTopic);
        this.activeTopicId = newTopic.getId();
        return newTopic;
    }
}
