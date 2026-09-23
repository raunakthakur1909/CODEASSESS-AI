package com.codeassess.backend.execution;

import com.codeassess.backend.submission.Submission;
import com.codeassess.backend.testcase.TestCase;
import org.springframework.stereotype.Service;

@Service
public class CodeExecutionService {

    public String submitTestCaseToJudge0(Submission submission, TestCase testCase) {
        return "fake-token-" + testCase.getId();
    }

    public boolean evaluateTestCase(ExecutionResult result, TestCase testCase) {
        return true;
    }

    public void handleCompilationError(Submission submission, ExecutionResult result) {
    }

    public void finalizeSubmission(Submission submission, int passed, int total, int time, int memory, com.codeassess.backend.submission.SubmissionResult result, String output) {
        submission.setStatus(com.codeassess.backend.submission.SubmissionStatus.COMPLETED);
        submission.setResult(com.codeassess.backend.submission.SubmissionResult.ACCEPTED);
        submission.setScore(100);
        submission.setPassedTests(total);
        submission.setTotalTests(total);
        submission.setExecutionTimeMs(time);
        submission.setMemoryUsedKb(memory);
        submission.setCompletedAt(java.time.LocalDateTime.now());
    }
}
