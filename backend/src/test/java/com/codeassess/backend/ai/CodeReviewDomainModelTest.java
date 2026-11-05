package com.codeassess.backend.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeReviewDomainModelTest {

    @Test
    void shouldCreateStructuredCodeReview() {
        CodeReview review = new CodeReview();
        review.setOverallScore(82);
        review.setSummary("The solution is correct and reasonably efficient.");
        review.setComplexityTime("O(n)");
        review.setComplexitySpace("O(n)");
        review.setStatus(ReviewStatus.COMPLETED);

        review.getStrengths().add("Uses an efficient HashMap approach");
        review.getOptimizationSuggestions().add("Use a more descriptive method name.");

        CodeReviewIssue issue = new CodeReviewIssue(
                ReviewSeverity.MEDIUM,
                ReviewCategory.CODE_QUALITY,
                "Input parsing could be separated from business logic",
                "Extract input handling into a separate method.",
                null,
                null,
                "Parsing Logic"
        );
        review.getIssues().add(issue);

        assertEquals(82, review.getOverallScore());
        assertEquals("The solution is correct and reasonably efficient.", review.getSummary());
        assertEquals("O(n)", review.getComplexityTime());
        assertEquals("O(n)", review.getComplexitySpace());
        assertEquals(ReviewStatus.COMPLETED, review.getStatus());
        assertFalse(review.getStrengths().isEmpty());
        assertEquals(1, review.getIssues().size());
        assertEquals("Input parsing could be separated from business logic", review.getIssues().get(0).getMessage());
    }
}
