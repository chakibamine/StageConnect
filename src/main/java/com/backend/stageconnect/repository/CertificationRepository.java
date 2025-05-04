package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByCandidateId(Long candidateId);
} 