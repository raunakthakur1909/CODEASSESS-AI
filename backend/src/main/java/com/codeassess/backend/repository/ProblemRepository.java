package com.codeassess.backend.repository;

import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.problem.Difficulty;
import com.codeassess.backend.problem.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findByAssessment(Assessment assessment);

    List<Problem> findByAssessmentOrderByIdAsc(Assessment assessment);

    List<Problem> findByAssessmentAndDifficulty(
            Assessment assessment,
            Difficulty difficulty
    );

    long countByAssessment(Assessment assessment);

    @Query("""
            SELECT COALESCE(SUM(p.points), 0)
            FROM Problem p
            WHERE p.assessment = :assessment
            """)
    int sumPointsByAssessment(
            @Param("assessment") Assessment assessment
    );
}
