package com.codeassess.backend.service;

import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.assessment.AssessmentStatus;
import com.codeassess.backend.draft.Draft;
import com.codeassess.backend.draft.dto.DraftResponse;
import com.codeassess.backend.draft.dto.SaveDraftRequest;
import com.codeassess.backend.exception.BusinessException;
import com.codeassess.backend.exception.ForbiddenException;
import com.codeassess.backend.exception.ResourceNotFoundException;
import com.codeassess.backend.model.Role;
import com.codeassess.backend.model.User;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.repository.AssessmentRepository;
import com.codeassess.backend.repository.DraftRepository;
import com.codeassess.backend.repository.ProblemRepository;
import com.codeassess.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class DraftService {

    private final DraftRepository draftRepository;
    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final ProblemRepository problemRepository;

    public DraftService(
            DraftRepository draftRepository,
            UserRepository userRepository,
            AssessmentRepository assessmentRepository,
            ProblemRepository problemRepository
    ) {
        this.draftRepository = draftRepository;
        this.userRepository = userRepository;
        this.assessmentRepository = assessmentRepository;
        this.problemRepository = problemRepository;
    }

    public DraftResponse saveDraft(
            Long assessmentId,
            Long problemId,
            SaveDraftRequest request,
            Long candidateId
    ) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        if (candidate.getRole() != Role.CANDIDATE) {
            throw new ForbiddenException("Only candidates can save drafts");
        }

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

        if (assessment.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new BusinessException("Drafts can only be saved for published assessments");
        }

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        if (!problem.getAssessment().getId().equals(assessment.getId())) {
            throw new BusinessException("Problem does not belong to the assessment");
        }

        Optional<Draft> existingDraft = draftRepository.findByCandidateAndAssessmentAndProblem(candidate, assessment, problem);
        Draft draft;

        if (existingDraft.isPresent()) {
            draft = existingDraft.get();
            draft.setLanguage(request.getLanguage());
            draft.setSourceCode(request.getSourceCode());
        } else {
            draft = new Draft();
            draft.setCandidate(candidate);
            draft.setAssessment(assessment);
            draft.setProblem(problem);
            draft.setLanguage(request.getLanguage());
            draft.setSourceCode(request.getSourceCode());
        }

        Draft savedDraft = draftRepository.save(draft);

        return DraftResponse.fromEntity(savedDraft);
    }

    @Transactional(readOnly = true)
    public DraftResponse getDraft(Long assessmentId, Long problemId, Long candidateId) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        Optional<Draft> draft = draftRepository.findByCandidateAndAssessmentAndProblem(candidate, assessment, problem);

        return draft.map(DraftResponse::fromEntity).orElse(null);
    }
}
