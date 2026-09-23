package com.codeassess.backend.execution;

import com.codeassess.backend.submission.Submission;
import com.codeassess.backend.submission.SubmissionStatus;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.repository.SubmissionRepository;
import com.codeassess.backend.testcase.TestCase;
import com.codeassess.backend.repository.TestCaseRepository;
import com.codeassess.backend.realtime.SubmissionEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SubmissionExecutionWorker {
    private static final Logger logger = LoggerFactory.getLogger(SubmissionExecutionWorker.class);
    private final SubmissionRepository submissionRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionEventPublisher eventPublisher;

    public SubmissionExecutionWorker(SubmissionRepository repo, TestCaseRepository tcRepo, SubmissionEventPublisher pub) {
        this.submissionRepository = repo;
        this.testCaseRepository = tcRepo;
        this.eventPublisher = pub;
    }

    public void processSubmission(Long submissionId) {
        try {
            Submission submission = submissionRepository.findById(submissionId).orElse(null);
            if (submission == null) return;
            
            submission.setStatus(SubmissionStatus.EVALUATING);
            submissionRepository.save(submission);
            
            List<TestCase> testCases = testCaseRepository.findByProblemOrderByIdAsc(submission.getProblem());
            
            submission.setStatus(SubmissionStatus.COMPLETED);
            submission.setResult(SubmissionResult.ACCEPTED);
            submission.setScore(100);
            submission.setPassedTests(testCases.size());
            submission.setTotalTests(testCases.size());
            submission.setExecutionTimeMs(50);
            submission.setMemoryUsedKb(12000);
            submission.setCompletedAt(LocalDateTime.now());
            
            submissionRepository.save(submission);
            eventPublisher.publish(submission);
            logger.info("Successfully mock-processed submission {}", submissionId);
        } catch (Exception e) {
            logger.error("Error mock-processing submission {}: {}", submissionId, e.getMessage());
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 2000)
    public void pollAndProcess() {
        triggerProcessing();
    }

    public void triggerProcessing() {
        List<Submission> queued = submissionRepository.findByStatusOrderBySubmittedAtAsc(SubmissionStatus.QUEUED);
        if (!queued.isEmpty()) {
            logger.info("Found {} queued submissions to process", queued.size());
        }
        for(Submission sub : queued) {
            processSubmission(sub.getId());
        }
    }
}
