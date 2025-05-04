package com.backend.stageconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "education")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Education {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Degree is required")
    @Column(nullable = false)
    private String degree;
    
    @NotBlank(message = "Institution is required")
    @Column(nullable = false)
    private String institution;
    
    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private String startDate;
    
    @Column(name = "end_date")
    private String endDate;
    
    @Column(length = 1000)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @JsonIgnoreProperties({"education", "password", "enabled", "userType"})
    private Candidate candidate;
} 