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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
                .andExpect(jsonPath("$.name").value("New Session"))
                .andExpect(jsonPath("$.creatorIp").value("127.0.0.1"))
                .andExpect(jsonPath("$.pmCode").exists());
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

    @Test
    void testVoteOnSpecificTopicSuccess() throws Exception {
        PokerSession session = new PokerSession("Session", "ip");
        Topic topic = session.createNewTopic("Topic 1");
        Mockito.when(sessionService.getSession("1")).thenReturn(session);

        PokerSessionController.VoteRequest req = new PokerSessionController.VoteRequest();
        req.username = "user1";
        req.vote = "8";

        mockMvc.perform(post("/api/sessions/1/topics/" + topic.getId() + "/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testRevealSpecificTopicSuccess() throws Exception {
        PokerSession session = new PokerSession("Session", "ip");
        Topic topic = session.createNewTopic("Topic 1");
        String code = session.getPmCode();
        Mockito.when(sessionService.getSession("1")).thenReturn(session);

        mockMvc.perform(post("/api/sessions/1/topics/" + topic.getId() + "/reveal")
                .param("code", code))
                .andExpect(status().isOk());
        
        assertTrue(topic.isRevealed());
    }

    @Test
    void testListSessions() throws Exception {
        PokerSession session = new PokerSession("S1", "ip");
        Mockito.when(sessionService.listSessions()).thenReturn(Collections.singletonList(session));

        mockMvc.perform(get("/api/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("S1"));
    }

    @Test
    void testGetSessionNotFound() throws Exception {
        Mockito.when(sessionService.getSession("none")).thenReturn(null);

        mockMvc.perform(get("/api/sessions/none"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteSessions() throws Exception {
        mockMvc.perform(delete("/api/sessions").param("hours", "5"))
                .andExpect(status().isNoContent());
        Mockito.verify(sessionService).deleteSessionsOlderThan(5);
    }

    @Test
    void testValidatePmCode() throws Exception {
        PokerSession session = new PokerSession("S1", "ip");
        String code = session.getPmCode();
        Mockito.when(sessionService.getSession("1")).thenReturn(session);

        // Success
        mockMvc.perform(get("/api/sessions/1/validate-pm").param("code", code))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Failure
        mockMvc.perform(get("/api/sessions/1/validate-pm").param("code", "wrong"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
        
        // Session not found
        Mockito.when(sessionService.getSession("2")).thenReturn(null);
        mockMvc.perform(get("/api/sessions/2/validate-pm").param("code", code))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testStreamSessionNotFound() throws Exception {
        Mockito.when(sessionService.getSession("1")).thenReturn(null);
        mockMvc.perform(get("/api/sessions/1/stream"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testVoteBadRequest() throws Exception {
        PokerSessionController.VoteRequest req = new PokerSessionController.VoteRequest();
        // Missing username or vote
        mockMvc.perform(post("/api/sessions/1/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVoteSessionNotFound() throws Exception {
        Mockito.when(sessionService.getSession("1")).thenReturn(null);
        PokerSessionController.VoteRequest req = new PokerSessionController.VoteRequest();
        req.username = "u";
        req.vote = "v";

        mockMvc.perform(post("/api/sessions/1/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testVoteTopicRevealedConflict() throws Exception {
        PokerSession session = new PokerSession("S1", "ip");
        Topic topic = session.createNewTopic("T1");
        topic.setRevealed(true);
        Mockito.when(sessionService.getSession("1")).thenReturn(session);

        PokerSessionController.VoteRequest req = new PokerSessionController.VoteRequest();
        req.username = "u";
        req.vote = "v";

        mockMvc.perform(post("/api/sessions/1/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void testVoteOnSpecificTopicNotFound() throws Exception {
        PokerSession session = new PokerSession("S1", "ip");
        Mockito.when(sessionService.getSession("1")).thenReturn(session);

        PokerSessionController.VoteRequest req = new PokerSessionController.VoteRequest();
        req.username = "u";
        req.vote = "v";

        mockMvc.perform(post("/api/sessions/1/topics/missing/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRevealTopicForbidden() throws Exception {
        PokerSession session = new PokerSession("S1", "ip");
        Mockito.when(sessionService.getSession("1")).thenReturn(session);

        mockMvc.perform(post("/api/sessions/1/reveal").param("code", "wrong"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRevealTopicSessionNotFound() throws Exception {
        Mockito.when(sessionService.getSession("1")).thenReturn(null);
        mockMvc.perform(post("/api/sessions/1/reveal").param("code", "123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTopicForbidden() throws Exception {
        PokerSession session = new PokerSession("S1", "ip");
        Mockito.when(sessionService.getSession("1")).thenReturn(session);

        PokerSessionController.CreateTopicRequest req = new PokerSessionController.CreateTopicRequest();
        req.name = "T1";

        mockMvc.perform(post("/api/sessions/1/topics")
                .param("code", "wrong")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
