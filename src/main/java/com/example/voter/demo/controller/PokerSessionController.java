package com.example.voter.demo.controller;

import com.example.voter.demo.model.PokerSession;
import com.example.voter.demo.model.Topic;
import com.example.voter.demo.service.PokerSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class PokerSessionController {

    private final PokerSessionService sessionService;

    public PokerSessionController(PokerSessionService sessionService) {
        this.sessionService = sessionService;
    }

    public static class CreateSessionRequest {
        public String name;
    }

    public static class VoteRequest {
        public String username;
        public String vote;
    }

    public static class CreateTopicRequest {
        public String name;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSession(@RequestBody CreateSessionRequest request, HttpServletRequest httpRequest) {
        String name = request.name != null ? request.name : "Unnamed Session";
        String ip = httpRequest.getRemoteAddr();
        PokerSession session = sessionService.createSession(name, ip);

        Map<String, Object> response = new HashMap<>();
        response.put("id", session.getId());
        response.put("name", session.getName());
        response.put("creatorIp", session.getCreatorIp());
        response.put("pmCode", session.getPmCode());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PokerSession>> listSessions() {
        return ResponseEntity.ok(sessionService.listSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PokerSession> getSession(@PathVariable String id) {
        PokerSession session = sessionService.getSession(id);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{id}/validate-pm")
    public ResponseEntity<Boolean> validatePmCode(@PathVariable String id, @RequestParam String code) {
        PokerSession session = sessionService.getSession(id);
        if (session != null && session.getPmCode() != null && session.getPmCode().equals(code)) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.SseEmitter> streamSession(@PathVariable String id) {
        PokerSession session = sessionService.getSession(id);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessionService.subscribe(id));
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<PokerSession> vote(@PathVariable String id, @RequestBody VoteRequest request, HttpServletRequest httpRequest) {
        if (request.username == null || request.vote == null) {
            return ResponseEntity.badRequest().build();
        }

        PokerSession session = sessionService.getSession(id);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        Topic active = session.getActiveTopic();
        if (active != null) {
            if (active.isRevealed()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Topic already closed
            }
            active.addVote(request.username, request.vote, httpRequest.getRemoteAddr());
            sessionService.broadcastSessionState(id);
        }
        return ResponseEntity.ok(session);
    }

    // Vote on a specific topic by ID (not just the active one)
    @PostMapping("/{id}/topics/{topicId}/vote")
    public ResponseEntity<PokerSession> voteOnTopic(
            @PathVariable String id,
            @PathVariable String topicId,
            @RequestBody VoteRequest request,
            HttpServletRequest httpRequest) {

        if (request.username == null || request.vote == null) {
            return ResponseEntity.badRequest().build();
        }

        PokerSession session = sessionService.getSession(id);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        Topic target = session.getTopics().stream()
                .filter(t -> t.getId().equals(topicId))
                .findFirst().orElse(null);

        if (target == null) {
            return ResponseEntity.notFound().build();
        }
        if (target.isRevealed()) {
            // Topic is closed — no more votes
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        target.addVote(request.username, request.vote, httpRequest.getRemoteAddr());
        sessionService.broadcastSessionState(id);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{id}/reveal")
    public ResponseEntity<Void> revealTopic(@PathVariable String id, @RequestParam String code) {
        PokerSession session = sessionService.getSession(id);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        if (session.getPmCode() == null || !session.getPmCode().equals(code)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Topic active = session.getActiveTopic();
        if (active != null) {
            active.setRevealed(true);
            sessionService.broadcastSessionState(id);
        }
        return ResponseEntity.ok().build();
    }

    // Reveal a specific topic by topicId (PM only)
    @PostMapping("/{id}/topics/{topicId}/reveal")
    public ResponseEntity<Void> revealSpecificTopic(
            @PathVariable String id,
            @PathVariable String topicId,
            @RequestParam String code) {

        PokerSession session = sessionService.getSession(id);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        if (session.getPmCode() == null || !session.getPmCode().equals(code)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        session.getTopics().stream()
                .filter(t -> t.getId().equals(topicId))
                .findFirst()
                .ifPresent(t -> {
                    t.setRevealed(true);
                    sessionService.broadcastSessionState(id);
                });
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/topics")
    public ResponseEntity<Topic> createTopic(@PathVariable String id, @RequestParam String code, @RequestBody CreateTopicRequest request) {
        PokerSession session = sessionService.getSession(id);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        if (session.getPmCode() == null || !session.getPmCode().equals(code)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Topic topic = session.createNewTopic(request.name);
        sessionService.broadcastSessionState(id);
        return ResponseEntity.ok(topic);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteSessionsOlderThan(@RequestParam(value = "hours", defaultValue = "1") long hours) {
        sessionService.deleteSessionsOlderThan(hours);
        return ResponseEntity.noContent().build();
    }
}
