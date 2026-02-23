package com.example.voter.demo.controller;

import com.example.voter.demo.model.Poll;
import com.example.voter.demo.service.PollService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/polls")
public class PollController {

    private final PollService pollService;

    public PollController(PollService pollService) {
        this.pollService = pollService;
    }

    public static class CreatePollRequest {
        public String name;
    }

    public static class VoteRequest {
        public String username;
        public String vote;
    }

    @PostMapping
    public ResponseEntity<java.util.Map<String, Object>> createPoll(@RequestBody CreatePollRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        String name = request.name != null ? request.name : "Unnamed Poll";
        String ip = httpRequest.getRemoteAddr();
        Poll poll = pollService.createPoll(name, ip);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", poll.getId());
        response.put("name", poll.getName());
        response.put("creatorIp", poll.getCreatorIp());
        response.put("pmCode", poll.getPmCode());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Poll>> listPolls() {
        return ResponseEntity.ok(pollService.listPolls());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Poll> getPoll(@PathVariable String id) {
        Poll poll = pollService.getPoll(id);
        if (poll == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(poll);
    }

    @GetMapping("/{id}/validate-pm")
    public ResponseEntity<Boolean> validatePmCode(@PathVariable String id, @RequestParam String code) {
        Poll poll = pollService.getPoll(id);
        if (poll != null && poll.getPmCode() != null && poll.getPmCode().equals(code)) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }

    @GetMapping(value = "/{id}/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.SseEmitter> streamPoll(
            @PathVariable String id) {
        Poll poll = pollService.getPoll(id);
        if (poll == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pollService.subscribe(id));
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<Poll> vote(@PathVariable String id, @RequestBody VoteRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (request.username == null || request.vote == null) {
            return ResponseEntity.badRequest().build();
        }

        String ip = httpRequest.getRemoteAddr();
        Poll poll = pollService.vote(id, request.username, request.vote, ip);
        if (poll == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(poll);
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePollsOlderThan(
            @RequestParam(value = "hours", defaultValue = "1") long hours) {
        pollService.deletePollsOlderThan(hours);
        return ResponseEntity.noContent().build();
    }
}
