package com.codeassess.backend.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Judge0 API.
 * Binds properties with prefix "judge0" from application.properties.
 * 
 * Properties:
 * - judge0.api.url: Judge0 API base URL
 * - judge0.api.key: RapidAPI key (optional, for hosted Judge0)
 * - judge0.api.host: RapidAPI host (optional)
 * - judge0.max-wait-time: Maximum wait time for execution results (seconds)
 * - judge0.poll-interval: Polling interval for checking status (milliseconds)
 */
@Configuration
@ConfigurationProperties(prefix = "judge0.api")
public class Judge0Config {

    private static final Logger logger = LoggerFactory.getLogger(Judge0Config.class);

    private String url;
    private String key;
    private String host;
    private Integer maxWaitTime = 30;
    private Integer pollInterval = 1000;

    @PostConstruct
    public void validateConfig() {
        if (url == null || url.isEmpty()) {
            logger.warn("Judge0 API URL is not configured!");
        } else {
            logger.info("Judge0 configured: url={}", url);
        }

        if (key == null || key.isEmpty() || key.equals("YOUR_RAPIDAPI_KEY_HERE")) {
            logger.warn("Judge0 API key is not configured. Will attempt to connect without authentication.");
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(Integer maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public Integer getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Integer pollInterval) {
        this.pollInterval = pollInterval;
    }

    /**
     * Checks if Judge0 is properly configured with all required settings.
     * 
     * @return true if configuration is valid and ready to use
     */
    public boolean isConfigured() {
        return url != null && !url.isBlank()
                && key != null && !key.isBlank()
                && !"YOUR_RAPIDAPI_KEY_HERE".equals(key);
    }

    /**
     * Gets the API URL.
     * Alias for getUrl() for consistency with validator.
     */
    public String getApiUrl() {
        return url;
    }

    /**
     * Gets the API key.
     * Alias for getKey() for consistency with validator.
     */
    public String getApiKey() {
        return key;
    }

    /**
     * Gets the API host.
     * Alias for getHost() for consistency with validator.
     */
    public String getApiHost() {
        return host;
    }

    /**
     * Gets the full URL for creating a submission.
     * 
     * @return submission endpoint URL
     */
    public String getSubmissionUrl() {
        return url + "/submissions";
    }

    /**
     * Gets the full URL for retrieving a submission by token.
     * 
     * @param token submission token
     * @return submission status endpoint URL
     */
    public String getSubmissionUrl(String token) {
        return url + "/submissions/" + token;
    }
}
