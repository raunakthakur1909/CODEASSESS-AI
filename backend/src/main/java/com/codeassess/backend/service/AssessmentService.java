package com.codeassess.backend.service;

import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.assessment.AssessmentStatus;
import com.codeassess.backend.assessment.dto.AssessmentResponse;
import com.codeassess.backend.assessment.dto.CreateAssessmentRequest;
import com.codeassess.backend.assessment.dto.UpdateAssessmentRequest;
import com.codeassess.backend.exception.BusinessException;
import com.codeassess.backend.exception.ForbiddenException;
import com.codeassess.backend.exception.ResourceNotFoundException;
import com.codeassess.backend.model.User;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.repository.AssessmentRepository;
import com.codeassess.backend.repository.ProblemRepository;
import com.codeassess.backend.repository.TestCaseRepository;
import com.codeassess.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public AssessmentService(
            AssessmentRepository assessmentRepository,
            UserRepository userRepository,
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository
    ) {
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    public AssessmentResponse createAssessment(
            CreateAssessmentRequest request,
            Long creatorId
    ) {

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Creator not found")
                );

        Assessment assessment = new Assessment();

        assessment.setTitle(request.getTitle().trim());
        assessment.setDescription(request.getDescription().trim());
        if (request.getInstructions() != null) {
            assessment.setInstructions(request.getInstructions().trim());
        }
        assessment.setDurationMinutes(request.getDurationMinutes());

        // New assessments start with zero marks.
        // Total marks will be calculated from problems later.
        assessment.setTotalMarks(0);

        assessment.setCreatedBy(creator);
        assessment.setStatus(AssessmentStatus.DRAFT);

        Assessment savedAssessment =
                assessmentRepository.save(assessment);

        return AssessmentResponse.fromEntity(savedAssessment);
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAllAssessments(Long userId, boolean isAdmin) {

        return assessmentRepository.findAll()
                .stream()
                .filter(assessment -> isAdmin || assessment.getStatus() == AssessmentStatus.PUBLISHED)
                .map(AssessmentResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessmentById(Long id, Long userId, boolean isAdmin) {

        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Assessment not found")
                );

        if (!isAdmin && assessment.getStatus() == AssessmentStatus.DRAFT) {
            throw new ForbiddenException("You do not have permission to view this assessment");
        }

        return AssessmentResponse.fromEntity(assessment);
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAssessmentsByCreator(
            Long creatorId
    ) {

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Creator not found")
                );

        return assessmentRepository.findByCreatedBy(creator)
                .stream()
                .map(AssessmentResponse::fromEntity)
                .toList();
    }

    public AssessmentResponse updateAssessment(
            Long assessmentId,
            UpdateAssessmentRequest request,
            Long creatorId
    ) {

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Assessment not found")
                );

        validateOwnership(assessment, creatorId);

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BusinessException(
                    "Only draft assessments can be updated"
            );
        }

        assessment.setTitle(request.getTitle().trim());
        assessment.setDescription(request.getDescription().trim());
        if (request.getInstructions() != null) {
            assessment.setInstructions(request.getInstructions().trim());
        } else {
            assessment.setInstructions(null);
        }
        assessment.setDurationMinutes(request.getDurationMinutes());

        Assessment updatedAssessment =
                assessmentRepository.save(assessment);

        return AssessmentResponse.fromEntity(updatedAssessment);
    }

    public void deleteAssessment(
            Long assessmentId,
            Long creatorId
    ) {

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Assessment not found")
                );

        validateOwnership(assessment, creatorId);

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BusinessException(
                    "Only draft assessments can be deleted"
            );
        }

        assessmentRepository.delete(assessment);
    }

    public AssessmentResponse publishAssessment(
            Long assessmentId,
            Long creatorId
    ) {

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Assessment not found"
                        )
                );

        validateOwnership(assessment, creatorId);

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BusinessException(
                    "Only draft assessments can be published"
            );
        }

        // Get all problems for this assessment
        List<Problem> problems =
                problemRepository.findByAssessmentOrderByIdAsc(assessment);

        if (problems.isEmpty()) {
            throw new BusinessException(
                    "Assessment must contain at least one problem"
            );
        }

        int totalMarks = 0;

        // Validate each problem has test cases and hidden test cases
        for (Problem problem : problems) {

            totalMarks += problem.getPoints();

            long testCaseCount =
                    testCaseRepository.countByProblem(problem);

            if (testCaseCount == 0) {
                throw new BusinessException(
                        "Problem '" + problem.getTitle()
                                + "' must contain at least one test case"
                );
            }

            long hiddenTestCaseCount =
                    testCaseRepository.countByProblemAndHiddenTrue(problem);

            if (hiddenTestCaseCount == 0) {
                throw new BusinessException(
                        "Problem '" + problem.getTitle()
                                + "' must contain at least one hidden test case"
                );
            }
        }

        if (totalMarks <= 0) {
            throw new BusinessException(
                    "Assessment total marks must be greater than zero"
            );
        }

        // Set the authoritative total marks and publish
        assessment.setTotalMarks(totalMarks);
        assessment.setStatus(AssessmentStatus.PUBLISHED);

        Assessment publishedAssessment =
                assessmentRepository.save(assessment);

        return AssessmentResponse.fromEntity(publishedAssessment);
    }

    public AssessmentResponse duplicateAssessment(
            Long assessmentId,
            Long creatorId
    ) {

        Assessment original = assessmentRepository.findById(assessmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Assessment not found")
                );

        validateOwnership(original, creatorId);

        Assessment copy = new Assessment();
        copy.setTitle(original.getTitle() + " (Copy)");
        copy.setDescription(original.getDescription());
        copy.setInstructions(original.getInstructions());
        copy.setDurationMinutes(original.getDurationMinutes());
        copy.setTotalMarks(original.getTotalMarks());
        copy.setCreatedBy(original.getCreatedBy());
        copy.setStatus(AssessmentStatus.DRAFT);
        
        Assessment savedCopy = assessmentRepository.save(copy);
        
        // Deep copy problems
        List<Problem> originalProblems = problemRepository.findByAssessmentOrderByIdAsc(original);
        for (Problem op : originalProblems) {
            Problem pCopy = new Problem();
            pCopy.setAssessment(savedCopy);
            pCopy.setTitle(op.getTitle());
            pCopy.setDescription(op.getDescription());
            pCopy.setDifficulty(op.getDifficulty());
            pCopy.setPoints(op.getPoints());
            pCopy.setInputFormat(op.getInputFormat());
            pCopy.setOutputFormat(op.getOutputFormat());
            pCopy.setConstraints(op.getConstraints());
            pCopy.setStarterCode(op.getStarterCode());
            pCopy.setStarterLanguage(op.getStarterLanguage());

            Problem savedPCopy = problemRepository.save(pCopy);
            
            // Deep copy test cases
            List<com.codeassess.backend.testcase.TestCase> originalTestCases = testCaseRepository.findByProblemOrderByIdAsc(op);
            for (com.codeassess.backend.testcase.TestCase otc : originalTestCases) {
                com.codeassess.backend.testcase.TestCase tcCopy = new com.codeassess.backend.testcase.TestCase();
                tcCopy.setProblem(savedPCopy);
                tcCopy.setInput(otc.getInput());
                tcCopy.setExpectedOutput(otc.getExpectedOutput());
                tcCopy.setHidden(otc.isHidden());
                tcCopy.setTimeLimitMs(otc.getTimeLimitMs());
                tcCopy.setMemoryLimitMb(otc.getMemoryLimitMb());
                testCaseRepository.save(tcCopy);
            }
        }
        
        return AssessmentResponse.fromEntity(savedCopy);
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
}
