package com.codeassess.backend.repository;

import com.codeassess.backend.assessment.AssessmentAttempt;
import com.codeassess.backend.assessment.AssessmentAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {

    Optional<AssessmentAttempt> findByAssessmentIdAndCandidateId(Long assessmentId, Long candidateId);
    
    List<AssessmentAttempt> findByStatusAndExpiresAtBefore(AssessmentAttemptStatus status, LocalDateTime expiresAt);
}
