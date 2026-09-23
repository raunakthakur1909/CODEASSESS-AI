package com.codeassess.backend.service;

import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.assessment.AssessmentStatus;
import com.codeassess.backend.exception.BusinessException;
import com.codeassess.backend.exception.ForbiddenException;
import com.codeassess.backend.exception.ResourceNotFoundException;
import com.codeassess.backend.model.Role;
import com.codeassess.backend.model.User;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.repository.ProblemRepository;
import com.codeassess.backend.repository.SubmissionRepository;
import com.codeassess.backend.repository.UserRepository;
import com.codeassess.backend.submission.Submission;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.submission.SubmissionStatus;
import com.codeassess.backend.submission.dto.CreateSubmissionRequest;
import com.codeassess.backend.submission.dto.SubmissionResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing code submissions.
 * Enforces security rules: candidates only, published assessments only.
 */
@Service
@Transactional
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final com.codeassess.backend.repository.AssessmentAttemptRepository attemptRepository;
    private final CodeReviewService codeReviewService;
    private final Counter submissionCreatedCounter;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            ProblemRepository problemRepository,
            UserRepository userRepository,
            com.codeassess.backend.repository.AssessmentAttemptRepository attemptRepository,
            CodeReviewService codeReviewService,
            MeterRegistry meterRegistry
    ) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
        this.codeReviewService = codeReviewService;
        this.submissionCreatedCounter = meterRegistry.counter("codeassess.submissions.created");
    }

    /**
     * Create a new code submission.
     * Validates: candidate role, problem exists, assessment is published.
     * All metadata (status, score, etc.) controlled by backend.
     */
    public SubmissionResponse createSubmission(
            Long problemId,
            CreateSubmissionRequest request,
            Long candidateId
    ) {

        // Get and validate candidate
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidate not found"
                        )
                );

        // Only candidates can submit code
        if (candidate.getRole() != Role.CANDIDATE) {
            throw new ForbiddenException(
                    "Only candidates can submit code"
            );
        }

        // Get and validate problem
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Problem not found"
                        )
                );

        // Get associated assessment
        Assessment assessment = problem.getAssessment();

        if (assessment == null) {
            throw new BusinessException(
                    "Problem is not associated with an assessment"
            );
        }

        // Only published assessments can receive submissions
        if (assessment.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new BusinessException(
                    "Code can only be submitted to published assessments"
            );
        }

        // Validate source code
        String sourceCode = request.getSourceCode().trim();

        if (sourceCode.isEmpty()) {
            throw new BusinessException(
                    "Source code cannot be empty"
            );
        }

        if (assessment.getStatus() != com.codeassess.backend.assessment.AssessmentStatus.PUBLISHED) {
            throw new com.codeassess.backend.exception.BusinessException(
                    "Cannot submit to an unpublished assessment"
            );
        }

        // Check attempt expiry
        com.codeassess.backend.assessment.AssessmentAttempt attempt = attemptRepository.findByAssessmentIdAndCandidateId(assessment.getId(), candidate.getId())
                .orElseThrow(() -> new com.codeassess.backend.exception.BusinessException("You must start the assessment before submitting"));

        if (attempt.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new com.codeassess.backend.exception.BusinessException("Assessment time has expired");
        }

        // Create submission with server-controlled state
        Submission submission = new Submission();

        submission.setCandidate(candidate);
        submission.setProblem(problem);
        submission.setLanguage(request.getLanguage());
        submission.setSourceCode(sourceCode);

        // Initial state - controlled by backend
        submission.setStatus(SubmissionStatus.QUEUED);
        submission.setResult(SubmissionResult.PENDING);

        submission.setScore(0);
        submission.setPassedTests(0);
        submission.setTotalTests(0);

        Submission saved = submissionRepository.save(submission);

        submissionCreatedCounter.increment();

        return SubmissionResponse.fromEntity(saved);
    }

    /**
     * Get a single submission.
     * Candidates can only view their own submissions.
     */
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(
            Long submissionId,
            Long userId
    ) {

        Submission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Submission not found"
                                )
                        );

        validateSubmissionAccess(submission, userId);

        return SubmissionResponse.fromEntity(submission);
    }

    /**
     * Get all submissions by the current candidate.
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getMySubmissions(
            Long candidateId
    ) {

        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidate not found"
                        )
                );

        return submissionRepository
                .findByCandidateOrderBySubmittedAtDesc(candidate)
                .stream()
                .map(SubmissionResponse::fromEntity)
                .toList();
    }

    /**
     * Get all submissions by the current candidate for a specific problem.
     * Shows attempt history.
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getMyProblemSubmissions(
            Long problemId,
            Long candidateId
    ) {

        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidate not found"
                        )
                );

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Problem not found"
                        )
                );

        return submissionRepository
                .findByCandidateAndProblemOrderBySubmittedAtDesc(
                        candidate,
                        problem
                )
                .stream()
                .map(SubmissionResponse::fromEntity)
                .toList();
    }

    /**
     * Validate that the user has access to view this submission.
     * Candidates can only view their own submissions.
     */
    private void validateSubmissionAccess(
            Submission submission,
            Long userId
    ) {

        if (!submission.getCandidate().getId().equals(userId)) {
            throw new ForbiddenException(
                    "You do not have permission to view this submission"
            );
        }
    }

    /**
     * Update submission with execution results.
     * Called by the execution engine after code execution completes.
     * 
     * @param submissionId the submission to update
     * @param result final evaluation result
     * @param passedTests number of test cases passed
     * @param totalTests total number of test cases
     * @param score calculated score (0-100)
     * @param executionTimeMs execution time in milliseconds
     * @param memoryUsedKb memory used in kilobytes
     * @param compilerOutput compiler output (if any)
     * @param runtimeOutput runtime output (if any)
     */
    public void updateSubmissionResult(
            Long submissionId,
            SubmissionResult result,
            int passedTests,
            int totalTests,
            int score,
            Integer executionTimeMs,
            Integer memoryUsedKb,
            String compilerOutput,
            String runtimeOutput
    ) {

        Submission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Submission not found"
                                )
                        );

        submission.setStatus(SubmissionStatus.COMPLETED);
        submission.setResult(result);
        submission.setPassedTests(passedTests);
        submission.setTotalTests(totalTests);
        submission.setScore(score);
        submission.setExecutionTimeMs(executionTimeMs);
        submission.setMemoryUsedKb(memoryUsedKb);
        submission.setCompilerOutput(compilerOutput);
        submission.setRuntimeOutput(runtimeOutput);
        submission.markCompleted();

        submissionRepository.save(submission);

        // Automatically queue AI review for completed submission
        try {
            codeReviewService.queueReview(submissionId, submission.getCandidate().getId());
        } catch (Exception e) {
            // Don't fail submission update if review queueing fails
            // (review might already exist or AI might be disabled)
        }
    }

    /**
     * Mark submission as failed due to system error.
     * 
     * @param submissionId the submission to mark as failed
     * @param errorMessage error message describing the failure
     */
    public void markSubmissionFailed(
            Long submissionId,
            String errorMessage
    ) {

        Submission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Submission not found"
                                )
                        );

        submission.setStatus(SubmissionStatus.FAILED);
        submission.setResult(SubmissionResult.SYSTEM_ERROR);
        submission.setRuntimeOutput(errorMessage);
        submission.markCompleted();

        submissionRepository.save(submission);
    }

    /**
     * Update submission status (e.g., from QUEUED to RUNNING).
     * 
     * @param submissionId the submission to update
     * @param status new status
     */
    public void updateSubmissionStatus(
            Long submissionId,
            SubmissionStatus status
    ) {

        Submission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Submission not found"
                                )
                        );

        submission.setStatus(status);
        submissionRepository.save(submission);
    }

    /**
     * Store Judge0 execution token for tracking.
     * 
     * @param submissionId the submission
     * @param executionToken Judge0 submission token
     */
    public void storeExecutionToken(
            Long submissionId,
            String executionToken
    ) {

        Submission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Submission not found"
                                )
                        );

        submission.setExecutionToken(executionToken);
        submissionRepository.save(submission);
    }
}
