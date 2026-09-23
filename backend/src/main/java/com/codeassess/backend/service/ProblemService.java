package com.codeassess.backend.service;

import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.assessment.AssessmentStatus;
import com.codeassess.backend.exception.BusinessException;
import com.codeassess.backend.exception.ForbiddenException;
import com.codeassess.backend.exception.ResourceNotFoundException;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.problem.dto.CreateProblemRequest;
import com.codeassess.backend.problem.dto.ProblemResponse;
import com.codeassess.backend.problem.dto.UpdateProblemRequest;
import com.codeassess.backend.repository.AssessmentRepository;
import com.codeassess.backend.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final AssessmentRepository assessmentRepository;

    public ProblemService(
            ProblemRepository problemRepository,
            AssessmentRepository assessmentRepository
    ) {
        this.problemRepository = problemRepository;
        this.assessmentRepository = assessmentRepository;
    }

    public ProblemResponse createProblem(
            Long assessmentId,
            CreateProblemRequest request,
            Long creatorId
    ) {

        Assessment assessment = getAssessment(assessmentId);

        validateOwnership(assessment, creatorId);
        validateDraft(assessment);

        Problem problem = new Problem();

        problem.setAssessment(assessment);
        problem.setTitle(request.getTitle().trim());
        problem.setDescription(request.getDescription().trim());
        problem.setDifficulty(request.getDifficulty());
        problem.setPoints(request.getPoints());

        problem.setInputFormat(trimNullable(request.getInputFormat()));
        problem.setOutputFormat(trimNullable(request.getOutputFormat()));
        problem.setConstraints(trimNullable(request.getConstraints()));
        problem.setStarterCode(trimNullable(request.getStarterCode()));
        problem.setStarterLanguage(request.getStarterLanguage());

        Problem savedProblem = problemRepository.save(problem);

        recalculateTotalMarks(assessment);

        return ProblemResponse.fromEntity(savedProblem);
    }

    @Transactional(readOnly = true)
    public List<ProblemResponse> getProblemsByAssessment(
            Long assessmentId,
            boolean isAdmin
    ) {

        Assessment assessment = getAssessment(assessmentId);
        
        if (!isAdmin && assessment.getStatus() == AssessmentStatus.DRAFT) {
            throw new ForbiddenException("You do not have permission to view problems for this assessment");
        }

        return problemRepository
                .findByAssessmentOrderByIdAsc(assessment)
                .stream()
                .map(ProblemResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProblemResponse getProblemById(Long problemId, boolean isAdmin) {

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Problem not found"
                        )
                );

        if (!isAdmin && problem.getAssessment().getStatus() == AssessmentStatus.DRAFT) {
            throw new ForbiddenException("You do not have permission to view this problem");
        }

        return ProblemResponse.fromEntity(problem);
    }

    public ProblemResponse updateProblem(
            Long problemId,
            UpdateProblemRequest request,
            Long creatorId
    ) {

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Problem not found"
                        )
                );

        Assessment assessment = problem.getAssessment();

        validateOwnership(assessment, creatorId);
        validateDraft(assessment);

        problem.setTitle(request.getTitle().trim());
        problem.setDescription(request.getDescription().trim());
        problem.setDifficulty(request.getDifficulty());
        problem.setPoints(request.getPoints());

        problem.setInputFormat(trimNullable(request.getInputFormat()));
        problem.setOutputFormat(trimNullable(request.getOutputFormat()));
        problem.setConstraints(trimNullable(request.getConstraints()));
        problem.setStarterCode(trimNullable(request.getStarterCode()));
        problem.setStarterLanguage(request.getStarterLanguage());

        Problem updatedProblem = problemRepository.save(problem);

        recalculateTotalMarks(assessment);

        return ProblemResponse.fromEntity(updatedProblem);
    }

    public void deleteProblem(
            Long problemId,
            Long creatorId
    ) {

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Problem not found"
                        )
                );

        Assessment assessment = problem.getAssessment();

        validateOwnership(assessment, creatorId);
        validateDraft(assessment);

        problemRepository.delete(problem);

        /*
         * Recalculate after deletion so the assessment's
         * total marks remain accurate.
         */
        recalculateTotalMarks(assessment);
    }

    private Assessment getAssessment(Long assessmentId) {

        return assessmentRepository.findById(assessmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Assessment not found"
                        )
                );
    }

    private void validateOwnership(
            Assessment assessment,
            Long creatorId
    ) {

        if (!assessment.getCreatedBy().getId().equals(creatorId)) {
            throw new ForbiddenException(
                    "You do not have permission to modify this assessment"
            );
        }
    }

    private void validateDraft(Assessment assessment) {

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BusinessException(
                    "Problems can only be modified while the assessment is in DRAFT status"
            );
        }
    }

    private void recalculateTotalMarks(Assessment assessment) {

        int totalMarks =
                problemRepository.sumPointsByAssessment(assessment);

        assessment.setTotalMarks(totalMarks);

        assessmentRepository.save(assessment);
    }

    private String trimNullable(String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}
