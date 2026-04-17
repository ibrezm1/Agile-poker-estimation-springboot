package com.example.voter.demo.service;

import com.example.voter.demo.model.PokerSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class PokerSessionService {
    private final Map<String, PokerSession> sessions = new ConcurrentHashMap<>();
    // CopyOnWriteArrayList is thread-safe: safe to iterate while other threads add/remove
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private static final int MAX_SESSIONS = 50;

    public PokerSession createSession(String name, String creatorIp) {
        if (sessions.size() >= MAX_SESSIONS) {
            evictOldestSession();
        }
        PokerSession session = new PokerSession(name, creatorIp);
        sessions.put(session.getId(), session);
        emitters.put(session.getId(), new CopyOnWriteArrayList<>());
        return session;
    }

    private void evictOldestSession() {
        PokerSession oldest = null;
        for (PokerSession s : sessions.values()) {
            if (oldest == null || s.getCreatedAt().isBefore(oldest.getCreatedAt())) {
                oldest = s;
            }
        }

        if (oldest != null) {
            String sessionId = oldest.getId();
            sessions.remove(sessionId);
            List<SseEmitter> sessionEmitters = emitters.remove(sessionId);
            if (sessionEmitters != null) {
                sessionEmitters.forEach(SseEmitter::complete);
            }
        }
    }

    public PokerSession getSession(String id) {
        return sessions.get(id);
    }

    public List<PokerSession> listSessions() {
        return new ArrayList<>(sessions.values());
    }

    public SseEmitter subscribe(String sessionId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(sessionId, emitter));
        emitter.onTimeout(() -> removeEmitter(sessionId, emitter));
        emitter.onError((e) -> removeEmitter(sessionId, emitter));

        return emitter;
    }

    private void removeEmitter(String sessionId, SseEmitter emitter) {
        List<SseEmitter> sessionEmitters = emitters.get(sessionId);
        if (sessionEmitters != null) {
            sessionEmitters.remove(emitter);
        }
    }

    public void broadcastSessionState(String sessionId) {
        PokerSession session = sessions.get(sessionId);
        if (session == null) return;

        List<SseEmitter> sessionEmitters = emitters.get(sessionId);
        if (sessionEmitters == null) return;

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : sessionEmitters) {
            try {
                emitter.send(SseEmitter.event().data(session));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }
        sessionEmitters.removeAll(deadEmitters);
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanupOldSessions() {
        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        sessions.entrySet().removeIf(entry -> {
            boolean old = entry.getValue().getCreatedAt().isBefore(oneDayAgo);
            if (old) {
                List<SseEmitter> sessionEmitters = emitters.remove(entry.getKey());
                if (sessionEmitters != null) {
                    sessionEmitters.forEach(SseEmitter::complete);
                }
            }
            return old;
        });
    }

    public void deleteSessionsOlderThan(long hours) {
        Instant threshold = Instant.now().minus(hours, ChronoUnit.HOURS);
        sessions.entrySet().removeIf(entry -> {
            boolean old = entry.getValue().getCreatedAt().isBefore(threshold);
            if (old) {
                List<SseEmitter> sessionEmitters = emitters.remove(entry.getKey());
                if (sessionEmitters != null) {
                    sessionEmitters.forEach(SseEmitter::complete);
                }
            }
            return old;
        });
    }
}
