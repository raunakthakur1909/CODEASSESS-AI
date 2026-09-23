package com.codeassess.backend.controller;

import com.codeassess.backend.exception.ForbiddenException;
import com.codeassess.backend.exception.ResourceNotFoundException;
import com.codeassess.backend.realtime.SseConnectionManager;
import com.codeassess.backend.realtime.SubmissionEvent;
import com.codeassess.backend.repository.SubmissionRepository;
import com.codeassess.backend.submission.Submission;
import com.codeassess.backend.realtime.AssessmentEvent;
import com.codeassess.backend.realtime.AssessmentEventType;
import com.codeassess.backend.repository.AssessmentRepository;
import com.codeassess.backend.assessment.Assessment;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/events")
public class SseController {

    private final SseConnectionManager connectionManager;
    private final SubmissionRepository submissionRepository;
    private final AssessmentRepository assessmentRepository;

    public SseController(SseConnectionManager connectionManager, SubmissionRepository submissionRepository, AssessmentRepository assessmentRepository) {
        this.connectionManager = connectionManager;
        this.submissionRepository = submissionRepository;
        this.assessmentRepository = assessmentRepository;
    }

    @GetMapping(value = "/submissions/{submissionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToSubmissionEvents(
            @PathVariable Long submissionId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (!isAdmin && !submission.getCandidate().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to view this submission");
        }

        SseEmitter emitter = connectionManager.connect("submission:" + submissionId);

        // Send initial state immediately
        SubmissionEvent initialEvent = new SubmissionEvent(
                submission.getId(),
                submission.getStatus(),
                submission.getResult(),
                submission.getScore(),
                submission.getPassedTests(),
                submission.getTotalTests(),
                null,
                submission.getSubmittedAt().toInstant(ZoneOffset.UTC)
        );

        try {
            emitter.send(SseEmitter.event()
                    .name("submission-status")
                    .data(initialEvent));
        } catch (IOException e) {
            connectionManager.remove("submission:" + submissionId, emitter);
        }

        return emitter;
    }

    @GetMapping(value = "/assessments/{assessmentId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToAssessmentEvents(
            @PathVariable Long assessmentId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

        if (!isAdmin && assessment.getStatus() == com.codeassess.backend.assessment.AssessmentStatus.DRAFT) {
            throw new ForbiddenException("You do not have permission to view this assessment");
        }

        SseEmitter emitter = connectionManager.connect("assessment:" + assessmentId);

        Instant expiresAt = null;
        if (assessment.getStartedAt() != null) {
            expiresAt = assessment.getStartedAt().plusMinutes(assessment.getDurationMinutes()).toInstant(ZoneOffset.UTC);
        }

        // Send initial state immediately
        AssessmentEvent initialEvent = new AssessmentEvent(
                assessment.getId(),
                AssessmentEventType.ASSESSMENT_STARTED, // Provide initial state type
                assessment.getStatus().name(),
                java.time.ZonedDateTime.now(ZoneOffset.UTC).toInstant(),
                expiresAt
        );

        try {
            emitter.send(SseEmitter.event()
                    .name("assessment-status")
                    .data(initialEvent));
        } catch (IOException e) {
            connectionManager.remove("assessment:" + assessmentId, emitter);
        }

        return emitter;
    }
}
