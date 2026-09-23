package com.codeassess.backend.service;

import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.assessment.AssessmentAttempt;
import com.codeassess.backend.assessment.AssessmentAttemptStatus;
import com.codeassess.backend.assessment.AssessmentStatus;
import com.codeassess.backend.assessment.dto.AssessmentAttemptResponse;
import com.codeassess.backend.exception.BusinessException;
import com.codeassess.backend.exception.ResourceNotFoundException;
import com.codeassess.backend.model.User;
import com.codeassess.backend.repository.AssessmentAttemptRepository;
import com.codeassess.backend.repository.AssessmentRepository;
import com.codeassess.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AssessmentAttemptService {

    private final AssessmentAttemptRepository attemptRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;

    public AssessmentAttemptService(AssessmentAttemptRepository attemptRepository, AssessmentRepository assessmentRepository, UserRepository userRepository) {
        this.attemptRepository = attemptRepository;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
    }

    public AssessmentAttemptResponse startAttempt(Long assessmentId, Long candidateId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

        if (assessment.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new BusinessException("Cannot start an unpublished assessment");
        }

        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        AssessmentAttempt attempt = attemptRepository.findByAssessmentIdAndCandidateId(assessmentId, candidateId)
                .orElse(null);

        if (attempt == null) {
            // Create new attempt
            attempt = new AssessmentAttempt();
            attempt.setAssessment(assessment);
            attempt.setCandidate(candidate);
            attempt = attemptRepository.save(attempt);
        } else {
            // Handle existing attempt
            if (attempt.getStatus() == AssessmentAttemptStatus.COMPLETED) {
                throw new BusinessException("You have already completed this assessment");
            } else if (attempt.getStatus() == AssessmentAttemptStatus.EXPIRED) {
                // Delete expired attempt and create a new one
                attemptRepository.delete(attempt);
                attempt = new AssessmentAttempt();
                attempt.setAssessment(assessment);
                attempt.setCandidate(candidate);
                attempt = attemptRepository.save(attempt);
            } else if (attempt.getExpiresAt() != null && attempt.getExpiresAt().isBefore(LocalDateTime.now())) {
                // Mark as expired, delete it, and create a new one
                attemptRepository.delete(attempt);
                attempt = new AssessmentAttempt();
                attempt.setAssessment(assessment);
                attempt.setCandidate(candidate);
                attempt = attemptRepository.save(attempt);
            }
            // Else: IN_PROGRESS and not expired - continue with existing attempt
        }

        return new AssessmentAttemptResponse(
                attempt.getId(),
                attempt.getCandidate().getId(),
                attempt.getAssessment().getId(),
                attempt.getStartedAt(),
                attempt.getExpiresAt(),
                attempt.getStatus()
        );
    }
    
    public AssessmentAttemptResponse getAttempt(Long assessmentId, Long candidateId) {
        AssessmentAttempt attempt = attemptRepository.findByAssessmentIdAndCandidateId(assessmentId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment attempt not found"));

        if (attempt.getStatus() == AssessmentAttemptStatus.IN_PROGRESS 
                && attempt.getExpiresAt() != null 
                && attempt.getExpiresAt().isBefore(LocalDateTime.now())) {
             attempt.setStatus(AssessmentAttemptStatus.EXPIRED);
             attempt = attemptRepository.save(attempt);
        }

        return new AssessmentAttemptResponse(
                attempt.getId(),
                attempt.getCandidate().getId(),
                attempt.getAssessment().getId(),
                attempt.getStartedAt(),
                attempt.getExpiresAt(),
                attempt.getStatus()
        );
    }
}
