package com.codeassess.backend.controller;

import com.codeassess.backend.analytics.dto.LeaderboardEntry;
import com.codeassess.backend.analytics.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaderboardController Tests")
class LeaderboardControllerTest {

    @Mock
    private LeaderboardService leaderboardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LeaderboardController controller = new LeaderboardController(leaderboardService);
        // Note: We use standaloneSetup to bypass full Spring Security filter chain for unit tests, 
        // but we can manually verify interactions. Full security integration is tested in E2E.
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("CANDIDATE can access leaderboard")
    void candidateCanAccessLeaderboard() throws Exception {
        when(leaderboardService.getLeaderboard(1L)).thenReturn(List.of(new LeaderboardEntry()));

        mockMvc.perform(get("/api/leaderboard/assessments/1")
                        .with(authenticated(candidateAuth("7"))))
                .andExpect(status().isOk());

        verify(leaderboardService).getLeaderboard(1L);
    }

    @Test
    @DisplayName("ADMIN can access leaderboard")
    void adminCanAccessLeaderboard() throws Exception {
        when(leaderboardService.getLeaderboard(1L)).thenReturn(List.of(new LeaderboardEntry()));

        mockMvc.perform(get("/api/leaderboard/assessments/1")
                        .with(authenticated(adminAuth("3"))))
                .andExpect(status().isOk());

        verify(leaderboardService).getLeaderboard(1L);
    }

    @Test
    @DisplayName("Service throws exception when assessment not found")
    void handlesAssessmentNotFound() throws Exception {
        when(leaderboardService.getLeaderboard(999L)).thenThrow(new IllegalArgumentException("Assessment not found"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> 
            mockMvc.perform(get("/api/leaderboard/assessments/999")
                        .with(authenticated(candidateAuth("7"))))
        ).hasCauseInstanceOf(IllegalArgumentException.class);

        verify(leaderboardService).getLeaderboard(999L);
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
