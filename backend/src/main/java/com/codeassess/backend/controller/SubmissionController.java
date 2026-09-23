package com.codeassess.backend.controller;

import com.codeassess.backend.submission.dto.CreateSubmissionRequest;
import com.codeassess.backend.submission.dto.SubmissionResponse;
import com.codeassess.backend.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for code submission operations.
 * All endpoints require CANDIDATE role.
 */
@RestController
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    /**
     * Submit code for a problem
     * POST /api/problems/{problemId}/submissions
     */
    @PostMapping("/problems/{problemId}/submissions")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<SubmissionResponse> createSubmission(
            @PathVariable Long problemId,
            @Valid @RequestBody CreateSubmissionRequest request,
            Authentication authentication
    ) {

        Long candidateId =
                Long.parseLong(authentication.getName());

        SubmissionResponse response =
                submissionService.createSubmission(
                        problemId,
                        request,
                        candidateId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get a single submission by ID
     * GET /api/submissions/{id}
     */
    @GetMapping("/submissions/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<SubmissionResponse> getSubmission(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long candidateId =
                Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                submissionService.getSubmission(
                        id,
                        candidateId
                )
        );
    }

    /**
     * Get all submissions by the current candidate
     * GET /api/submissions/my
     */
    @GetMapping("/submissions/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(
            Authentication authentication
    ) {

        Long candidateId =
                Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                submissionService.getMySubmissions(candidateId)
        );
    }

    /**
     * Get all submissions by the current candidate for a specific problem
     * GET /api/problems/{problemId}/submissions/my
     */
    @GetMapping("/problems/{problemId}/submissions/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<SubmissionResponse>> getMyProblemSubmissions(
            @PathVariable Long problemId,
            Authentication authentication
    ) {

        Long candidateId =
                Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                submissionService.getMyProblemSubmissions(
                        problemId,
                        candidateId
                )
        );
    }
}
