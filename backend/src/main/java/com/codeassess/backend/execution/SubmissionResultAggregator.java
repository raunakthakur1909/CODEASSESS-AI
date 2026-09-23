package com.codeassess.backend.execution;

import com.codeassess.backend.submission.SubmissionResult;
import org.springframework.stereotype.Component;

@Component
public class SubmissionResultAggregator {

    public SubmissionEvaluationSummary aggregate(
            int passedTests,
            int totalTests
    ) {

        if (totalTests <= 0) {
            return new SubmissionEvaluationSummary(
                    SubmissionResult.SYSTEM_ERROR,
                    0,
                    0,
                    0
            );
        }

        int score =
                (int) Math.round(
                        ((double) passedTests / totalTests) * 100
                );

        SubmissionResult result;

        if (passedTests == totalTests) {
            result = SubmissionResult.ACCEPTED;
        } else {
            result = SubmissionResult.WRONG_ANSWER;
        }

        return new SubmissionEvaluationSummary(
                result,
                passedTests,
                totalTests,
                score
        );
    }
}
