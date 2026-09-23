package com.codeassess.backend.controller;

import com.codeassess.backend.ai.dto.CodeReviewResponse;
import com.codeassess.backend.service.CodeReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CodeReviewController {

    private final CodeReviewService codeReviewService;

    public CodeReviewController(CodeReviewService codeReviewService) {
        this.codeReviewService = codeReviewService;
    }

    @PostMapping("/submissions/{submissionId}/ai-review")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CodeReviewResponse> generateReview(
            @PathVariable Long submissionId,
            Authentication authentication
    ) {
        Long candidateId = authenticatedUserId(authentication);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(codeReviewService.queueReview(submissionId, candidateId));
    }

    @GetMapping("/submissions/{submissionId}/ai-review")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CodeReviewResponse> getReviewBySubmission(
            @PathVariable Long submissionId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                codeReviewService.getReviewBySubmission(
                        submissionId,
                        authenticatedUserId(authentication)
                )
        );
    }

    @GetMapping("/ai-reviews/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<CodeReviewResponse>> getMyReviews(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                codeReviewService.getMyReviews(authenticatedUserId(authentication))
        );
    }

    @GetMapping("/ai-reviews/{reviewId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<CodeReviewResponse> getReviewById(
            @PathVariable Long reviewId,
            Authentication authentication
    ) {
        Long userId = authenticatedUserId(authentication);
        boolean admin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        CodeReviewResponse response = admin
                ? codeReviewService.getReviewByIdForAdmin(reviewId)
                : codeReviewService.getReviewById(reviewId, userId);

        return ResponseEntity.ok(response);
    }

    private Long authenticatedUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}