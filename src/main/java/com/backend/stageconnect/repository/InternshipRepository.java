package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.Internship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {
    List<Internship> findByCompanyId(Long companyId);
    List<Internship> findByCompanyIdAndStatus(Long companyId, String status);
} 