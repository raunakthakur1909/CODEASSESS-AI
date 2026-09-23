package com.codeassess.backend.controller;

import com.codeassess.backend.assessment.dto.AssessmentResponse;
import com.codeassess.backend.assessment.dto.CreateAssessmentRequest;
import com.codeassess.backend.assessment.dto.UpdateAssessmentRequest;
import com.codeassess.backend.assessment.dto.AssessmentAttemptResponse;
import com.codeassess.backend.service.AssessmentAttemptService;
import com.codeassess.backend.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final AssessmentAttemptService attemptService;

    public AssessmentController(AssessmentService assessmentService, AssessmentAttemptService attemptService) {
        this.assessmentService = assessmentService;
        this.attemptService = attemptService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssessmentResponse> createAssessment(
            @Valid @RequestBody CreateAssessmentRequest request,
            Authentication authentication
    ) {

        Long userId = Long.parseLong(authentication.getName());

        AssessmentResponse response =
                assessmentService.createAssessment(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AssessmentResponse>> getAllAssessments(Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(
                assessmentService.getAllAssessments(userId, isAdmin)
        );
    }

    @GetMapping("/available")
    public ResponseEntity<List<AssessmentResponse>> getAvailableAssessments(Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                assessmentService.getAllAssessments(userId, false)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssessmentResponse> getAssessmentById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(
                assessmentService.getAssessmentById(id, userId, isAdmin)
        );
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AssessmentResponse>> getMyAssessments(
            Authentication authentication
    ) {

        Long userId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                assessmentService.getAssessmentsByCreator(userId)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssessmentResponse> updateAssessment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssessmentRequest request,
            Authentication authentication
    ) {

        Long userId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                assessmentService.updateAssessment(
                        id,
                        request,
                        userId
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAssessment(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long userId = Long.parseLong(authentication.getName());

        assessmentService.deleteAssessment(id, userId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssessmentResponse> publishAssessment(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long userId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                assessmentService.publishAssessment(id, userId)
        );
    }

    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssessmentResponse> duplicateAssessment(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long userId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                assessmentService.duplicateAssessment(id, userId)
        );
    }

    @PostMapping("/{id}/attempt")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<AssessmentAttemptResponse> startAttempt(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(
                attemptService.startAttempt(id, userId)
        );
    }

    @GetMapping("/{id}/attempt")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<AssessmentAttemptResponse> getAttempt(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(
                attemptService.getAttempt(id, userId)
        );
    }
}
