package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findByCandidateId(Long candidateId);
    void deleteByCandidateIdAndId(Long candidateId, Long id);
} 