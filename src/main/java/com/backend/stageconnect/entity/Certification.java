package com.backend.stageconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "certifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Certification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Issuer is required")
    @Column(nullable = false)
    private String issuer;
    
    @Column(name = "issue_date")
    private String date;
    
    @Column(name = "credential_id")
    private String credentialId;
    
    @Column
    private String url;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @JsonIgnoreProperties({"certifications", "password", "enabled", "userType"})
    private Candidate candidate;

    // Helper method to set up bidirectional relationship
    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
        if (candidate != null && !candidate.getCertifications().contains(this)) {
            candidate.getCertifications().add(this);
        }
    }
} 