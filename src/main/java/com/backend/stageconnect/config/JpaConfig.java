package com.backend.stageconnect.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = {"com.backend.stageconnect.entity"})
@EnableJpaRepositories(basePackages = {"com.backend.stageconnect.repository"})
public class JpaConfig {
    // Configuration class to explicitly scan for entities and repositories
} 