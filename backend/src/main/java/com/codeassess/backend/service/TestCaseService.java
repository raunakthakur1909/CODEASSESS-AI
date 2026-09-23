package com.codeassess.backend.service;

import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.assessment.AssessmentStatus;
import com.codeassess.backend.exception.BusinessException;
import com.codeassess.backend.exception.ForbiddenException;
import com.codeassess.backend.exception.ResourceNotFoundException;
import com.codeassess.backend.model.Role;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.repository.ProblemRepository;
import com.codeassess.backend.repository.TestCaseRepository;
import com.codeassess.backend.testcase.TestCase;
import com.codeassess.backend.testcase.dto.CreateTestCaseRequest;
import com.codeassess.backend.testcase.dto.TestCaseResponse;
import com.codeassess.backend.testcase.dto.UpdateTestCaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;

    public TestCaseService(
            TestCaseRepository testCaseRepository,
            ProblemRepository problemRepository
    ) {
        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    /**
     * Create a new test case (ADMIN only)
     */
    public TestCaseResponse createTestCase(
            Long problemId,
            CreateTestCaseRequest request,
            Long creatorId
    ) {

        Problem problem = getProblem(problemId);
        Assessment assessment = problem.getAssessment();

        validateOwnership(assessment, creatorId);
        validateDraft(assessment);

        TestCase testCase = new TestCase();
        testCase.setProblem(problem);
        testCase.setInput(request.getInput().trim());
        testCase.setExpectedOutput(request.getExpectedOutput().trim());
        testCase.setHidden(request.getHidden());
        testCase.setTimeLimitMs(request.getTimeLimitMs());
        testCase.setMemoryLimitMb(request.getMemoryLimitMb());

        TestCase savedTestCase = testCaseRepository.save(testCase);

        // Admin gets full data
        return TestCaseResponse.fromEntity(savedTestCase, true);
    }

    /**
     * Get test cases for a problem (role-based filtering)
     */
    @Transactional(readOnly = true)
    public List<TestCaseResponse> getTestCasesByProblem(
            Long problemId,
            Role userRole,
            Long userId
    ) {

        Problem problem = getProblem(problemId);
        Assessment assessment = problem.getAssessment();
        
        if (userRole != Role.ADMIN && assessment.getStatus() == AssessmentStatus.DRAFT) {
            throw new ForbiddenException("You do not have permission to view test cases for this assessment");
        }

        if (userRole == Role.ADMIN) {
            // Admin sees all test cases with full data
            return testCaseRepository
                    .findByProblemOrderByIdAsc(problem)
                    .stream()
                    .map(tc -> TestCaseResponse.fromEntity(tc, true))
                    .toList();
        } else {
            // Candidate sees only public test cases without sensitive data
            return testCaseRepository
                    .findByProblemAndHiddenFalseOrderByIdAsc(problem)
                    .stream()
                    .map(tc -> TestCaseResponse.fromEntity(tc, false))
                    .toList();
        }
    }

    /**
     * Get single test case by ID (role-based filtering)
     */
    @Transactional(readOnly = true)
    public TestCaseResponse getTestCaseById(
            Long testCaseId,
            Role userRole,
            Long userId
    ) {

        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Test case not found")
                );

        Problem problem = testCase.getProblem();
        Assessment assessment = problem.getAssessment();

        if (userRole != Role.ADMIN && assessment.getStatus() == AssessmentStatus.DRAFT) {
            throw new ForbiddenException("You do not have permission to view this test case");
        }

        if (userRole == Role.ADMIN) {
            // Admin sees full data
            return TestCaseResponse.fromEntity(testCase, true);
        } else {
            // Candidate can only see public test cases without sensitive data
            if (testCase.isHidden()) {
                throw new ForbiddenException("You do not have permission to view this test case");
            }
            return TestCaseResponse.fromEntity(testCase, false);
        }
    }

    /**
     * Update test case (ADMIN only)
     */
    public TestCaseResponse updateTestCase(
            Long testCaseId,
            UpdateTestCaseRequest request,
            Long creatorId
    ) {

        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Test case not found")
                );

        Problem problem = testCase.getProblem();
        Assessment assessment = problem.getAssessment();

        validateOwnership(assessment, creatorId);
        validateDraft(assessment);

        testCase.setInput(request.getInput().trim());
        testCase.setExpectedOutput(request.getExpectedOutput().trim());
        testCase.setHidden(request.getHidden());
        testCase.setTimeLimitMs(request.getTimeLimitMs());
        testCase.setMemoryLimitMb(request.getMemoryLimitMb());

        TestCase updatedTestCase = testCaseRepository.save(testCase);

        // Admin gets full data
        return TestCaseResponse.fromEntity(updatedTestCase, true);
    }

    /**
     * Delete test case (ADMIN only)
     */
    public void deleteTestCase(
            Long testCaseId,
            Long creatorId
    ) {

        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Test case not found")
                );

        Problem problem = testCase.getProblem();
        Assessment assessment = problem.getAssessment();

        validateOwnership(assessment, creatorId);
        validateDraft(assessment);

        testCaseRepository.delete(testCase);
    }

    /**
     * Get test case counts for a problem
     */
    @Transactional(readOnly = true)
    public TestCaseCount getTestCaseCount(Long problemId, Role userRole) {
        Problem problem = getProblem(problemId);
        Assessment assessment = problem.getAssessment();

        if (userRole != Role.ADMIN && assessment.getStatus() == AssessmentStatus.DRAFT) {
            throw new ForbiddenException("You do not have permission to view test case counts for this assessment");
        }
        
        long total = testCaseRepository.countByProblem(problem);
        long publicCount = testCaseRepository.countByProblemAndHiddenFalse(problem);
        long hiddenCount = testCaseRepository.countByProblemAndHiddenTrue(problem);

        return new TestCaseCount(total, publicCount, hiddenCount);
    }

    // Helper methods

    private Problem getProblem(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Problem not found")
                );
    }

    private void validateOwnership(
            Assessment assessment,
            Long creatorId
    ) {
        if (!assessment.getCreatedBy().getId().equals(creatorId)) {
            throw new ForbiddenException(
                    "You do not have permission to modify this assessment"
            );
        }
    }

    private void validateDraft(Assessment assessment) {
        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BusinessException(
                    "Test cases can only be modified while the assessment is in DRAFT status"
            );
        }
    }

    // Inner class for count response
    public static class TestCaseCount {
        private final long total;
        private final long publicCount;
        private final long hiddenCount;

        public TestCaseCount(long total, long publicCount, long hiddenCount) {
            this.total = total;
            this.publicCount = publicCount;
            this.hiddenCount = hiddenCount;
        }

        public long getTotal() {
            return total;
        }

        public long getPublicCount() {
            return publicCount;
        }

        public long getHiddenCount() {
            return hiddenCount;
        }
    }
}
