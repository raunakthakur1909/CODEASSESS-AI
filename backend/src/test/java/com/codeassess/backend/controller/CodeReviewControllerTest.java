package com.codeassess.backend.controller;

import com.codeassess.backend.ai.dto.CodeReviewResponse;
import com.codeassess.backend.service.CodeReviewService;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CodeReviewControllerTest {

    @Mock
    private CodeReviewService codeReviewService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CodeReviewController controller = new CodeReviewController(codeReviewService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldGenerateReviewUsingAuthenticatedUserId() throws Exception {
        CodeReviewResponse response = new CodeReviewResponse();
        when(codeReviewService.queueReview(42L, 7L)).thenReturn(response);

        mockMvc.perform(post("/api/submissions/42/ai-review")
                        .with(authenticated(candidateAuthentication("7")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

            verify(codeReviewService).queueReview(42L, 7L);
    }

    @Test
    void shouldReturnCandidateReviewHistory() throws Exception {
        when(codeReviewService.getMyReviews(7L)).thenReturn(List.of(new CodeReviewResponse()));

        mockMvc.perform(get("/api/ai-reviews/my")
                        .with(authenticated(candidateAuthentication("7"))))
                .andExpect(status().isOk());

        verify(codeReviewService).getMyReviews(7L);
    }

    @Test
    void shouldUseCandidateAuthorizationForReviewById() throws Exception {
        when(codeReviewService.getReviewById(17L, 7L)).thenReturn(new CodeReviewResponse());

        mockMvc.perform(get("/api/ai-reviews/17")
                        .with(authenticated(candidateAuthentication("7"))))
                .andExpect(status().isOk());

        verify(codeReviewService).getReviewById(17L, 7L);
    }

    @Test
    void shouldUseAdminReviewLookupForReviewById() throws Exception {
        when(codeReviewService.getReviewByIdForAdmin(17L)).thenReturn(new CodeReviewResponse());

        mockMvc.perform(get("/api/ai-reviews/17")
                        .with(authenticated(adminAuthentication("3"))))
                .andExpect(status().isOk());

        verify(codeReviewService).getReviewByIdForAdmin(17L);
    }

    private Authentication candidateAuthentication(String userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"))
        );
    }

    private Authentication adminAuthentication(String userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    private RequestPostProcessor authenticated(Authentication authentication) {
        return request -> {
            request.setUserPrincipal(authentication);
            return request;
        };
    }
}
