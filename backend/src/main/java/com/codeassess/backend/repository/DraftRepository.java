package com.codeassess.backend.repository;

import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.draft.Draft;
import com.codeassess.backend.model.User;
import com.codeassess.backend.problem.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DraftRepository extends JpaRepository<Draft, Long> {

    Optional<Draft> findByCandidateAndAssessmentAndProblem(User candidate, Assessment assessment, Problem problem);

    void deleteByCandidateAndAssessment(User candidate, Assessment assessment);

}
