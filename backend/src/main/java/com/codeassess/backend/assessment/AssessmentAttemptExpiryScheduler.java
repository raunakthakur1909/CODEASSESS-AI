package com.codeassess.backend.assessment;

import com.codeassess.backend.repository.AssessmentAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AssessmentAttemptExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentAttemptExpiryScheduler.class);

    private final AssessmentAttemptRepository attemptRepository;

    public AssessmentAttemptExpiryScheduler(AssessmentAttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    /**
     * Automatically mark IN_PROGRESS attempts as EXPIRED when their expiry time passes.
     * Runs every 30 seconds to ensure attempts are marked expired promptly.
     */
    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    @Transactional
    public void checkAttemptExpirations() {
        LocalDateTime now = LocalDateTime.now();

        // Find all IN_PROGRESS attempts that have expired
        List<AssessmentAttempt> expiredAttempts = attemptRepository
                .findByStatusAndExpiresAtBefore(AssessmentAttemptStatus.IN_PROGRESS, now);

        if (!expiredAttempts.isEmpty()) {
            logger.info("Found {} expired assessment attempts to mark as EXPIRED", expiredAttempts.size());

            for (AssessmentAttempt attempt : expiredAttempts) {
                attempt.setStatus(AssessmentAttemptStatus.EXPIRED);
                attemptRepository.save(attempt);
                logger.info("Marked assessment attempt {} as EXPIRED (Assessment ID: {}, Candidate ID: {})",
                        attempt.getId(),
                        attempt.getAssessment().getId(),
                        attempt.getCandidate().getId());
            }
        }
    }

    /**
     * Clean up very old EXPIRED attempts (older than 90 days) to prevent database bloat.
     * Runs once per day at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * *") // Run daily at 3:00 AM
    @Transactional
    public void cleanupOldExpiredAttempts() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);

        List<AssessmentAttempt> oldExpiredAttempts = attemptRepository
                .findByStatusAndExpiresAtBefore(AssessmentAttemptStatus.EXPIRED, cutoffDate);

        if (!oldExpiredAttempts.isEmpty()) {
            logger.info("Cleaning up {} old expired attempts (older than 90 days)", oldExpiredAttempts.size());
            attemptRepository.deleteAll(oldExpiredAttempts);
        }
    }
}
