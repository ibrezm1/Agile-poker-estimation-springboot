package com.example.voter.demo.controller;

import com.example.voter.demo.model.Poll;
import com.example.voter.demo.service.PollService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PollControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PollService pollService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PollController(pollService)).build();
    }

    @Test
    void testCreatePoll() throws Exception {
        Poll poll = new Poll("1", "New Poll", "127.0.0.1");
        Mockito.when(pollService.createPoll(anyString(), anyString())).thenReturn(poll);

        PollController.CreatePollRequest req = new PollController.CreatePollRequest();
        req.name = "New Poll";

        mockMvc.perform(post("/api/polls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("New Poll"))
                .andExpect(jsonPath("$.pmCode").exists());
    }

    @Test
    void testCreatePollNullName() throws Exception {
        Poll poll = new Poll("1", "Unnamed Poll", "127.0.0.1");
        Mockito.when(pollService.createPoll(anyString(), anyString())).thenReturn(poll);

        PollController.CreatePollRequest req = new PollController.CreatePollRequest();

        mockMvc.perform(post("/api/polls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Unnamed Poll"));
    }

    @Test
    void testListPolls() throws Exception {
        Poll poll1 = new Poll("1", "Poll 1", "ip");
        Mockito.when(pollService.listPolls()).thenReturn(Arrays.asList(poll1));

        mockMvc.perform(get("/api/polls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void testGetPollFound() throws Exception {
        Poll poll = new Poll("1", "Poll", "ip");
        Mockito.when(pollService.getPoll("1")).thenReturn(poll);

        mockMvc.perform(get("/api/polls/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void testGetPollNotFound() throws Exception {
        Mockito.when(pollService.getPoll("1")).thenReturn(null);

        mockMvc.perform(get("/api/polls/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testValidatePmCodeCorrect() throws Exception {
        Poll poll = new Poll("1", "Poll", "ip");
        String code = poll.getPmCode();
        Mockito.when(pollService.getPoll("1")).thenReturn(poll);

        mockMvc.perform(get("/api/polls/1/validate-pm").param("code", code))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testValidatePmCodeIncorrect() throws Exception {
        Poll poll = new Poll("1", "Poll", "ip");
        Mockito.when(pollService.getPoll("1")).thenReturn(poll);

        mockMvc.perform(get("/api/polls/1/validate-pm").param("code", "wrong_code"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testValidatePmCodePollNotFound() throws Exception {
        Mockito.when(pollService.getPoll("1")).thenReturn(null);

        mockMvc.perform(get("/api/polls/1/validate-pm").param("code", "any"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testStreamPollFound() throws Exception {
        Poll poll = new Poll("1", "Poll", "ip");
        SseEmitter emitter = new SseEmitter();
        Mockito.when(pollService.getPoll("1")).thenReturn(poll);
        Mockito.when(pollService.subscribe("1")).thenReturn(emitter);

        mockMvc.perform(get("/api/polls/1/stream")
                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk());
    }

    @Test
    void testStreamPollNotFound() throws Exception {
        Mockito.when(pollService.getPoll("1")).thenReturn(null);

        mockMvc.perform(get("/api/polls/1/stream")
                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isNotFound());
    }

    @Test
    void testVoteSuccess() throws Exception {
        Poll poll = new Poll("1", "Poll", "ip");
        Mockito.when(pollService.vote(eq("1"), eq("user1"), eq("yes"), anyString())).thenReturn(poll);

        PollController.VoteRequest req = new PollController.VoteRequest();
        req.username = "user1";
        req.vote = "yes";

        mockMvc.perform(post("/api/polls/1/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void testVoteMissingFields() throws Exception {
        PollController.VoteRequest req = new PollController.VoteRequest();
        // req.username and req.vote are null

        mockMvc.perform(post("/api/polls/1/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVotePollNotFound() throws Exception {
        Mockito.when(pollService.vote(eq("1"), eq("user1"), eq("yes"), anyString())).thenReturn(null);

        PollController.VoteRequest req = new PollController.VoteRequest();
        req.username = "user1";
        req.vote = "yes";

        mockMvc.perform(post("/api/polls/1/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeletePollsOlderThan() throws Exception {
        Mockito.doNothing().when(pollService).deletePollsOlderThan(anyLong());

        mockMvc.perform(delete("/api/polls").param("hours", "2"))
                .andExpect(status().isNoContent());
    }
}
