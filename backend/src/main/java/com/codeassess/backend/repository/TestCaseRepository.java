package com.codeassess.backend.repository;

import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.testcase.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    List<TestCase> findByProblemOrderByIdAsc(Problem problem);

    List<TestCase> findByProblemAndHiddenFalseOrderByIdAsc(Problem problem);

    long countByProblem(Problem problem);

    long countByProblemAndHiddenFalse(Problem problem);

    long countByProblemAndHiddenTrue(Problem problem);
}
