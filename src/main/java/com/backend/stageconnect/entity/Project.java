package com.backend.stageconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "company_projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Description is required")
    @Column(nullable = false, length = 1000)
    private String description;
    
    // Store tags as a comma-separated string
    @Column(name = "tags_list", length = 500)
    private String tagsList;
    
    // Transient field to handle tags as a list in the application
    @Transient
    private List<String> tags = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnoreProperties({"projects", "password", "enabled", "userType"})
    private Company company;

    // Helper method to set up bidirectional relationship
    public void setCompany(Company company) {
        this.company = company;
        if (company != null && !company.getProjects().contains(this)) {
            company.getProjects().add(this);
        }
    }
    
    // Convert list to string before persisting
    @PrePersist
    @PreUpdate
    public void prepareTags() {
        if (tags != null && !tags.isEmpty()) {
            this.tagsList = String.join(",", tags);
        } else {
            this.tagsList = "";
        }
    }
    
    // Convert string to list after loading
    @PostLoad
    public void loadTags() {
        if (tagsList != null && !tagsList.isEmpty()) {
            this.tags = Arrays.asList(tagsList.split(","));
        } else {
            this.tags = new ArrayList<>();
        }
    }
    
    // Getter and setter for tags that interact with the database field
    public List<String> getTags() {
        if (tags == null) {
            loadTags();
        }
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
        prepareTags();
    }
} 