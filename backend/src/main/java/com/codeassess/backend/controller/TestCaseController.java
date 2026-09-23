package com.codeassess.backend.controller;

import com.codeassess.backend.dto.MessageResponse;
import com.codeassess.backend.model.Role;
import com.codeassess.backend.model.User;
import com.codeassess.backend.repository.UserRepository;
import com.codeassess.backend.service.TestCaseService;
import com.codeassess.backend.testcase.dto.CreateTestCaseRequest;
import com.codeassess.backend.testcase.dto.TestCaseResponse;
import com.codeassess.backend.testcase.dto.UpdateTestCaseRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TestCaseController {

    private final TestCaseService testCaseService;
    private final UserRepository userRepository;

    public TestCaseController(
            TestCaseService testCaseService,
            UserRepository userRepository
    ) {
        this.testCaseService = testCaseService;
        this.userRepository = userRepository;
    }

    /**
     * Create test case (ADMIN only)
     * POST /api/problems/{problemId}/test-cases
     */
    @PostMapping("/problems/{problemId}/test-cases")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestCaseResponse> createTestCase(
            @PathVariable Long problemId,
            @Valid @RequestBody CreateTestCaseRequest request,
            Authentication authentication
    ) {

        User user = getUserFromAuth(authentication);

        TestCaseResponse response = testCaseService.createTestCase(
                problemId,
                request,
                user.getId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get all test cases for a problem (role-based filtering)
     * GET /api/problems/{problemId}/test-cases
     */
    @GetMapping("/problems/{problemId}/test-cases")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<List<TestCaseResponse>> getTestCasesByProblem(
            @PathVariable Long problemId,
            Authentication authentication
    ) {

        User user = getUserFromAuth(authentication);

        List<TestCaseResponse> responses = testCaseService.getTestCasesByProblem(
                problemId,
                user.getRole(),
                user.getId()
        );

        return ResponseEntity.ok(responses);
    }

    /**
     * Get single test case by ID (role-based filtering)
     * GET /api/test-cases/{id}
     */
    @GetMapping("/test-cases/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<TestCaseResponse> getTestCaseById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        User user = getUserFromAuth(authentication);

        TestCaseResponse response = testCaseService.getTestCaseById(
                id,
                user.getRole(),
                user.getId()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update test case (ADMIN only)
     * PUT /api/test-cases/{id}
     */
    @PutMapping("/test-cases/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestCaseResponse> updateTestCase(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTestCaseRequest request,
            Authentication authentication
    ) {

        User user = getUserFromAuth(authentication);

        TestCaseResponse response = testCaseService.updateTestCase(
                id,
                request,
                user.getId()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Delete test case (ADMIN only)
     * DELETE /api/test-cases/{id}
     */
    @DeleteMapping("/test-cases/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteTestCase(
            @PathVariable Long id,
            Authentication authentication
    ) {

        User user = getUserFromAuth(authentication);

        testCaseService.deleteTestCase(id, user.getId());

        return ResponseEntity.ok(
                new MessageResponse("Test case deleted successfully")
        );
    }

    /**
     * Get test case counts for a problem
     * GET /api/problems/{problemId}/test-cases/count
     */
    @GetMapping("/problems/{problemId}/test-cases/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ResponseEntity<TestCaseService.TestCaseCount> getTestCaseCount(
            @PathVariable Long problemId,
            Authentication authentication
    ) {

        User user = getUserFromAuth(authentication);

        TestCaseService.TestCaseCount count = 
                testCaseService.getTestCaseCount(problemId, user.getRole());

        return ResponseEntity.ok(count);
    }

    // Helper method

    private User getUserFromAuth(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }
}
