package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.InternshipDTO;
import com.backend.stageconnect.entity.Company;
import com.backend.stageconnect.entity.Internship;
import com.backend.stageconnect.repository.CompanyRepository;
import com.backend.stageconnect.repository.InternshipRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies/{companyId}/internships")
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"},
    allowCredentials = "true",
    allowedHeaders = {"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers", "x-cors-debug"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}
)
public class InternshipController {

    @Autowired
    private InternshipRepository internshipRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping
    public ResponseEntity<List<InternshipDTO>> getAllInternships(
            @PathVariable Long companyId,
            @RequestParam(required = false) String status) {
        if (!companyRepository.existsById(companyId)) {
            return ResponseEntity.notFound().build();
        }
        
        List<Internship> internships;
        if (status != null) {
            internships = internshipRepository.findByCompanyIdAndStatus(companyId, status);
        } else {
            internships = internshipRepository.findByCompanyId(companyId);
        }
        
        List<InternshipDTO> internshipDTOs = internships.stream()
            .map(InternshipDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(internshipDTOs);
    }

    @GetMapping("/{internshipId}")
    public ResponseEntity<InternshipDTO> getInternship(
            @PathVariable Long companyId,
            @PathVariable Long internshipId) {
        return internshipRepository.findById(internshipId)
                .map(internship -> {
                    if (!internship.getCompany().getId().equals(companyId)) {
                        return ResponseEntity.notFound().<InternshipDTO>build();
                    }
                    return ResponseEntity.ok(InternshipDTO.fromEntity(internship));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createInternship(
            @PathVariable Long companyId,
            @Valid @RequestBody Internship internship,
            BindingResult bindingResult) {
        try {
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

            return companyRepository.findById(companyId)
                    .map(company -> {
                        try {
                            // Create new internship instance
                            Internship newInternship = new Internship();
                            newInternship.setTitle(internship.getTitle());
                            newInternship.setDepartment(internship.getDepartment());
                            newInternship.setLocation(internship.getLocation());
                            newInternship.setWorkType(internship.getWorkType());
                            newInternship.setDuration(internship.getDuration());
                            newInternship.setCompensation(internship.getCompensation());
                            newInternship.setStatus("active");
                            newInternship.setPostedDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                            newInternship.setDeadline(internship.getDeadline());
                            newInternship.setCompany(company);

                            // Save the internship
                            Internship saved = internshipRepository.save(newInternship);
                            
                            return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Internship created successfully",
                                "data", InternshipDTO.fromEntity(saved)
                            ));
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create internship: " + e.getMessage());
                        }
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create internship: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{internshipId}")
    public ResponseEntity<?> updateInternship(
            @PathVariable Long companyId,
            @PathVariable Long internshipId,
            @Valid @RequestBody Internship internshipDetails,
            BindingResult bindingResult) {
        try {
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

            if (!companyRepository.existsById(companyId)) {
                return ResponseEntity.notFound().build();
            }

            return internshipRepository.findById(internshipId)
                    .map(internship -> {
                        if (!internship.getCompany().getId().equals(companyId)) {
                            return ResponseEntity.notFound().build();
                        }
                        internship.setTitle(internshipDetails.getTitle());
                        internship.setDepartment(internshipDetails.getDepartment());
                        internship.setLocation(internshipDetails.getLocation());
                        internship.setWorkType(internshipDetails.getWorkType());
                        internship.setDuration(internshipDetails.getDuration());
                        internship.setCompensation(internshipDetails.getCompensation());
                        internship.setDeadline(internshipDetails.getDeadline());
                        internship.setStatus(internshipDetails.getStatus());
                        
                        Internship updated = internshipRepository.save(internship);
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Internship updated successfully",
                            "data", InternshipDTO.fromEntity(updated)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update internship: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{internshipId}")
    public ResponseEntity<?> deleteInternship(
            @PathVariable Long companyId,
            @PathVariable Long internshipId) {
        try {
            return internshipRepository.findById(internshipId)
                    .map(internship -> {
                        if (!internship.getCompany().getId().equals(companyId)) {
                            return ResponseEntity.notFound().build();
                        }
                        internshipRepository.delete(internship);
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Internship deleted successfully"
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete internship: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PatchMapping("/{internshipId}/status")
    public ResponseEntity<?> updateInternshipStatus(
            @PathVariable Long companyId,
            @PathVariable Long internshipId,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            if (newStatus == null || (!newStatus.equals("active") && !newStatus.equals("closed"))) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid status value. Must be 'active' or 'closed'"
                ));
            }

            return internshipRepository.findById(internshipId)
                    .map(internship -> {
                        if (!internship.getCompany().getId().equals(companyId)) {
                            return ResponseEntity.notFound().build();
                        }
                        internship.setStatus(newStatus);
                        Internship updated = internshipRepository.save(internship);
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Internship status updated successfully",
                            "data", InternshipDTO.fromEntity(updated)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update internship status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 