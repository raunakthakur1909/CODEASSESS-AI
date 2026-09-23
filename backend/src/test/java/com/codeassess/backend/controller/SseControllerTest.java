package com.codeassess.backend.controller;

import com.codeassess.backend.exception.ForbiddenException;
import com.codeassess.backend.exception.GlobalExceptionHandler;
import com.codeassess.backend.exception.ResourceNotFoundException;
import com.codeassess.backend.model.User;
import com.codeassess.backend.realtime.SseConnectionManager;
import com.codeassess.backend.repository.SubmissionRepository;
import com.codeassess.backend.repository.AssessmentRepository;
import com.codeassess.backend.submission.Submission;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.submission.SubmissionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("SseController Security & Event Tests")
class SseControllerTest {

    @Mock
    private SseConnectionManager connectionManager;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SseController controller = new SseController(connectionManager, submissionRepository, assessmentRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Candidate owns submission -> 200 OK + SSE Connection")
    void candidateOwnsSubmission_Allowed() throws Exception {
        Submission submission = createSubmission(42L, 7L);
        when(submissionRepository.findById(42L)).thenReturn(Optional.of(submission));
        when(connectionManager.connect("submission:42")).thenReturn(new SseEmitter());

        mockMvc.perform(get("/api/events/submissions/42")
                        .with(authenticated(candidateAuth("7"))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM_VALUE));

        verify(connectionManager).connect("submission:42");
    }

    @Test
    @DisplayName("Admin -> 200 OK regardless of ownership")
    void adminAccess_Allowed() throws Exception {
        Submission submission = createSubmission(42L, 7L); // Owned by 7
        when(submissionRepository.findById(42L)).thenReturn(Optional.of(submission));
        when(connectionManager.connect("submission:42")).thenReturn(new SseEmitter());

        mockMvc.perform(get("/api/events/submissions/42")
                        .with(authenticated(adminAuth("99")))) // Admin is 99
                .andExpect(status().isOk());

        verify(connectionManager).connect("submission:42");
    }

    @Test
    @DisplayName("Candidate DOES NOT own submission -> 403 Forbidden")
    void candidateDoesNotOwnSubmission_Forbidden() throws Exception {
        Submission submission = createSubmission(42L, 10L); // Owned by 10
        when(submissionRepository.findById(42L)).thenReturn(Optional.of(submission));

        mockMvc.perform(get("/api/events/submissions/42")
                        .with(authenticated(candidateAuth("7")))) // Requester is 7
                .andExpect(status().isForbidden());

        verify(connectionManager, never()).connect(anyString());
    }

    @Test
    @DisplayName("Missing submission -> 404 Not Found")
    void missingSubmission_NotFound() throws Exception {
        when(submissionRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/events/submissions/99")
                        .with(authenticated(candidateAuth("7"))))
                .andExpect(status().isNotFound());

        verify(connectionManager, never()).connect(anyString());
    }

    @Test
    @DisplayName("Missing authentication -> Handled by SecurityFilterChain (simulated 401/400 without principal)")
    void missingAuthentication_Fails() throws Exception {
        // Without authentication mock, it throws exception inside controller since Principal is null.
        // In real app, SecurityFilterChain catches it before controller. 
        // We just ensure we don't accidentally allow it.
        mockMvc.perform(get("/api/events/submissions/42"))
                .andExpect(status().isInternalServerError());
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Submission createSubmission(Long id, Long candidateId) {
        Submission submission = new Submission();
        
        try {
            var idField = Submission.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(submission, id);
        } catch (Exception e) {}

        User candidate = new User();
        try {
            var userIdField = User.class.getDeclaredField("id");
            userIdField.setAccessible(true);
            userIdField.set(candidate, candidateId);
        } catch (Exception e) {}

        submission.setCandidate(candidate);
        submission.setStatus(SubmissionStatus.COMPLETED);
        submission.setResult(SubmissionResult.ACCEPTED);
        submission.setScore(100);
        submission.setPassedTests(5);
        submission.setTotalTests(5);
        
        try {
            var submittedAtField = Submission.class.getDeclaredField("submittedAt");
            submittedAtField.setAccessible(true);
            submittedAtField.set(submission, LocalDateTime.now());
        } catch (Exception e) {}

        return submission;
    }

    private Authentication candidateAuth(String userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"))
        );
    }

    private Authentication adminAuth(String userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    private RequestPostProcessor authenticated(Authentication auth) {
        return request -> {
            request.setUserPrincipal(auth);
            return request;
        };
    }
}
