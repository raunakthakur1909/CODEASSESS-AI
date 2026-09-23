package com.codeassess.backend.controller;

import com.codeassess.backend.analytics.dto.AIAnalytics;
import com.codeassess.backend.analytics.service.AIAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for AI analytics endpoints.
 * 
 * Security:
 * - ADMIN: Can view all AI analytics
 * - CANDIDATE: Can view only their own AI analytics
 */
@RestController
@RequestMapping("/api/analytics/ai")
public class AIAnalyticsController {

    private final AIAnalyticsService aiAnalyticsService;

    public AIAnalyticsController(AIAnalyticsService aiAnalyticsService) {
        this.aiAnalyticsService = aiAnalyticsService;
    }

    /**
     * Get AI analytics for an assessment (ADMIN only)
     * 
     * @param assessmentId Assessment ID
     * @return AI review statistics including scores and issue counts
     */
    @GetMapping("/assessments/{assessmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AIAnalytics> getAssessmentAIAnalytics(
            @PathVariable Long assessmentId
    ) {
        AIAnalytics analytics = aiAnalyticsService.getAssessmentAIAnalytics(assessmentId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get AI analytics for a candidate (ADMIN or own data)
     * 
     * @param candidateId Candidate user ID
     * @param authentication Current user authentication
     * @return Candidate's AI review statistics
     */
    @GetMapping("/candidates/{candidateId}")
    public ResponseEntity<AIAnalytics> getCandidateAIAnalytics(
            @PathVariable Long candidateId,
            Authentication authentication
    ) {
        Long currentUserId = Long.parseLong(authentication.getName());
        
        // Check authorization: ADMIN can view any, CANDIDATE can view only their own
        if (!isAdmin(authentication) && !currentUserId.equals(candidateId)) {
            return ResponseEntity.status(403).build();
        }

        AIAnalytics analytics = aiAnalyticsService.getCandidateAIAnalytics(candidateId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get AI analytics for current user (convenience endpoint)
     * 
     * @param authentication Current user authentication
     * @return Current user's AI analytics
     */
    @GetMapping("/me")
    public ResponseEntity<AIAnalytics> getMyAIAnalytics(
            Authentication authentication
    ) {
        Long currentUserId = Long.parseLong(authentication.getName());
        AIAnalytics analytics = aiAnalyticsService.getCandidateAIAnalytics(currentUserId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get AI analytics for a problem (ADMIN only)
     * 
     * @param problemId Problem ID
     * @return Problem's AI review statistics
     */
    @GetMapping("/problems/{problemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AIAnalytics> getProblemAIAnalytics(
            @PathVariable Long problemId
    ) {
        AIAnalytics analytics = aiAnalyticsService.getProblemAIAnalytics(problemId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Check if current user has ADMIN role
     */
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
