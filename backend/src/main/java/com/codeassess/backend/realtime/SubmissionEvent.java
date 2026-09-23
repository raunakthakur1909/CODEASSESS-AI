package com.codeassess.backend.realtime;

import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.submission.SubmissionStatus;

import java.time.Instant;

public record SubmissionEvent(
        Long submissionId,
        SubmissionStatus status,
        SubmissionResult result,
        Integer score,
        Integer passedTests,
        Integer totalTests,
        Integer currentTestCase,
        Instant timestamp
) {}
