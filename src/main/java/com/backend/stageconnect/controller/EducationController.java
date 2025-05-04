package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.EducationDTO;
import com.backend.stageconnect.entity.Candidate;
import com.backend.stageconnect.entity.Education;
import com.backend.stageconnect.repository.CandidateRepository;
import com.backend.stageconnect.repository.EducationRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/candidates/{candidateId}/education")
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"},
    allowCredentials = "true",
    allowedHeaders = {"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers", "x-cors-debug"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}
)
public class EducationController {

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    // Get all education entries for a candidate
    @GetMapping
    public ResponseEntity<List<EducationDTO>> getAllEducation(@PathVariable Long candidateId) {
        if (!candidateRepository.existsById(candidateId)) {
            return ResponseEntity.notFound().build();
        }
        List<Education> educations = educationRepository.findByCandidateId(candidateId);
        List<EducationDTO> educationDTOs = educations.stream()
            .map(EducationDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(educationDTOs);
    }

    // Get a specific education entry
    @GetMapping("/{educationId}")
    public ResponseEntity<EducationDTO> getEducation(
            @PathVariable Long candidateId,
            @PathVariable Long educationId) {
        return educationRepository.findById(educationId)
                .map(education -> {
                    if (!education.getCandidate().getId().equals(candidateId)) {
                        return ResponseEntity.notFound().<EducationDTO>build();
                    }
                    return ResponseEntity.ok(EducationDTO.fromEntity(education));
                })
                .orElse(ResponseEntity.notFound().<EducationDTO>build());
    }

    // Create a new education entry
    @PostMapping
    public ResponseEntity<?> createEducation(
            @PathVariable Long candidateId,
            @Valid @RequestBody Education education,
            BindingResult bindingResult) {
        try {
            // Handle validation errors
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                    ));
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Validation failed",
                    "errors", errors
                ));
            }

            return candidateRepository.findById(candidateId)
                    .map(candidate -> {
                        education.setCandidate(candidate);
                        Education saved = educationRepository.save(education);
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Education created successfully",
                            "data", EducationDTO.fromEntity(saved)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create education: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Update an education entry
    @PutMapping("/{educationId}")
    public ResponseEntity<?> updateEducation(
            @PathVariable Long candidateId,
            @PathVariable Long educationId,
            @Valid @RequestBody Education educationDetails,
            BindingResult bindingResult) {
        try {
            // Handle validation errors
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                    ));
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Validation failed",
                    "errors", errors
                ));
            }

            if (!candidateRepository.existsById(candidateId)) {
                return ResponseEntity.notFound().build();
            }

            return educationRepository.findById(educationId)
                    .map(education -> {
                        if (!education.getCandidate().getId().equals(candidateId)) {
                            return ResponseEntity.notFound().build();
                        }
                        education.setDegree(educationDetails.getDegree());
                        education.setInstitution(educationDetails.getInstitution());
                        education.setStartDate(educationDetails.getStartDate());
                        education.setEndDate(educationDetails.getEndDate());
                        education.setDescription(educationDetails.getDescription());
                        Education updated = educationRepository.save(education);
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Education updated successfully",
                            "data", EducationDTO.fromEntity(updated)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update education: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Delete an education entry
    @DeleteMapping("/{educationId}")
    public ResponseEntity<?> deleteEducation(
            @PathVariable Long candidateId,
            @PathVariable Long educationId) {
        try {
            return educationRepository.findById(educationId)
                    .map(education -> {
                        if (!education.getCandidate().getId().equals(candidateId)) {
                            return ResponseEntity.notFound().build();
                        }
                        educationRepository.delete(education);
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Education deleted successfully"
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete education: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 