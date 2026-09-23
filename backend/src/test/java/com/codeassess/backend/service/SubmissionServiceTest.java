package com.codeassess.backend.service;

import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.assessment.AssessmentStatus;
import com.codeassess.backend.exception.BusinessException;
import com.codeassess.backend.model.Role;
import com.codeassess.backend.model.User;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.repository.ProblemRepository;
import com.codeassess.backend.repository.AssessmentAttemptRepository;
import com.codeassess.backend.repository.SubmissionRepository;
import com.codeassess.backend.repository.UserRepository;
import com.codeassess.backend.service.CodeReviewService;
import com.codeassess.backend.submission.dto.CreateSubmissionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Submission Service Tests - Expiry & Recovery")
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AssessmentAttemptRepository attemptRepository;
    @Mock
    private CodeReviewService codeReviewService;

    private SubmissionService submissionService;

    @BeforeEach
    void setUp() {
        submissionService = new SubmissionService(
                submissionRepository,
                problemRepository,
                userRepository,
                attemptRepository,
                codeReviewService,
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry()
        );
    }

    @Test
    @DisplayName("Should reject submission if Assessment is CLOSED")
    void testClosedAssessmentRejectsSubmission() {
        // Arrange
        User candidate = new User();
        candidate.setRole(Role.CANDIDATE);

        Assessment assessment = new Assessment();
        assessment.setStatus(AssessmentStatus.CLOSED);

        Problem problem = new Problem();
        problem.setAssessment(assessment);

        when(userRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(problemRepository.findById(100L)).thenReturn(Optional.of(problem));

        CreateSubmissionRequest request = new CreateSubmissionRequest();
        request.setSourceCode("System.out.println('test');");
        request.setLanguage(com.codeassess.backend.submission.Language.JAVA);

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> submissionService.createSubmission(100L, request, 1L)
        );

        assertTrue(exception.getMessage().contains("published"));
    }
}
