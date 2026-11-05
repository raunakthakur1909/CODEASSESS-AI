package com.codeassess.backend.ai;

import com.codeassess.backend.ai.dto.AIReviewIssuePayload;
import com.codeassess.backend.ai.dto.AIReviewPayload;
import com.codeassess.backend.ai.dto.CodeReviewResponse;
import com.codeassess.backend.config.AIProperties;
import com.codeassess.backend.exception.ConflictException;
import com.codeassess.backend.exception.ForbiddenException;
import com.codeassess.backend.model.Role;
import com.codeassess.backend.model.User;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.repository.CodeReviewRepository;
import com.codeassess.backend.repository.SubmissionRepository;
import com.codeassess.backend.repository.UserRepository;
import com.codeassess.backend.service.CodeReviewService;
import com.codeassess.backend.submission.Language;
import com.codeassess.backend.submission.Submission;
import com.codeassess.backend.submission.SubmissionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeReviewServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private CodeReviewRepository codeReviewRepository;

    @Mock
    private UserRepository userRepository;

    private AIReviewParser parser;
    private AIProvider aiProvider;
    private CodeReviewService service;

    @BeforeEach
    void setUp() {
        parser = new AIReviewParser(new com.fasterxml.jackson.databind.ObjectMapper());
        aiProvider = new MockAIProvider();
        service = new CodeReviewService(
                submissionRepository,
                codeReviewRepository,
                userRepository,
                parser,
                aiProvider,
                new AIPromptBuilder(),
                new AIProperties()
        );
    }

    @Test
    void shouldGenerateReviewForCompletedSubmissionOwnedByCandidate() {
        User candidate = new User("Jane", "jane@example.com", "secret", Role.CANDIDATE);
        candidate.setId(10L);

        Submission submission = new Submission();
        // entity id is not settable by public API; the repository mock provides the value by id lookup
        submission.setCandidate(candidate);
        submission.setSourceCode("public class Demo { public static void main(String[] args) {} }");
        submission.setLanguage(Language.JAVA);
        submission.setStatus(SubmissionStatus.COMPLETED);
        submission.setProblem(new Problem());

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(codeReviewRepository.findBySubmission(submission)).thenReturn(Optional.empty());
        when(codeReviewRepository.save(any(CodeReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CodeReviewResponse response = service.generateReview(1L, 10L);

        assertNotNull(response);
        assertEquals(0, response.getOverallScore());
        assertNull(response.getSummary());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void shouldRejectDuplicateReviewForSameSubmission() {
        User candidate = new User("Jane", "jane@example.com", "secret", Role.CANDIDATE);
        candidate.setId(10L);

        Submission submission = new Submission();
        submission.setCandidate(candidate);
        submission.setSourceCode("public class Demo { } ");
        submission.setLanguage(Language.JAVA);
        submission.setStatus(SubmissionStatus.COMPLETED);
        submission.setProblem(new Problem());

        when(submissionRepository.findById(2L)).thenReturn(Optional.of(submission));
        CodeReview existingReview = new CodeReview();
        existingReview.setStatus(ReviewStatus.COMPLETED);
        when(codeReviewRepository.findBySubmission(submission)).thenReturn(Optional.of(existingReview));

        assertThrows(ConflictException.class, () -> service.generateReview(2L, 10L));
    }

    @Test
    void shouldRejectSubmissionFromDifferentCandidate() {
        User candidate = new User("Jane", "jane@example.com", "secret", Role.CANDIDATE);
        candidate.setId(10L);

        User otherCandidate = new User("John", "john@example.com", "secret", Role.CANDIDATE);
        otherCandidate.setId(11L);

        Submission submission = new Submission();
        submission.setCandidate(otherCandidate);
        submission.setSourceCode("public class Demo { } ");
        submission.setLanguage(Language.JAVA);
        submission.setStatus(SubmissionStatus.COMPLETED);
        submission.setProblem(new Problem());

        when(submissionRepository.findById(3L)).thenReturn(Optional.of(submission));

        assertThrows(ForbiddenException.class, () -> service.generateReview(3L, 10L));
    }

    @Test
    void shouldRejectNonCompletedSubmission() {
        User candidate = new User("Jane", "jane@example.com", "secret", Role.CANDIDATE);
        candidate.setId(10L);

        Submission submission = new Submission();
        submission.setCandidate(candidate);
        submission.setSourceCode("public class Demo { }");
        submission.setLanguage(Language.JAVA);
        submission.setStatus(SubmissionStatus.RUNNING);
        submission.setProblem(new Problem());

        when(submissionRepository.findById(4L)).thenReturn(Optional.of(submission));

        assertThrows(IllegalStateException.class, () -> service.generateReview(4L, 10L));
    }

    @Test
    void shouldProcessClaimedReviewAndPersistCompletedPayload() {
        Submission submission = completedSubmission(10L);
        CodeReview review = new CodeReview();
        review.setSubmission(submission);
        review.setAttemptCount(1);

        when(codeReviewRepository.claimPendingReview(
                org.mockito.ArgumentMatchers.eq(5L),
                org.mockito.ArgumentMatchers.eq(ReviewStatus.PENDING),
                org.mockito.ArgumentMatchers.eq(ReviewStatus.PROCESSING),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(1);
        when(codeReviewRepository.findById(5L)).thenReturn(Optional.of(review));
        when(codeReviewRepository.save(any(CodeReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.processReview(5L);

        assertEquals(ReviewStatus.COMPLETED, review.getStatus());
        assertEquals(88, review.getOverallScore());
        assertNull(review.getLastError());
    }

    @Test
    void shouldMarkReviewFailedAfterMaximumAttempts() {
        Submission submission = completedSubmission(10L);
        CodeReview review = new CodeReview();
        review.setSubmission(submission);
        review.setAttemptCount(3);

        AIProvider failingProvider = new AIProvider() {
            @Override
            public String reviewCode(String prompt) {
                throw new IllegalStateException("provider unavailable");
            }

            @Override
            public String getProviderName() {
                return "failing";
            }
        };
        CodeReviewService failingService = new CodeReviewService(
                submissionRepository,
                codeReviewRepository,
                userRepository,
                parser,
                failingProvider,
                new AIPromptBuilder(),
                new AIProperties()
        );

        when(codeReviewRepository.claimPendingReview(
                org.mockito.ArgumentMatchers.eq(6L),
                org.mockito.ArgumentMatchers.eq(ReviewStatus.PENDING),
                org.mockito.ArgumentMatchers.eq(ReviewStatus.PROCESSING),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(1);
        when(codeReviewRepository.findById(6L)).thenReturn(Optional.of(review));
        when(codeReviewRepository.save(any(CodeReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        failingService.processReview(6L);

        assertEquals(ReviewStatus.FAILED, review.getStatus());
        assertEquals("provider unavailable", review.getLastError());
        assertNull(review.getNextAttemptAt());
    }

    private Submission completedSubmission(Long candidateId) {
        User candidate = new User("Jane", "jane@example.com", "secret", Role.CANDIDATE);
        candidate.setId(candidateId);

        Submission submission = new Submission();
        submission.setCandidate(candidate);
        submission.setSourceCode("public class Demo { }");
        submission.setLanguage(Language.JAVA);
        submission.setStatus(SubmissionStatus.COMPLETED);
        submission.setProblem(new Problem());
        return submission;
    }
}
