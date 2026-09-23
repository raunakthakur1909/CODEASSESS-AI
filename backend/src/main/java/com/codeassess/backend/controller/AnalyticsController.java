package com.codeassess.backend.controller;

import com.codeassess.backend.analytics.dto.AssessmentAnalyticsResponse;
import com.codeassess.backend.analytics.dto.CandidateAnalyticsResponse;
import com.codeassess.backend.analytics.dto.ProblemAnalyticsResponse;
import com.codeassess.backend.analytics.service.AssessmentAnalyticsService;
import com.codeassess.backend.analytics.service.CandidateAnalyticsService;
import com.codeassess.backend.analytics.service.ProblemAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for analytics endpoints.
 * 
 * Security:
 * - ADMIN: Can view all analytics
 * - CANDIDATE: Can view only their own analytics
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AssessmentAnalyticsService assessmentAnalyticsService;
    private final CandidateAnalyticsService candidateAnalyticsService;
    private final ProblemAnalyticsService problemAnalyticsService;

    public AnalyticsController(
            AssessmentAnalyticsService assessmentAnalyticsService,
            CandidateAnalyticsService candidateAnalyticsService,
            ProblemAnalyticsService problemAnalyticsService
    ) {
        this.assessmentAnalyticsService = assessmentAnalyticsService;
        this.candidateAnalyticsService = candidateAnalyticsService;
        this.problemAnalyticsService = problemAnalyticsService;
    }

    /**
     * Get analytics for an assessment (ADMIN only)
     * 
     * @param assessmentId Assessment ID
     * @return Assessment analytics including submission metrics and problem breakdown
     */
    @GetMapping("/assessments/{assessmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssessmentAnalyticsResponse> getAssessmentAnalytics(
            @PathVariable Long assessmentId
    ) {
        AssessmentAnalyticsResponse analytics = assessmentAnalyticsService
                .getAssessmentAnalytics(assessmentId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get analytics for a candidate (ADMIN or own data)
     * 
     * @param candidateId Candidate user ID
     * @param authentication Current user authentication
     * @return Candidate performance analytics
     */
    @GetMapping("/candidates/{candidateId}")
    public ResponseEntity<CandidateAnalyticsResponse> getCandidateAnalytics(
            @PathVariable Long candidateId,
            Authentication authentication
    ) {
        Long currentUserId = Long.parseLong(authentication.getName());
        
        // Check authorization: ADMIN can view any, CANDIDATE can view only their own
        if (!isAdmin(authentication) && !currentUserId.equals(candidateId)) {
            return ResponseEntity.status(403).build();
        }

        CandidateAnalyticsResponse analytics = candidateAnalyticsService
                .getCandidateAnalytics(candidateId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get analytics for current user (convenience endpoint)
     * 
     * @param authentication Current user authentication
     * @return Current user's analytics
     */
    @GetMapping("/me")
    public ResponseEntity<CandidateAnalyticsResponse> getMyAnalytics(
            Authentication authentication
    ) {
        Long currentUserId = Long.parseLong(authentication.getName());
        CandidateAnalyticsResponse analytics = candidateAnalyticsService
                .getCandidateAnalytics(currentUserId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get analytics for a problem (ADMIN only)
     * 
     * @param problemId Problem ID
     * @return Problem statistics including acceptance rate and performance level
     */
    @GetMapping("/problems/{problemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemAnalyticsResponse> getProblemAnalytics(
            @PathVariable Long problemId
    ) {
        ProblemAnalyticsResponse analytics = problemAnalyticsService
                .getProblemAnalytics(problemId);
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
