package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.CertificationDTO;
import com.backend.stageconnect.entity.Candidate;
import com.backend.stageconnect.entity.Certification;
import com.backend.stageconnect.repository.CandidateRepository;
import com.backend.stageconnect.repository.CertificationRepository;
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
@RequestMapping("/api/candidates/{candidateId}/certifications")
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"},
    allowCredentials = "true",
    allowedHeaders = {"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers", "x-cors-debug"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}
)
public class CertificationController {

    @Autowired
    private CertificationRepository certificationRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @GetMapping
    public ResponseEntity<List<CertificationDTO>> getAllCertifications(@PathVariable Long candidateId) {
        if (!candidateRepository.existsById(candidateId)) {
            return ResponseEntity.notFound().build();
        }
        List<Certification> certifications = certificationRepository.findByCandidateId(candidateId);
        List<CertificationDTO> certificationDTOs = certifications.stream()
            .map(CertificationDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(certificationDTOs);
    }

    @GetMapping("/{certificationId}")
    public ResponseEntity<CertificationDTO> getCertification(
            @PathVariable Long candidateId,
            @PathVariable Long certificationId) {
        return certificationRepository.findById(certificationId)
                .map(certification -> {
                    if (!certification.getCandidate().getId().equals(candidateId)) {
                        return ResponseEntity.notFound().<CertificationDTO>build();
                    }
                    return ResponseEntity.ok(CertificationDTO.fromEntity(certification));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCertification(
            @PathVariable Long candidateId,
            @Valid @RequestBody Certification certification,
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

            return candidateRepository.findById(candidateId)
                    .map(candidate -> {
                        try {
                            // Create new certification instance
                            Certification newCertification = new Certification();
                            newCertification.setName(certification.getName());
                            newCertification.setIssuer(certification.getIssuer());
                            newCertification.setDate(certification.getDate());
                            newCertification.setCredentialId(certification.getCredentialId());
                            newCertification.setUrl(certification.getUrl());
                            newCertification.setCandidate(candidate);

                            // Save the certification
                            Certification saved = certificationRepository.save(newCertification);
                            
                            return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Certification created successfully",
                                "data", CertificationDTO.fromEntity(saved)
                            ));
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create certification: " + e.getMessage());
                        }
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create certification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{certificationId}")
    public ResponseEntity<?> updateCertification(
            @PathVariable Long candidateId,
            @PathVariable Long certificationId,
            @Valid @RequestBody Certification certificationDetails,
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

            if (!candidateRepository.existsById(candidateId)) {
                return ResponseEntity.notFound().build();
            }

            return certificationRepository.findById(certificationId)
                    .map(certification -> {
                        if (!certification.getCandidate().getId().equals(candidateId)) {
                            return ResponseEntity.notFound().build();
                        }
                        certification.setName(certificationDetails.getName());
                        certification.setIssuer(certificationDetails.getIssuer());
                        certification.setDate(certificationDetails.getDate());
                        certification.setCredentialId(certificationDetails.getCredentialId());
                        certification.setUrl(certificationDetails.getUrl());
                        Certification updated = certificationRepository.save(certification);
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Certification updated successfully",
                            "data", CertificationDTO.fromEntity(updated)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update certification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{certificationId}")
    public ResponseEntity<?> deleteCertification(
            @PathVariable Long candidateId,
            @PathVariable Long certificationId) {
        try {
            return certificationRepository.findById(certificationId)
                    .map(certification -> {
                        if (!certification.getCandidate().getId().equals(candidateId)) {
                            return ResponseEntity.notFound().build();
                        }
                        certificationRepository.delete(certification);
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Certification deleted successfully"
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete certification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 