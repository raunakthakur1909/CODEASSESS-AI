package com.codeassess.backend.ai;

import com.codeassess.backend.repository.CodeReviewRepository;
import com.codeassess.backend.service.CodeReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeReviewWorkerTest {

    @Mock
    private CodeReviewRepository codeReviewRepository;

    @Mock
    private CodeReviewService codeReviewService;

    @Mock
    private CodeReview review;

    @Test
    void shouldDelegatePendingReviewsToService() {
        when(review.getId()).thenReturn(9L);
        when(codeReviewRepository.findByStatusOrderByCreatedAtAsc(ReviewStatus.PENDING))
                .thenReturn(List.of(review));

        CodeReviewWorker worker = new CodeReviewWorker(codeReviewRepository, codeReviewService);
        worker.triggerProcessing();

        verify(codeReviewService).processReview(9L);
    }
}
