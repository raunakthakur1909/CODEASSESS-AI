package com.codeassess.backend.service;

import com.codeassess.backend.ai.AIPromptBuilder;
import com.codeassess.backend.ai.AIProvider;
import com.codeassess.backend.ai.AIReviewParser;
import com.codeassess.backend.ai.CodeReview;
import com.codeassess.backend.ai.CodeReviewIssue;
import com.codeassess.backend.ai.ReviewCategory;
import com.codeassess.backend.ai.ReviewSeverity;
import com.codeassess.backend.ai.ReviewStatus;
import com.codeassess.backend.ai.dto.AIReviewIssuePayload;
import com.codeassess.backend.ai.dto.AIReviewPayload;
import com.codeassess.backend.ai.dto.CodeReviewResponse;
import com.codeassess.backend.config.AIProperties;
import com.codeassess.backend.exception.ConflictException;
import com.codeassess.backend.exception.ForbiddenException;
import com.codeassess.backend.exception.ResourceNotFoundException;
import com.codeassess.backend.repository.CodeReviewRepository;
import com.codeassess.backend.repository.SubmissionRepository;
import com.codeassess.backend.repository.UserRepository;
import com.codeassess.backend.submission.Submission;
import com.codeassess.backend.submission.SubmissionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CodeReviewService {

    private final SubmissionRepository submissionRepository;
    private final CodeReviewRepository codeReviewRepository;
    private final UserRepository userRepository;
    private final AIReviewParser aiReviewParser;
    private final AIProvider aiProvider;
    private final AIPromptBuilder aiPromptBuilder;
    private final AIProperties aiProperties;

    public CodeReviewService(
            SubmissionRepository submissionRepository,
            CodeReviewRepository codeReviewRepository,
            UserRepository userRepository,
            AIReviewParser aiReviewParser,
            AIProvider aiProvider,
            AIPromptBuilder aiPromptBuilder,
            AIProperties aiProperties
    ) {
        this.submissionRepository = submissionRepository;
        this.codeReviewRepository = codeReviewRepository;
        this.userRepository = userRepository;
        this.aiReviewParser = aiReviewParser;
        this.aiProvider = aiProvider;
        this.aiPromptBuilder = aiPromptBuilder;
        this.aiProperties = aiProperties;
    }

    public CodeReviewResponse queueReview(Long submissionId, Long candidateId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        validateCandidateOwnership(submission, candidateId);

        if (submission.getStatus() != SubmissionStatus.COMPLETED) {
            throw new IllegalStateException("AI review can only be generated for completed submissions");
        }

        if (codeReviewRepository.existsBySubmission(submission)) {
            throw new ConflictException("A review already exists for this submission");
        }

        CodeReview review = new CodeReview();
        review.setSubmission(submission);
        review.setStatus(ReviewStatus.PENDING);
        review.setAttemptCount(0);
        review.setNextAttemptAt(null);
        review.setLastError(null);

        return CodeReviewResponse.fromEntity(codeReviewRepository.save(review));
    }

    /**
     * Backward-compatible service entry point. New callers should use queueReview.
     */
    public CodeReviewResponse generateReview(Long submissionId, Long candidateId) {
        return queueReview(submissionId, candidateId);
    }

    @Transactional
    public void processReview(Long reviewId) {
        LocalDateTime now = LocalDateTime.now();
        int claimed = codeReviewRepository.claimPendingReview(
                reviewId,
                ReviewStatus.PENDING,
                ReviewStatus.PROCESSING,
                now
        );

        if (claimed == 0) {
            return;
        }

        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("AI review not found"));

        try {
            String prompt = aiPromptBuilder.buildPrompt(review.getSubmission());
            String rawReview = aiProvider.reviewCode(prompt);
            AIReviewPayload payload = aiReviewParser.parse(rawReview);

            applyPayload(review, payload);
            review.setStatus(ReviewStatus.COMPLETED);
            review.setLastError(null);
            review.setNextAttemptAt(null);
            codeReviewRepository.save(review);
        } catch (Exception exception) {
            handleProcessingFailure(review, exception);
        }
    }

    @Transactional(readOnly = true)
    public CodeReviewResponse getReviewBySubmission(Long submissionId, Long candidateId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        validateCandidateOwnership(submission, candidateId);

        CodeReview review = codeReviewRepository.findBySubmission(submission)
                .orElseThrow(() -> new ResourceNotFoundException("No AI review found for this submission"));

        return CodeReviewResponse.fromEntity(review);
    }

    @Transactional(readOnly = true)
    public List<CodeReviewResponse> getMyReviews(Long candidateId) {
        return codeReviewRepository.findBySubmissionCandidateIdOrderByCreatedAtDesc(candidateId)
                .stream()
                .map(CodeReviewResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CodeReviewResponse getReviewById(Long reviewId, Long candidateId) {
        CodeReview review = findReview(reviewId);
        validateCandidateOwnership(review.getSubmission(), candidateId);
        return CodeReviewResponse.fromEntity(review);
    }

    @Transactional(readOnly = true)
    public CodeReviewResponse getReviewByIdForAdmin(Long reviewId) {
        return CodeReviewResponse.fromEntity(findReview(reviewId));
    }

    private CodeReview findReview(Long reviewId) {
        return codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("AI review not found"));
    }

    private void validateCandidateOwnership(Submission submission, Long candidateId) {
        if (candidateId == null || submission.getCandidate() == null || !submission.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("You do not have permission to review this submission");
        }
    }

    private void applyPayload(CodeReview review, AIReviewPayload payload) {
        review.setOverallScore(payload.getOverallScore());
        review.setSummary(payload.getSummary());
        review.setComplexityTime(payload.getTimeComplexity());
        review.setComplexitySpace(payload.getSpaceComplexity());

        review.setStrengths(payload.getStrengths() != null ? payload.getStrengths() : new ArrayList<>());
        review.setOptimizationSuggestions(payload.getOptimizationSuggestions() != null ? payload.getOptimizationSuggestions() : new ArrayList<>());
        review.setSecurityIssues(payload.getSecurityIssues() != null ? payload.getSecurityIssues() : new ArrayList<>());

        List<CodeReviewIssue> issues = new ArrayList<>();
        if (payload.getIssues() != null) {
            for (AIReviewIssuePayload issuePayload : payload.getIssues()) {
                if (issuePayload == null) {
                    continue;
                }

                ReviewSeverity severity = issuePayload.getSeverity() != null ? issuePayload.getSeverity() : ReviewSeverity.MEDIUM;
                ReviewCategory category = issuePayload.getCategory() != null ? issuePayload.getCategory() : ReviewCategory.CODE_QUALITY;
                String message = issuePayload.getMessage() != null ? issuePayload.getMessage() : "No issue details provided";
                String suggestion = issuePayload.getSuggestion() != null ? issuePayload.getSuggestion() : "Review the code carefully";

                issues.add(new CodeReviewIssue(severity, category, message, suggestion));
            }
        }
        review.setIssues(issues);
    }

    private void handleProcessingFailure(CodeReview review, Exception exception) {
        int attempts = review.getAttemptCount() != null ? review.getAttemptCount() : 0;
        review.setLastError(errorMessage(exception));

        int maxAttempts = Math.max(1, aiProperties.getMaxAttempts());
        if (attempts >= maxAttempts) {
            review.setStatus(ReviewStatus.FAILED);
            review.setNextAttemptAt(null);
        } else {
            review.setStatus(ReviewStatus.PENDING);
            review.setNextAttemptAt(LocalDateTime.now().plusSeconds(
                    (long) Math.max(0, aiProperties.getRetryDelaySeconds()) * attempts
            ));
        }

        codeReviewRepository.save(review);
    }

    private String errorMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}