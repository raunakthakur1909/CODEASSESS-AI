package com.codeassess.backend.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock")
public class MockAIProvider implements AIProvider {

    @Override
    public String reviewCode(String prompt) {
        return "{\n"
                + "  \"overallScore\": 88,\n"
                + "  \"summary\": \"The solution is correct and easy to follow.\",\n"
                + "  \"timeComplexity\": \"O(n)\",\n"
                + "  \"spaceComplexity\": \"O(n)\",\n"
                + "  \"strengths\": [\n"
                + "    \"Uses a clear and direct approach\",\n"
                + "    \"Reads well and is simple to maintain\"\n"
                + "  ],\n"
                + "  \"issues\": [],\n"
                + "  \"securityIssues\": [],\n"
                + "  \"optimizationSuggestions\": [\n"
                + "    \"Consider extracting reusable logic into a helper method\"\n"
                + "  ]\n"
                + "}";
    }

    @Override
    public String getProviderName() {
        return "mock";
    }
}
