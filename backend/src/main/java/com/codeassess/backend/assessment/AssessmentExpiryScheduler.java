package com.codeassess.backend.assessment;

import com.codeassess.backend.realtime.AssessmentEventPublisher;
import com.codeassess.backend.realtime.AssessmentEventType;
import com.codeassess.backend.repository.AssessmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class AssessmentExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentExpiryScheduler.class);

    private final AssessmentRepository assessmentRepository;
    private final AssessmentEventPublisher eventPublisher;

    public AssessmentExpiryScheduler(AssessmentRepository assessmentRepository, AssessmentEventPublisher eventPublisher) {
        this.assessmentRepository = assessmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedRate = 5000) // Run every 5 seconds
    @Transactional
    public void checkAssessmentExpirations() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        // Find all PUBLISHED assessments
        List<Assessment> activeAssessments = assessmentRepository.findByStatus(AssessmentStatus.PUBLISHED);

        for (Assessment assessment : activeAssessments) {
            if (assessment.getStartedAt() == null) {
                continue;
            }

            ZonedDateTime expiresAt = assessment.getStartedAt().atZone(ZoneOffset.UTC).plusMinutes(assessment.getDurationMinutes());

            if (now.isAfter(expiresAt) || now.isEqual(expiresAt)) {
                // Assessment has expired
                logger.info("Assessment {} has expired", assessment.getId());
                assessment.setStatus(AssessmentStatus.CLOSED); 
                assessmentRepository.save(assessment);
                
                eventPublisher.publish(assessment, AssessmentEventType.ASSESSMENT_EXPIRED);
            } else if (now.plusMinutes(5).isAfter(expiresAt) || now.plusMinutes(5).isEqual(expiresAt)) {
                // Assessment is expiring within 5 minutes
                // We only want to publish this event; we do NOT change the database status.
                eventPublisher.publish(assessment, AssessmentEventType.ASSESSMENT_EXPIRING);
            }
        }
    }
}
