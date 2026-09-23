package com.codeassess.backend.ai;

import com.codeassess.backend.config.AIProperties;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.submission.Language;
import com.codeassess.backend.submission.Submission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AIPromptBuilderTest {

    @Test
    void shouldIncludeProblemAndSourceButNeverTestCaseData() {
        Problem problem = new Problem();
        problem.setTitle("Two sum");
        problem.setDescription("Find two numbers that add to a target.");

        Submission submission = new Submission();
        submission.setProblem(problem);
        submission.setLanguage(Language.JAVA);
        submission.setSourceCode("public class Solution {} ");

        String prompt = new AIPromptBuilder().buildPrompt(submission);

        assertTrue(prompt.contains("Two sum"));
        assertTrue(prompt.contains("BEGIN SOURCE CODE"));
        assertTrue(prompt.contains("public class Solution"));
        assertFalse(prompt.contains("expectedOutput"));
        assertFalse(prompt.contains("secret-hidden-input"));
    }

    @Test
    void shouldRejectSourceCodeAboveConfiguredLimit() {
        AIProperties properties = new AIProperties();
        properties.setMaxSourceCodeLength(5);

        Submission submission = new Submission();
        submission.setSourceCode("123456");

        assertThrows(IllegalArgumentException.class, () ->
                new AIPromptBuilder(properties).buildPrompt(submission)
        );
    }

    @Test
    void shouldRejectPromptAboveConfiguredLimit() {
        AIProperties properties = new AIProperties();
        properties.setMaxPromptLength(10);

        Submission submission = new Submission();
        submission.setSourceCode("class Solution {}");

        assertThrows(IllegalArgumentException.class, () ->
                new AIPromptBuilder(properties).buildPrompt(submission)
        );
    }

    @Test
    void shouldAllowConfiguredSourceLimitWhenPromptLimitAllowsMetadata() {
        AIProperties properties = new AIProperties();
        properties.setMaxSourceCodeLength(100);
        properties.setMaxPromptLength(120000);

        Submission submission = new Submission();
        submission.setSourceCode("class Solution {}");

        assertDoesNotThrow(() -> new AIPromptBuilder(properties).buildPrompt(submission));
    }
}
