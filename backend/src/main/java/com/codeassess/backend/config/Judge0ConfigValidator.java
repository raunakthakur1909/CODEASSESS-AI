package com.codeassess.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Validates Judge0 configuration on application startup.
 * 
 * This validator checks if Judge0 is properly configured and logs warnings
 * if the configuration is missing or incomplete. The application will still
 * start, but code execution features will not work.
 */
@Component
public class Judge0ConfigValidator {

    private static final Logger logger = LoggerFactory.getLogger(Judge0ConfigValidator.class);

    private final Judge0Config judge0Config;

    public Judge0ConfigValidator(Judge0Config judge0Config) {
        this.judge0Config = judge0Config;
    }

    /**
     * Validates Judge0 configuration after application startup.
     * Logs appropriate messages based on configuration state.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        logger.info("Validating Judge0 configuration...");

        if (!judge0Config.isConfigured()) {
            logger.warn("═══════════════════════════════════════════════════════════");
            logger.warn("Judge0 API is NOT properly configured!");
            logger.warn("Code execution features will NOT work.");
            logger.warn("");
            logger.warn("To enable Judge0:");
            logger.warn("1. Sign up for RapidAPI: https://rapidapi.com/");
            logger.warn("2. Subscribe to Judge0 CE: https://rapidapi.com/judge0-official/api/judge0-ce");
            logger.warn("3. Copy your RapidAPI key");
            logger.warn("4. Update application.properties:");
            logger.warn("   judge0.api.key=YOUR_RAPIDAPI_KEY");
            logger.warn("");
            logger.warn("Current configuration:");
            logger.warn("  API URL: {}", judge0Config.getApiUrl());
            logger.warn("  API Host: {}", judge0Config.getApiHost());
            logger.warn("  API Key: {}", maskApiKey(judge0Config.getApiKey()));
            logger.warn("═══════════════════════════════════════════════════════════");
        } else {
            logger.info("✓ Judge0 configuration is valid");
            logger.info("  API URL: {}", judge0Config.getApiUrl());
            logger.info("  API Host: {}", judge0Config.getApiHost());
            logger.info("  Max Wait Time: {} seconds", judge0Config.getMaxWaitTime());
            logger.info("  Poll Interval: {} ms", judge0Config.getPollInterval());
            logger.info("  API Key: {}", maskApiKey(judge0Config.getApiKey()));
        }
    }

    /**
     * Masks API key for logging purposes.
     * Shows only first 4 and last 4 characters.
     * 
     * @param apiKey the API key to mask
     * @return masked API key
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "[NOT SET]";
        }

        if ("YOUR_RAPIDAPI_KEY_HERE".equals(apiKey)) {
            return "[DEFAULT - NOT CONFIGURED]";
        }

        if (apiKey.length() <= 8) {
            return "***";
        }

        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
