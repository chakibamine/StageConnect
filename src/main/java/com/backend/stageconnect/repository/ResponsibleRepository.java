package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.Responsible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResponsibleRepository extends JpaRepository<Responsible, Long> {
    Optional<Responsible> findByEmail(String email);
    List<Responsible> findByCompanyId(Long companyId);
} 