package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Project;
import com.backend.stageconnect.entity.Company;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long companyId;
    private String companyName;
    private List<Long> internshipIds;

    // Default constructor
    public ProjectDTO() {}

    // Constructor with Project entity
    public ProjectDTO(Project project) {
        this.id = project.getId();
        this.title = project.getTitle();
        this.description = project.getDescription();
        this.startDate = project.getStartDate();
        this.endDate = project.getEndDate();
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
        
        Company company = project.getCompany();
        if (company != null) {
            this.companyId = company.getId();
            this.companyName = company.getName();
        }
        
        this.internshipIds = project.getInternships().stream()
            .map(internship -> internship.getId())
            .collect(Collectors.toList());
    }

    // Static method to convert Project to ProjectDTO
    public static ProjectDTO fromEntity(Project project) {
        return new ProjectDTO(project);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public List<Long> getInternshipIds() {
        return internshipIds;
    }

    public void setInternshipIds(List<Long> internshipIds) {
        this.internshipIds = internshipIds;
    }
} 