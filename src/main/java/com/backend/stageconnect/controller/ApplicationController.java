package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.ApplicationDTO;
import com.backend.stageconnect.entity.Application;
import com.backend.stageconnect.entity.Internship;
import com.backend.stageconnect.entity.User;
import com.backend.stageconnect.entity.Candidate;
import com.backend.stageconnect.repository.ApplicationRepository;
import com.backend.stageconnect.repository.InternshipRepository;
import com.backend.stageconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"},
    allowCredentials = "true",
    allowedHeaders = {"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers", "x-cors-debug"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class ApplicationController {

    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private InternshipRepository internshipRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Submit an application for an internship
    @PostMapping("/internships/{internshipId}")
    public ResponseEntity<?> submitApplication(
            @PathVariable Long internshipId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // Extract user ID from request body
            Long userId = Long.valueOf(requestBody.get("user_id").toString());
            
            // Check if the internship exists
            Internship internship = internshipRepository.findById(internshipId)
                    .orElse(null);
            if (internship == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Internship not found with ID: " + internshipId
                ));
            }
            
            // Check if the user exists
            User user = userRepository.findById(userId)
                    .orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            // Check if the user has already applied to this internship
            if (applicationRepository.existsByInternshipIdAndApplicantId(internshipId, userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "You have already applied to this internship"
                ));
            }
            
            // Create new application
            Application application = new Application();
            application.setInternship(internship);
            application.setApplicant(user);
            application.setStatus(Application.ApplicationStatus.PENDING);
            
            // Set cover letter if provided
            if (requestBody.containsKey("coverLetter")) {
                application.setCoverLetter((String) requestBody.get("coverLetter"));
            }
            
            // Set resume URL if provided
            if (requestBody.containsKey("resumeUrl")) {
                application.setResumeUrl((String) requestBody.get("resumeUrl"));
            }
            
            // Set available start date if provided
            if (requestBody.containsKey("availableStartDate")) {
                application.setAvailableStartDate((String) requestBody.get("availableStartDate"));
            }
            
            // Set question answers if provided
            if (requestBody.containsKey("questionAnswers")) {
                @SuppressWarnings("unchecked")
                Map<String, String> answers = (Map<String, String>) requestBody.get("questionAnswers");
                application.setQuestionAnswers(answers);
            }
            
            // Save the application
            Application savedApplication = applicationRepository.save(application);
            
            // Update the internship applicant count
            internship.setApplicantsCount(internship.getApplicantsCount() + 1);
            internshipRepository.save(internship);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Application submitted successfully",
                "data", ApplicationDTO.fromEntityForApplicant(savedApplication)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to submit application: " + e.getMessage()
            ));
        }
    }
    
    // Get applications for a specific internship (for company/admin)
    @GetMapping("/internships/{internshipId}")
    public ResponseEntity<?> getInternshipApplications(
            @PathVariable Long internshipId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        try {
            // Check if the internship exists
            if (!internshipRepository.existsById(internshipId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Internship not found with ID: " + internshipId
                ));
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Application> applicationsPage;
            
            // Filter by status if provided
            if (status != null && !status.isEmpty()) {
                try {
                    Application.ApplicationStatus statusEnum = Application.ApplicationStatus.valueOf(status.toUpperCase());
                    applicationsPage = applicationRepository.findByInternshipIdAndStatus(internshipId, statusEnum, pageable);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid status value: " + status
                    ));
                }
            } else {
                applicationsPage = applicationRepository.findByInternshipId(internshipId, pageable);
            }
            
            List<ApplicationDTO> applications = applicationsPage.getContent().stream()
                    .map(application -> {
                        ApplicationDTO dto = ApplicationDTO.fromEntity(application);
                        
                        // Add detailed applicant information for employer view
                        if (application.getApplicant() instanceof Candidate) {
                            Candidate candidate = (Candidate) application.getApplicant();
                            
                            // Create a map with additional applicant details
                            Map<String, Object> applicantDetails = new HashMap<>();
                            applicantDetails.put("id", candidate.getId());
                            applicantDetails.put("firstName", candidate.getFirstName());
                            applicantDetails.put("lastName", candidate.getLastName());
                            applicantDetails.put("email", candidate.getEmail());
                            applicantDetails.put("phone", candidate.getPhone());
                            applicantDetails.put("photo", candidate.getPhoto());
                            applicantDetails.put("location", candidate.getLocation());
                            applicantDetails.put("title", candidate.getTitle());
                            applicantDetails.put("university", candidate.getCompanyOrUniversity());
                            applicantDetails.put("about", candidate.getAbout());
                            applicantDetails.put("website", candidate.getWebsite());
                            
                            // Add education, experience, and certification counts if available
                            if (candidate.getEducation() != null) {
                                applicantDetails.put("educationCount", candidate.getEducation().size());
                            }
                            if (candidate.getExperiences() != null) {
                                applicantDetails.put("experienceCount", candidate.getExperiences().size());
                            }
                            if (candidate.getCertifications() != null) {
                                applicantDetails.put("certificationsCount", candidate.getCertifications().size());
                            }
                            
                            // Add the applicant details to the DTO
                            dto.setApplicant(applicantDetails);
                        }
                        
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", applications);
            response.put("currentPage", applicationsPage.getNumber());
            response.put("totalItems", applicationsPage.getTotalElements());
            response.put("totalPages", applicationsPage.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch applications: " + e.getMessage()
            ));
        }
    }
    
    // Get applications submitted by a user (for user/applicant)
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserApplications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            // Check if the user exists
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Application> applicationsPage = applicationRepository.findByApplicantId(userId, pageable);
            
            List<ApplicationDTO> applications = applicationsPage.getContent().stream()
                    .map(ApplicationDTO::fromEntityForApplicant)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("applications", applications);
            response.put("currentPage", applicationsPage.getNumber());
            response.put("totalItems", applicationsPage.getTotalElements());
            response.put("totalPages", applicationsPage.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch applications: " + e.getMessage()
            ));
        }
    }
    
    // Get a specific application
    @GetMapping("/{applicationId}")
    public ResponseEntity<?> getApplication(@PathVariable Long applicationId) {
        try {
            return applicationRepository.findById(applicationId)
                    .map(application -> {
                        ApplicationDTO dto = ApplicationDTO.fromEntity(application);
                        
                        // Add detailed applicant information for employer view
                        if (application.getApplicant() instanceof Candidate) {
                            Candidate candidate = (Candidate) application.getApplicant();
                            
                            // Create a map with additional applicant details
                            Map<String, Object> applicantDetails = new HashMap<>();
                            applicantDetails.put("id", candidate.getId());
                            applicantDetails.put("firstName", candidate.getFirstName());
                            applicantDetails.put("lastName", candidate.getLastName());
                            applicantDetails.put("email", candidate.getEmail());
                            applicantDetails.put("phone", candidate.getPhone());
                            applicantDetails.put("photo", candidate.getPhoto());
                            applicantDetails.put("location", candidate.getLocation());
                            applicantDetails.put("title", candidate.getTitle());
                            applicantDetails.put("university", candidate.getCompanyOrUniversity());
                            applicantDetails.put("about", candidate.getAbout());
                            applicantDetails.put("website", candidate.getWebsite());
                            
                            // Add education, experience, and certification counts if available
                            if (candidate.getEducation() != null) {
                                applicantDetails.put("educationCount", candidate.getEducation().size());
                            }
                            if (candidate.getExperiences() != null) {
                                applicantDetails.put("experienceCount", candidate.getExperiences().size());
                            }
                            if (candidate.getCertifications() != null) {
                                applicantDetails.put("certificationsCount", candidate.getCertifications().size());
                            }
                            
                            // Add the applicant details to the DTO
                            dto.setApplicant(applicantDetails);
                        }
                        
                        return ResponseEntity.ok(dto);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch application: " + e.getMessage()
            ));
        }
    }
    
    // Update application status (for company/admin)
    @PutMapping("/{applicationId}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> requestBody) {
        
        try {
            String status = requestBody.get("status");
            if (status == null || status.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Status is required"
                ));
            }
            
            Application.ApplicationStatus newStatus;
            try {
                newStatus = Application.ApplicationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid status value: " + status
                ));
            }
            
            return applicationRepository.findById(applicationId)
                    .map(application -> {
                        application.setStatus(newStatus);
                        
                        // If feedback is provided, update it
                        if (requestBody.containsKey("feedback") && requestBody.get("feedback") != null) {
                            application.setFeedback(requestBody.get("feedback"));
                        }
                        
                        // If interview date is provided, update it (especially when status is INTERVIEW_SCHEDULED)
                        if (requestBody.containsKey("interviewDate") && requestBody.get("interviewDate") != null) {
                            application.setInterviewDate(requestBody.get("interviewDate"));
                        }
                        
                        // If interview time is provided, update it
                        if (requestBody.containsKey("interviewTime") && requestBody.get("interviewTime") != null) {
                            application.setInterviewTime(requestBody.get("interviewTime"));
                        }
                        
                        // For INTERVIEW_SCHEDULED status, ensure interview date and time are set
                        if (newStatus == Application.ApplicationStatus.INTERVIEW_SCHEDULED) {
                            // If no interview date/time was provided but status is interview, add default values
                            if (application.getInterviewDate() == null) {
                                application.setInterviewDate("To be determined");
                            }
                            if (application.getInterviewTime() == null) {
                                application.setInterviewTime("To be determined");
                            }
                        }
                        
                        Application updatedApplication = applicationRepository.save(application);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Application status updated successfully",
                            "data", ApplicationDTO.fromEntity(updatedApplication)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to update application status: " + e.getMessage()
            ));
        }
    }
    
    // Delete an application (withdraw)
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long applicationId) {
        try {
            return applicationRepository.findById(applicationId)
                    .map(application -> {
                        // Get the internship to update its applicant count
                        Internship internship = application.getInternship();
                        
                        // Delete the application
                        applicationRepository.delete(application);
                        
                        // Update internship applicant count
                        if (internship != null) {
                            internship.setApplicantsCount(Math.max(0, internship.getApplicantsCount() - 1));
                            internshipRepository.save(internship);
                        }
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Application withdrawn successfully"
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to withdraw application: " + e.getMessage()
            ));
        }
    }
}