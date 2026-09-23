package com.codeassess.backend.controller;

import com.codeassess.backend.problem.dto.CreateProblemRequest;
import com.codeassess.backend.problem.dto.ProblemResponse;
import com.codeassess.backend.problem.dto.UpdateProblemRequest;
import com.codeassess.backend.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping("/api/assessments/{assessmentId}/problems")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemResponse> createProblem(
            @PathVariable Long assessmentId,
            @Valid @RequestBody CreateProblemRequest request,
            Authentication authentication
    ) {

        Long userId = Long.parseLong(authentication.getName());

        ProblemResponse response =
                problemService.createProblem(
                        assessmentId,
                        request,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/api/assessments/{assessmentId}/problems")
    public ResponseEntity<List<ProblemResponse>> getProblemsByAssessment(
            @PathVariable Long assessmentId,
            Authentication authentication
    ) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(
                problemService.getProblemsByAssessment(assessmentId, isAdmin)
        );
    }

    @GetMapping("/api/problems/{problemId}")
    public ResponseEntity<ProblemResponse> getProblemById(
            @PathVariable Long problemId,
            Authentication authentication
    ) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(
                problemService.getProblemById(problemId, isAdmin)
        );
    }

    @PutMapping("/api/problems/{problemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemResponse> updateProblem(
            @PathVariable Long problemId,
            @Valid @RequestBody UpdateProblemRequest request,
            Authentication authentication
    ) {

        Long userId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                problemService.updateProblem(
                        problemId,
                        request,
                        userId
                )
        );
    }

    @DeleteMapping("/api/problems/{problemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProblem(
            @PathVariable Long problemId,
            Authentication authentication
    ) {

        Long userId = Long.parseLong(authentication.getName());

        problemService.deleteProblem(problemId, userId);

        return ResponseEntity.noContent().build();
    }
}
