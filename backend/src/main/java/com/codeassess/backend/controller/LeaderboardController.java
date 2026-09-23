package com.codeassess.backend.controller;

import com.codeassess.backend.analytics.dto.LeaderboardEntry;
import com.codeassess.backend.analytics.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for leaderboard endpoints.
 * 
 * Security:
 * - ADMIN: Can view any leaderboard
 * - CANDIDATE: Can view leaderboards for published assessments they have access to
 */
@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    /**
     * Get leaderboard for an assessment
     * 
     * Returns candidates ranked by:
     * 1. Total score (DESC)
     * 2. Accepted submissions (DESC)
     * 3. Average execution time (ASC)
     * 4. Last submission timestamp (ASC)
     * 
     * @param assessmentId Assessment ID
     * @return Ranked list of candidates
     */
    @GetMapping("/assessments/{assessmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(
            @PathVariable Long assessmentId
    ) {
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(assessmentId);
        return ResponseEntity.ok(leaderboard);
    }
}
