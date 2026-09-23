package com.codeassess.backend.repository;

import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.assessment.AssessmentStatus;
import com.codeassess.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    java.util.Optional<Assessment> findByTitle(String title);

    List<Assessment> findByCreatedBy(User createdBy);

    List<Assessment> findByStatus(AssessmentStatus status);

    List<Assessment> findByCreatedByAndStatus(
            User createdBy,
            AssessmentStatus status
    );
}
