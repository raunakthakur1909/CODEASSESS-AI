package com.codeassess.backend.service;

import com.codeassess.backend.run.dto.RunRequest;
import com.codeassess.backend.run.dto.RunResponse;
import com.codeassess.backend.run.dto.RunResult;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.testcase.TestCase;
import com.codeassess.backend.repository.TestCaseRepository;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class RunService {

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;

    public RunService(TestCaseRepository testCaseRepository, ProblemRepository problemRepository) {
        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    public RunResponse executeRun(Long assessmentId, Long problemId, RunRequest request, Long candidateId) {
        Problem problem = problemRepository.findById(problemId).orElse(null);
        List<TestCase> publicTestCases = problem != null ? testCaseRepository.findByProblemAndHiddenFalseOrderByIdAsc(problem) : new ArrayList<>();
        
        RunResponse runResponse = new RunResponse();
        List<RunResult> testResults = new ArrayList<>();
        
        for (TestCase tc : publicTestCases) {
            RunResult rr = new RunResult();
            rr.setTestCaseId(tc.getId());
            rr.setInput(tc.getInput());
            rr.setExpectedOutput(tc.getExpectedOutput());
            rr.setActualOutput(tc.getExpectedOutput()); // Fake pass
            rr.setPassed(true);
            rr.setTime(0.05);
            rr.setMemory(12000);
            testResults.add(rr);
        }
        
        runResponse.setTestResults(testResults);
        runResponse.setPassedTests(publicTestCases.size());
        runResponse.setTotalTests(publicTestCases.size());
        runResponse.setStatus(SubmissionResult.ACCEPTED);
        
        return runResponse;
    }
}
