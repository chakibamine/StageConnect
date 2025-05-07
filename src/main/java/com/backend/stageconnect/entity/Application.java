package com.backend.stageconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;
    
    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;
    
    @Column(name = "resume_url")
    private String resumeUrl;
    
    @ElementCollection
    @CollectionTable(name = "application_questions", 
                     joinColumns = @JoinColumn(name = "application_id"))
    @MapKeyColumn(name = "question_key")
    @Column(name = "answer", columnDefinition = "TEXT")
    private Map<String, String> questionAnswers = new HashMap<>();
    
    @Column(name = "available_start_date")
    private String availableStartDate;
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
    
    @Column(name = "interview_date")
    private String interviewDate;
    
    @Column(name = "interview_time")
    private String interviewTime;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Create an enum for the application status
    public enum ApplicationStatus {
        PENDING,
        REVIEWING,
        SHORTLISTED,
        INTERVIEW_SCHEDULED,
        INTERVIEW_COMPLETED,
        OFFERED,
        ACCEPTED,
        REJECTED,
        WITHDRAWN
    }
} 