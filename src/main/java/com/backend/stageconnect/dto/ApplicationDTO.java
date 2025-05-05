package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Application;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {
    private Long id;
    private Long internshipId;
    private String internshipTitle;
    private String companyName;
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;
    private String applicantPhoto;
    private String status;
    private String coverLetter;
    private String resumeUrl;
    private Map<String, String> questionAnswers;
    private String availableStartDate;
    private String feedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for company view
    private String applicantPhoneNumber;
    
    public static ApplicationDTO fromEntity(Application application) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(application.getId());
        
        // Set internship info
        if (application.getInternship() != null) {
            dto.setInternshipId(application.getInternship().getId());
            dto.setInternshipTitle(application.getInternship().getTitle());
            
            // Set company info if available
            if (application.getInternship().getCompany() != null) {
                dto.setCompanyName(application.getInternship().getCompany().getName());
            }
        }
        
        // Set applicant info
        if (application.getApplicant() != null) {
            dto.setApplicantId(application.getApplicant().getId());
            dto.setApplicantName(application.getApplicant().getFirstName() + " " + application.getApplicant().getLastName());
            dto.setApplicantEmail(application.getApplicant().getEmail());
            
            // Handle candidate-specific fields like photo and phone
            if (application.getApplicant() instanceof com.backend.stageconnect.entity.Candidate) {
                com.backend.stageconnect.entity.Candidate candidate = 
                    (com.backend.stageconnect.entity.Candidate) application.getApplicant();
                dto.setApplicantPhoto(candidate.getPhoto());
                dto.setApplicantPhoneNumber(candidate.getPhone());
            }
        }
        
        dto.setStatus(application.getStatus().name());
        dto.setCoverLetter(application.getCoverLetter());
        dto.setResumeUrl(application.getResumeUrl());
        dto.setQuestionAnswers(application.getQuestionAnswers());
        dto.setAvailableStartDate(application.getAvailableStartDate());
        dto.setFeedback(application.getFeedback());
        dto.setCreatedAt(application.getCreatedAt());
        dto.setUpdatedAt(application.getUpdatedAt());
        
        return dto;
    }
    
    // Simplified DTO for applicant's view (hides feedback and internal fields)
    public static ApplicationDTO fromEntityForApplicant(Application application) {
        ApplicationDTO dto = fromEntity(application);
        dto.setFeedback(null); // Hide feedback until it's shared with applicant
        return dto;
    }
} 