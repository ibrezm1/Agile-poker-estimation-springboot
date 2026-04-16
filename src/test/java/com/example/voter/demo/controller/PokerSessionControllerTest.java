package com.example.voter.demo.controller;

import com.example.voter.demo.model.PokerSession;
import com.example.voter.demo.model.Topic;
import com.example.voter.demo.service.PokerSessionService;
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

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PokerSessionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PokerSessionService sessionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PokerSessionController(sessionService)).build();
    }

    @Test
    void testCreateSession() throws Exception {
        PokerSession session = new PokerSession("New Session", "127.0.0.1");
        Mockito.when(sessionService.createSession(anyString(), anyString())).thenReturn(session);

        PokerSessionController.CreateSessionRequest req = new PokerSessionController.CreateSessionRequest();
        req.name = "New Session";

        mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Session"));
    }

    @Test
    void testGetSessionFound() throws Exception {
        PokerSession session = new PokerSession("Session", "ip");
        Mockito.when(sessionService.getSession("1")).thenReturn(session);

        mockMvc.perform(get("/api/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Session"));
    }

    @Test
    void testVoteSuccess() throws Exception {
        PokerSession session = new PokerSession("Session", "ip");
        Mockito.when(sessionService.getSession("1")).thenReturn(session);

        PokerSessionController.VoteRequest req = new PokerSessionController.VoteRequest();
        req.username = "user1";
        req.vote = "5";

        mockMvc.perform(post("/api/sessions/1/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testRevealSuccess() throws Exception {
        PokerSession session = new PokerSession("Session", "ip");
        String code = session.getPmCode();
        Mockito.when(sessionService.getSession("1")).thenReturn(session);

        mockMvc.perform(post("/api/sessions/1/reveal").param("code", code))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateTopicSuccess() throws Exception {
        PokerSession session = new PokerSession("Session", "ip");
        String code = session.getPmCode();
        Mockito.when(sessionService.getSession("1")).thenReturn(session);

        PokerSessionController.CreateTopicRequest req = new PokerSessionController.CreateTopicRequest();
        req.name = "Next Topic";

        mockMvc.perform(post("/api/sessions/1/topics")
                .param("code", code)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
