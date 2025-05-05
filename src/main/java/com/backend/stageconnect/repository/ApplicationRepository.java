package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    // Find all applications for a specific internship
    Page<Application> findByInternshipId(Long internshipId, Pageable pageable);
    
    // Find all applications by a specific user
    Page<Application> findByApplicantId(Long applicantId, Pageable pageable);
    
    // Find a specific application by internship ID and applicant ID
    Optional<Application> findByInternshipIdAndApplicantId(Long internshipId, Long applicantId);
    
    // Check if a user has already applied to an internship
    boolean existsByInternshipIdAndApplicantId(Long internshipId, Long applicantId);
    
    // Count applications for an internship
    long countByInternshipId(Long internshipId);
    
    // Count applications by status for an internship
    long countByInternshipIdAndStatus(Long internshipId, Application.ApplicationStatus status);
    
    // Find applications by status
    Page<Application> findByStatus(Application.ApplicationStatus status, Pageable pageable);
    
    // Find applications by internship ID and status
    Page<Application> findByInternshipIdAndStatus(Long internshipId, Application.ApplicationStatus status, Pageable pageable);
    
    // Find recent applications
    @Query("SELECT a FROM Application a ORDER BY a.createdAt DESC")
    Page<Application> findRecentApplications(Pageable pageable);
} 