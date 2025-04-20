package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);
    Optional<Company> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRegistrationNumber(String registrationNumber);
} 