package com.codeassess.backend.controller;

import com.codeassess.backend.draft.dto.DraftResponse;
import com.codeassess.backend.draft.dto.SaveDraftRequest;
import com.codeassess.backend.service.DraftService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessments/{assessmentId}/problems/{problemId}/drafts")
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<DraftResponse> saveDraft(
            @PathVariable Long assessmentId,
            @PathVariable Long problemId,
            @Valid @RequestBody SaveDraftRequest request,
            Authentication authentication
    ) {
        Long candidateId = Long.parseLong(authentication.getName());

        DraftResponse response = draftService.saveDraft(
                assessmentId,
                problemId,
                request,
                candidateId
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<DraftResponse> getDraft(
            @PathVariable Long assessmentId,
            @PathVariable Long problemId,
            Authentication authentication
    ) {
        Long candidateId = Long.parseLong(authentication.getName());

        DraftResponse response = draftService.getDraft(
                assessmentId,
                problemId,
                candidateId
        );

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}
