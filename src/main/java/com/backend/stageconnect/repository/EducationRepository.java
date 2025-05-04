package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByCandidateId(Long candidateId);
} 