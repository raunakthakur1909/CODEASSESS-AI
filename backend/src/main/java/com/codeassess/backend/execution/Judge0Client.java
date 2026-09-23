package com.codeassess.backend.execution;

import com.codeassess.backend.config.Judge0Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for Judge0 API communication.
 * 
 * Responsibilities:
 * - Submit code to Judge0 for execution
 * - Retrieve execution results by token
 * 
 * Does NOT contain:
 * - Assessment validation
 * - Candidate authorization
 * - Score calculation
 * - Hidden test case logic
 * - Submission state management
 * 
 * Those belong to the service/execution layers.
 */
@Component
public class Judge0Client {

    private static final Logger logger = LoggerFactory.getLogger(Judge0Client.class);

    private final RestTemplate restTemplate;
    private final Judge0Config judge0Config;
    private final Timer createSubmissionTimer;
    private final Timer getSubmissionTimer;
    private final Counter failureCounter;

    public Judge0Client(RestTemplate restTemplate, Judge0Config judge0Config, MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.judge0Config = judge0Config;
        this.createSubmissionTimer = meterRegistry.timer("codeassess.judge0.latency", "operation", "create");
        this.getSubmissionTimer = meterRegistry.timer("codeassess.judge0.latency", "operation", "get");
        this.failureCounter = meterRegistry.counter("codeassess.judge0.failures");
    }

    /**
     * Submit code to Judge0 for execution.
     * 
     * @param request The submission request with source code and language
     * @return Judge0 response containing the submission token
     * @throws RestClientException if the API call fails
     */
    public Judge0SubmissionResponse createSubmission(Judge0SubmissionRequest request) {
        String url = judge0Config.getUrl() + "/submissions?base64_encoded=false&wait=false";

        HttpHeaders headers = createHeaders();
        HttpEntity<Judge0SubmissionRequest> entity = new HttpEntity<>(request, headers);

        logger.debug("Submitting code to Judge0: language_id={}", request.getLanguageId());

        try {
            ResponseEntity<Judge0SubmissionResponse> response = createSubmissionTimer.record(() -> 
                restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Judge0SubmissionResponse.class
                )
            );

            Judge0SubmissionResponse result = response.getBody();
            if (result != null && result.getToken() != null) {
                logger.info("Submission created successfully: token={}", result.getToken());
                return result;
            } else {
                throw new RuntimeException("Judge0 response missing token");
            }

        } catch (RestClientException e) {
            failureCounter.increment();
            logger.error("Failed to create submission on Judge0: {}", e.getMessage());
            throw new RuntimeException("Failed to communicate with Judge0 API", e);
        }
    }

    /**
     * Submit code to Judge0 for synchronous execution (wait for result).
     * 
     * @param request The submission request with source code and language
     * @return Judge0 response containing the execution results
     */
    public Judge0SubmissionResponse createSynchronousSubmission(Judge0SubmissionRequest request) {
        String url = judge0Config.getUrl() + "/submissions?base64_encoded=false&wait=true";

        HttpHeaders headers = createHeaders();
        HttpEntity<Judge0SubmissionRequest> entity = new HttpEntity<>(request, headers);

        logger.debug("Submitting code to Judge0 synchronously: language_id={}", request.getLanguageId());

        try {
            ResponseEntity<Judge0SubmissionResponse> response = createSubmissionTimer.record(() -> 
                restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Judge0SubmissionResponse.class
                )
            );

            return response.getBody();

        } catch (RestClientException e) {
            failureCounter.increment();
            logger.error("Failed to create synchronous submission on Judge0: {}", e.getMessage());
            throw new RuntimeException("Failed to communicate with Judge0 API", e);
        }
    }

    /**
     * Retrieve submission result from Judge0 by token.
     * 
     * @param token The submission token returned by createSubmission
     * @return Judge0 response with execution results
     * @throws RestClientException if the API call fails
     */
    public Judge0SubmissionResponse getSubmission(String token) {
        String url = judge0Config.getUrl() + "/submissions/" + token + "?base64_encoded=false";

        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        logger.debug("Retrieving submission from Judge0: token={}", token);

        try {
            ResponseEntity<Judge0SubmissionResponse> response = getSubmissionTimer.record(() -> 
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Judge0SubmissionResponse.class
                )
            );

            Judge0SubmissionResponse result = response.getBody();
            if (result != null) {
                Integer statusId = result.getStatus() != null ? result.getStatus().getId() : null;
                logger.debug("Retrieved submission: token={}, status_id={}", token, statusId);
                return result;
            } else {
                throw new RuntimeException("Judge0 response is null");
            }

        } catch (RestClientException e) {
            failureCounter.increment();
            logger.error("Failed to retrieve submission from Judge0: token={}, error={}", 
                    token, e.getMessage());
            throw new RuntimeException("Failed to retrieve submission from Judge0", e);
        }
    }

    /**
     * Create HTTP headers for Judge0 API requests.
     * Includes RapidAPI authentication if configured.
     * 
     * @return HTTP headers with Content-Type and authentication
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Add RapidAPI headers if using RapidAPI Judge0
        if (judge0Config.getKey() != null && !judge0Config.getKey().isEmpty() 
                && !judge0Config.getKey().equals("YOUR_RAPIDAPI_KEY_HERE")) {
            headers.set("X-RapidAPI-Key", judge0Config.getKey());
            headers.set("X-RapidAPI-Host", judge0Config.getHost());
        }

        return headers;
    }

    /**
     * Check if a submission is still processing.
     * 
     * @param response The Judge0 submission response
     * @return true if status is In Queue (1) or Processing (2)
     */
    public boolean isProcessing(Judge0SubmissionResponse response) {
        if (response == null || response.getStatus() == null) {
            return false;
        }

        Integer statusId = response.getStatus().getId();
        // Status 1 = In Queue, Status 2 = Processing
        return statusId != null && (statusId == 1 || statusId == 2);
    }

    /**
     * Check if a submission completed successfully.
     * 
     * @param response The Judge0 submission response
     * @return true if status is Accepted (3)
     */
    public boolean isAccepted(Judge0SubmissionResponse response) {
        if (response == null || response.getStatus() == null) {
            return false;
        }

        Integer statusId = response.getStatus().getId();
        return statusId != null && statusId == 3;
    }

    /**
     * Check if a submission had a compilation error.
     * 
     * @param response The Judge0 submission response
     * @return true if status is Compilation Error (6)
     */
    public boolean isCompilationError(Judge0SubmissionResponse response) {
        if (response == null || response.getStatus() == null) {
            return false;
        }

        Integer statusId = response.getStatus().getId();
        return statusId != null && statusId == 6;
    }

    /**
     * Check if a submission had a runtime error.
     * 
     * @param response The Judge0 submission response
     * @return true if status is any runtime error (7-12)
     */
    public boolean isRuntimeError(Judge0SubmissionResponse response) {
        if (response == null || response.getStatus() == null) {
            return false;
        }

        Integer statusId = response.getStatus().getId();
        return statusId != null && statusId >= 7 && statusId <= 12;
    }
}
