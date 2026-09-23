package com.codeassess.backend.ai;

import com.codeassess.backend.repository.CodeReviewRepository;
import com.codeassess.backend.service.CodeReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CodeReviewWorker {

    private static final Logger logger = LoggerFactory.getLogger(CodeReviewWorker.class);
    private static final int BATCH_SIZE = 10;

    private final CodeReviewRepository codeReviewRepository;
    private final CodeReviewService codeReviewService;

    public CodeReviewWorker(
            CodeReviewRepository codeReviewRepository,
            CodeReviewService codeReviewService
    ) {
        this.codeReviewRepository = codeReviewRepository;
        this.codeReviewService = codeReviewService;
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void processQueuedReviews() {
        try {
            List<CodeReview> pendingReviews = codeReviewRepository
                    .findByStatusOrderByCreatedAtAsc(ReviewStatus.PENDING)
                    .stream()
                    .limit(BATCH_SIZE)
                    .toList();

            if (pendingReviews.isEmpty()) {
                logger.debug("No pending AI reviews to process");
                return;
            }

            logger.info("Found {} pending AI review(s) to process", pendingReviews.size());
            for (CodeReview review : pendingReviews) {
                codeReviewService.processReview(review.getId());
            }
        } catch (Exception exception) {
            logger.error("Error in AI review worker", exception);
        }
    }

    public void triggerProcessing() {
        processQueuedReviews();
    }
}