package com.codeassess.backend.controller;

import com.codeassess.backend.analytics.dto.CandidateAnalyticsResponse;
import com.codeassess.backend.analytics.service.CandidateAnalyticsService;
import com.codeassess.backend.analytics.service.AssessmentAnalyticsService;
import com.codeassess.backend.analytics.service.ProblemAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Milestone 9.7 — AnalyticsController ownership boundary tests.
 *
 * Covers all three access-control cases for the candidate analytics endpoints:
 *
 *   ADMIN → can view any candidate's analytics
 *   CANDIDATE + own ID → allowed
 *   CANDIDATE + different ID → 403 Forbidden
 *   /me endpoint → resolves ID from JWT, no ID param required
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsController — candidate ownership boundary")
class AnalyticsControllerTest {

    @Mock
    private AssessmentAnalyticsService assessmentAnalyticsService;

    @Mock
    private CandidateAnalyticsService candidateAnalyticsService;

    @Mock
    private ProblemAnalyticsService problemAnalyticsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AnalyticsController controller = new AnalyticsController(
                assessmentAnalyticsService,
                candidateAnalyticsService,
                problemAnalyticsService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ── /api/analytics/candidates/{candidateId} ───────────────────────────

    @Nested
    @DisplayName("GET /analytics/candidates/{candidateId}")
    class CandidateAnalyticsByIdTests {

        @Test
        @DisplayName("ADMIN can view any candidate's analytics")
        void adminCanViewAnyCandidateAnalytics() throws Exception {
            CandidateAnalyticsResponse response = candidateResponse(99L);
            when(candidateAnalyticsService.getCandidateAnalytics(99L)).thenReturn(response);

            mockMvc.perform(get("/api/analytics/candidates/99")
                            .with(authenticated(adminAuth("3"))))
                    .andExpect(status().isOk());

            verify(candidateAnalyticsService).getCandidateAnalytics(99L);
        }

        @Test
        @DisplayName("CANDIDATE can view their own analytics")
        void candidateCanViewOwnAnalytics() throws Exception {
            CandidateAnalyticsResponse response = candidateResponse(7L);
            when(candidateAnalyticsService.getCandidateAnalytics(7L)).thenReturn(response);

            mockMvc.perform(get("/api/analytics/candidates/7")
                            .with(authenticated(candidateAuth("7"))))
                    .andExpect(status().isOk());

            verify(candidateAnalyticsService).getCandidateAnalytics(7L);
        }

        @Test
        @DisplayName("CANDIDATE receives 403 when requesting another candidate's analytics")
        void candidateCannotViewOtherCandidateAnalytics() throws Exception {
            mockMvc.perform(get("/api/analytics/candidates/99")
                            .with(authenticated(candidateAuth("7"))))
                    .andExpect(status().isForbidden());

            // Service must NOT be called — the controller short-circuits at the ownership check
            verify(candidateAnalyticsService, never()).getCandidateAnalytics(any());
        }

        @Test
        @DisplayName("Service is never called when access is denied")
        void serviceNotCalledOnForbidden() throws Exception {
            mockMvc.perform(get("/api/analytics/candidates/55")
                            .with(authenticated(candidateAuth("12"))))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(candidateAnalyticsService);
        }
    }

    // ── /api/analytics/me ─────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /analytics/me")
    class MyAnalyticsTests {

        @Test
        @DisplayName("/me resolves candidate ID from JWT — CANDIDATE can call it")
        void candidateCanCallMeEndpoint() throws Exception {
            CandidateAnalyticsResponse response = candidateResponse(7L);
            when(candidateAnalyticsService.getCandidateAnalytics(7L)).thenReturn(response);

            mockMvc.perform(get("/api/analytics/me")
                            .with(authenticated(candidateAuth("7"))))
                    .andExpect(status().isOk());

            // ID resolved from JWT principal — not from any request param
            verify(candidateAnalyticsService).getCandidateAnalytics(7L);
        }

        @Test
        @DisplayName("ADMIN can also call /me (resolved to admin's own ID)")
        void adminCanCallMeEndpoint() throws Exception {
            CandidateAnalyticsResponse response = candidateResponse(3L);
            when(candidateAnalyticsService.getCandidateAnalytics(3L)).thenReturn(response);

            mockMvc.perform(get("/api/analytics/me")
                            .with(authenticated(adminAuth("3"))))
                    .andExpect(status().isOk());

            verify(candidateAnalyticsService).getCandidateAnalytics(3L);
        }

        @Test
        @DisplayName("/me uses JWT ID, never an arbitrary client-supplied value")
        void meEndpointUsesJwtIdOnly() throws Exception {
            // Even if the URL had a different ID, /me only uses the JWT
            CandidateAnalyticsResponse response = candidateResponse(7L);
            when(candidateAnalyticsService.getCandidateAnalytics(7L)).thenReturn(response);

            mockMvc.perform(get("/api/analytics/me")
                            .with(authenticated(candidateAuth("7"))))
                    .andExpect(status().isOk());

            // CRITICAL: the service is called with JWT ID (7), never a client-supplied override
            verify(candidateAnalyticsService).getCandidateAnalytics(7L);
            verify(candidateAnalyticsService, never()).getCandidateAnalytics(99L);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private CandidateAnalyticsResponse candidateResponse(Long id) {
        CandidateAnalyticsResponse response = new CandidateAnalyticsResponse();
        response.setCandidateId(id);
        response.setCandidateName("Test Candidate");
        response.setCandidateEmail("test@example.com");
        response.setAssessmentsAttempted(2);
        response.setTotalSubmissions(10);
        response.setAcceptedCount(7);
        response.setWrongAnswerCount(3);
        response.setAverageScore(75.0);
        response.setBestScore(95.0);
        response.setAcceptanceRate(70.0);
        response.setAverageExecutionTimeMs(210.5);
        response.setAverageAiReviewScore(80.0);
        return response;
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
