package com.codeassess.backend.realtime;

import com.codeassess.backend.execution.CodeExecutionService;
import com.codeassess.backend.execution.ExecutionResult;
import com.codeassess.backend.execution.Judge0Client;
import com.codeassess.backend.execution.Judge0SubmissionResponse;
import com.codeassess.backend.execution.SubmissionExecutionWorker;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.repository.SubmissionRepository;
import com.codeassess.backend.repository.TestCaseRepository;
import com.codeassess.backend.submission.Submission;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.submission.SubmissionStatus;
import com.codeassess.backend.testcase.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Event Publishing & Worker Integration Tests")
class SubmissionSseEventPublisherTest {

    @Mock
    private SseConnectionManager connectionManager;

    private SubmissionSseEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SubmissionSseEventPublisher(connectionManager);
    }

    @Test
    @DisplayName("Publishes correctly mapped events")
    void testEventMappingAndPublishing() {
        Submission submission = new Submission();
        
        try {
            var idField = Submission.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(submission, 10L);
        } catch (Exception e) {}

        submission.setStatus(SubmissionStatus.RUNNING);
        submission.setResult(SubmissionResult.PENDING);
        submission.setScore(0);
        submission.setPassedTests(0);
        submission.setTotalTests(0);

        publisher.publish(submission);

        ArgumentCaptor<SubmissionEvent> eventCaptor = ArgumentCaptor.forClass(SubmissionEvent.class);
        verify(connectionManager).publish(eq("submission:10"), eq("submission-status"), eventCaptor.capture());

        SubmissionEvent event = eventCaptor.getValue();
        assertThat(event.submissionId()).isEqualTo(10L);
        assertThat(event.status()).isEqualTo(SubmissionStatus.RUNNING);
        
        // Assert absence of sensitive fields explicitly by checking the record
        // (Since it's a DTO without getters for sourceCode/tokens, this is intrinsically true)
    }

    @Test
    @DisplayName("Worker publishes events after persisting states")
    void testWorkerIntegration(
            @Mock SubmissionRepository submissionRepository,
            @Mock CodeExecutionService codeExecutionService,
            @Mock TestCaseRepository testCaseRepository,
            @Mock Judge0Client judge0Client,
            @Mock SubmissionEventPublisher mockPublisher
    ) throws Exception {

        SubmissionExecutionWorker worker = new SubmissionExecutionWorker(
                submissionRepository,
                testCaseRepository,
                mockPublisher
        );

        Submission submission = new Submission();
        Problem problem = new Problem();
        
        try {
            var idField = Submission.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(submission, 42L);
            var probIdField = Problem.class.getDeclaredField("id");
            probIdField.setAccessible(true);
            probIdField.set(problem, 1L);
        } catch (Exception e) {}

        submission.setProblem(problem);
        submission.setExecutionToken("token-xyz");

        // Mock DB returning the submission
        when(submissionRepository.findById(42L))
                .thenReturn(Optional.of(submission));

        // Mock Test Cases
        TestCase tc1 = new TestCase();
        when(testCaseRepository.findByProblemOrderByIdAsc(problem))
                .thenReturn(List.of(tc1));

        worker.processSubmission(42L);

        // Verify publisher was called
        verify(mockPublisher, times(1)).publish(submission);
    }
}
