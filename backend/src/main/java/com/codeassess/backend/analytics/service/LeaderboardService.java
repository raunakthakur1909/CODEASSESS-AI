package com.codeassess.backend.analytics.service;

import com.codeassess.backend.analytics.dto.LeaderboardEntry;
import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.submission.Submission;
import com.codeassess.backend.submission.SubmissionStatus;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.model.User;
import com.codeassess.backend.repository.AssessmentRepository;
import com.codeassess.backend.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating leaderboards with deterministic ranking.
 * 
 * Ranking algorithm:
 * 1. Group submissions by candidate
 * 2. Calculate total score and accepted count per candidate
 * 3. Sort by: score DESC, accepted DESC, execution time ASC, timestamp ASC
 * 4. Assign rank positions
 */
@Service
public class LeaderboardService {

    private static final Logger logger = LoggerFactory.getLogger(LeaderboardService.class);

    private final AssessmentRepository assessmentRepository;
    private final SubmissionRepository submissionRepository;

    public LeaderboardService(
            AssessmentRepository assessmentRepository,
            SubmissionRepository submissionRepository
    ) {
        this.assessmentRepository = assessmentRepository;
        this.submissionRepository = submissionRepository;
    }

    /**
     * Generate leaderboard for an assessment with deterministic ranking
     * 
     * Ranking criteria (in order):
     * 1. Total score (DESC) - higher scores rank first
     * 2. Accepted submissions (DESC) - more accepted solutions rank first
     * 3. Average execution time (ASC) - faster execution ranks first
     * 4. Last submission timestamp (ASC) - earlier submission ranks first
     * 
     * This ensures deterministic ranking even with tied scores.
     */
    public List<LeaderboardEntry> getLeaderboard(Long assessmentId) {
        logger.debug("Generating leaderboard for assessment: {}", assessmentId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        // Get all completed submissions for this assessment
        List<Submission> completedSubmissions = submissionRepository
                .findCompletedSubmissionsByAssessmentForLeaderboard(assessmentId);

        if (completedSubmissions.isEmpty()) {
            logger.debug("No completed submissions found for assessment");
            return List.of();
        }

        // Group submissions by candidate and calculate their metrics
        Map<Long, CandidateLeaderboardData> candidateData = completedSubmissions.stream()
                .collect(Collectors.groupingBy(
                        submission -> submission.getCandidate().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                submissions -> new CandidateLeaderboardData(submissions)
                        )
                ));

        // Convert to leaderboard entries and sort with ranking
        List<LeaderboardEntry> leaderboard = candidateData.entrySet().stream()
                .map(entry -> createLeaderboardEntry(entry.getKey(), entry.getValue()))
                .sorted(this::compareLeaderboardEntries)
                .collect(Collectors.toList());

        // Assign ranks
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(i + 1);
        }

        logger.debug("Leaderboard generated with {} entries", leaderboard.size());
        return leaderboard;
    }

    /**
     * Deterministic comparison for leaderboard ranking
     * 
     * Order:
     * 1. Score DESC (higher is better)
     * 2. Accepted submissions DESC (more is better)
     * 3. Execution time ASC (less is better)
     * 4. Submission timestamp ASC (earlier is better)
     */
    private int compareLeaderboardEntries(LeaderboardEntry a, LeaderboardEntry b) {
        // 1. Compare by total score (DESC)
        int scoreComparison = Double.compare(b.getTotalScore(), a.getTotalScore());
        if (scoreComparison != 0) {
            return scoreComparison;
        }

        // 2. Compare by accepted submissions (DESC)
        int acceptedComparison = Integer.compare(b.getAcceptedCount(), a.getAcceptedCount());
        if (acceptedComparison != 0) {
            return acceptedComparison;
        }

        // 3. Compare by execution time (ASC - lower is better)
        int timeComparison = Double.compare(a.getAverageExecutionTimeMs(), b.getAverageExecutionTimeMs());
        if (timeComparison != 0) {
            return timeComparison;
        }

        // 4. Compare by submission timestamp (ASC - earlier is better)
        return a.getLastSubmissionAt().compareTo(b.getLastSubmissionAt());
    }

    /**
     * Create a leaderboard entry from candidate submission data
     */
    private LeaderboardEntry createLeaderboardEntry(Long candidateId, CandidateLeaderboardData data) {
        return new LeaderboardEntry(
                null, // Rank assigned later
                candidateId,
                data.getCandidateName(),
                data.getTotalScore(),
                data.getAcceptedCount(),
                data.getTotalProblems(),
                data.getAverageExecutionTime(),
                data.getLastSubmissionAt()
        );
    }

    /**
     * Helper class to aggregate candidate metrics for leaderboard
     */
    private static class CandidateLeaderboardData {
        private final String candidateName;
        private final Double totalScore;
        private final int acceptedCount;
        private final int totalProblems;
        private final Double averageExecutionTime;
        private final java.time.LocalDateTime lastSubmissionAt;

        public CandidateLeaderboardData(List<Submission> submissions) {
            if (submissions.isEmpty()) {
                throw new IllegalArgumentException("Cannot create leaderboard data from empty submissions");
            }

            this.candidateName = submissions.get(0).getCandidate().getName();
            this.totalScore = submissions.stream()
                    .mapToDouble(Submission::getScore)
                    .sum();
            this.acceptedCount = (int) submissions.stream()
                    .filter(s -> s.getScore() == 100) // Assuming 100 = accepted
                    .count();
            this.totalProblems = (int) submissions.stream()
                    .map(Submission::getProblem)
                    .distinct()
                    .count();
            
            // Average execution time of accepted submissions
            this.averageExecutionTime = submissions.stream()
                    .filter(s -> s.getExecutionTimeMs() != null && s.getScore() == 100)
                    .mapToDouble(Submission::getExecutionTimeMs)
                    .average()
                    .orElse(0.0);

            this.lastSubmissionAt = submissions.stream()
                    .map(Submission::getSubmittedAt)
                    .max(java.time.LocalDateTime::compareTo)
                    .orElse(null);
        }

        public String getCandidateName() { return candidateName; }
        public Double getTotalScore() { return totalScore; }
        public int getAcceptedCount() { return acceptedCount; }
        public int getTotalProblems() { return totalProblems; }
        public Double getAverageExecutionTime() { return averageExecutionTime; }
        public java.time.LocalDateTime getLastSubmissionAt() { return lastSubmissionAt; }
    }
}
