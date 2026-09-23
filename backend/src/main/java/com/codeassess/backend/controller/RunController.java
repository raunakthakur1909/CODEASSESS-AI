package com.codeassess.backend.controller;

import com.codeassess.backend.run.dto.RunRequest;
import com.codeassess.backend.run.dto.RunResponse;
import com.codeassess.backend.service.RunService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessments/{assessmentId}/problems/{problemId}/run")
public class RunController {

    private final RunService runService;

    public RunController(RunService runService) {
        this.runService = runService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<RunResponse> executeRun(
            @PathVariable Long assessmentId,
            @PathVariable Long problemId,
            @Valid @RequestBody RunRequest request,
            Authentication authentication
    ) {
        Long candidateId = Long.parseLong(authentication.getName());

        RunResponse response = runService.executeRun(
                assessmentId,
                problemId,
                request,
                candidateId
        );

        return ResponseEntity.ok(response);
    }
}
