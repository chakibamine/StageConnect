package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.Internship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {
    Page<Internship> findByCompanyId(Long companyId, Pageable pageable);
    Page<Internship> findByCompanyIdAndStatus(Long companyId, String status, Pageable pageable);
    List<Internship> findByStatus(String status);
    boolean existsByCompanyIdAndTitle(Long companyId, String title);
} 