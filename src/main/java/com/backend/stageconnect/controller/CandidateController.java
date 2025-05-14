package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.EducationDTO;
import com.backend.stageconnect.dto.CertificationDTO;
import com.backend.stageconnect.dto.ExperienceDTO;
import com.backend.stageconnect.dto.CandidateDTO;
import com.backend.stageconnect.entity.Candidate;
import com.backend.stageconnect.entity.UserType;
import com.backend.stageconnect.repository.CandidateRepository;
import com.backend.stageconnect.repository.UserRepository;
import com.backend.stageconnect.security.JwtService;
import com.backend.stageconnect.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {
    private static final Logger logger = LoggerFactory.getLogger(CandidateController.class);

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;

    @Autowired
    private FileStorageService fileStorageService;
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Candidate candidate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(candidate.getEmail())) {
                response.put("success", false);
                response.put("message", "Email already in use");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Set user type and encrypt password
            candidate.setUserType(UserType.CANDIDATE);
            candidate.setPassword(passwordEncoder.encode(candidate.getPassword()));
            candidate.setEnabled(true);
            
            // Save candidate
            Candidate savedCandidate = candidateRepository.save(candidate);
            
            // Generate JWT token
            String jwtToken = jwtService.generateToken(savedCandidate);
            
            response.put("success", true);
            response.put("message", "Candidate registered successfully");
            response.put("token", jwtToken);
            response.put("id", savedCandidate.getId());
            response.put("firstName", savedCandidate.getFirstName());
            response.put("lastName", savedCandidate.getLastName());
            response.put("email", savedCandidate.getEmail());
            response.put("userType", savedCandidate.getUserType());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            if (email == null || password == null) {
                response.put("success", false);
                response.put("message", "Email and password are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Find candidate by email
            Optional<Candidate> candidateOpt = candidateRepository.findByEmail(email);
            if (candidateOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Candidate candidate = candidateOpt.get();
            
            // Check password
            if (!passwordEncoder.matches(password, candidate.getPassword())) {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Generate JWT token
            String jwtToken = jwtService.generateToken(candidate);
            
            // Successful login
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", jwtToken);
            response.put("id", candidate.getId());
            response.put("firstName", candidate.getFirstName());
            response.put("lastName", candidate.getLastName());
            response.put("email", candidate.getEmail());
            response.put("userType", candidate.getUserType());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<?> getCandidateById(@PathVariable Long id) {
        try {
            Optional<Candidate> candidateOpt = candidateRepository.findById(id);
            if (candidateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Candidate candidate = candidateOpt.get();
            
            // Ensure password is preserved during updates
            String existingPassword = candidate.getPassword();
            
            // Convert experiences to DTOs
            List<ExperienceDTO> experienceDTOs = candidate.getExperiences().stream()
                .map(ExperienceDTO::fromEntity)
                .collect(Collectors.toList());
            
            // Create CandidateDTO
            CandidateDTO candidateDTO = CandidateDTO.fromEntity(candidate);
            candidateDTO.setExperiences(experienceDTOs);
            
            // Restore password before saving
            candidate.setPassword(existingPassword);
            
            return ResponseEntity.ok(candidateDTO);
        } catch (Exception e) {
            logger.error("Error processing candidate data: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing candidate data: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCandidates() {
        var candidates = candidateRepository.findAll();
        var response = candidates.stream().map(candidate -> {
            Map<String, Object> candidateMap = new HashMap<>();
            candidate.setPassword(null); // Don't return password
            
            candidateMap.put("id", candidate.getId());
            candidateMap.put("firstName", candidate.getFirstName());
            candidateMap.put("lastName", candidate.getLastName());
            candidateMap.put("email", candidate.getEmail());
            candidateMap.put("phone", candidate.getPhone());
            candidateMap.put("location", candidate.getLocation());
            candidateMap.put("title", candidate.getTitle());
            candidateMap.put("website", candidate.getWebsite());
            candidateMap.put("companyOrUniversity", candidate.getCompanyOrUniversity());
            candidateMap.put("about", candidate.getAbout());
            
            // Add complete photo URL if photo exists
            if (candidate.getPhoto() != null) {
                candidateMap.put("photo", baseUrl + candidate.getPhoto());
            }
            
            return candidateMap;
        }).toList();
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCandidate(
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("data") Candidate updatedCandidate) {
        
        Optional<Candidate> candidateOpt = candidateRepository.findById(id);
        if (candidateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Candidate candidate = candidateOpt.get();
        
        // Update fields except password
        candidate.setFirstName(updatedCandidate.getFirstName());
        candidate.setLastName(updatedCandidate.getLastName());
        candidate.setEmail(updatedCandidate.getEmail());
        candidate.setPhone(updatedCandidate.getPhone());
        candidate.setLocation(updatedCandidate.getLocation());
        candidate.setTitle(updatedCandidate.getTitle());
        candidate.setWebsite(updatedCandidate.getWebsite());
        candidate.setCompanyOrUniversity(updatedCandidate.getCompanyOrUniversity());
        candidate.setAbout(updatedCandidate.getAbout());
        
        // Handle file upload if provided
        if (file != null && !file.isEmpty()) {
            try {
                // Delete old file if exists
                if (candidate.getPhoto() != null) {
                    fileStorageService.deleteFile(candidate.getPhoto());
                }
                // Save new file
                String filePath = fileStorageService.saveFile(file);
                candidate.setPhoto(filePath);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
            }
        }
        
        // Save and return updated candidate without password
        Candidate saved = candidateRepository.save(candidate);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCandidate(@PathVariable Long id) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(id);
        if (candidateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Candidate candidate = candidateOpt.get();
        
        // Delete photo file if exists
        if (candidate.getPhoto() != null) {
            try {
                fileStorageService.deleteFile(candidate.getPhoto());
            } catch (IOException e) {
                // Log error but continue with deletion
                System.err.println("Failed to delete photo file: " + e.getMessage());
            }
        }

        candidateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/profile")
    @Transactional
    public ResponseEntity<?> getCandidateFullProfile(@PathVariable Long id) {
        try {
            Optional<Candidate> candidateOpt = candidateRepository.findById(id);
            if (candidateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Candidate candidate = candidateOpt.get();
            
            // Create response map
            Map<String, Object> response = new HashMap<>();
            
            // Add basic candidate info
            response.put("id", candidate.getId());
            response.put("firstName", candidate.getFirstName());
            response.put("lastName", candidate.getLastName());
            response.put("email", candidate.getEmail());
            response.put("phone", candidate.getPhone());
            response.put("location", candidate.getLocation());
            response.put("title", candidate.getTitle());
            response.put("website", candidate.getWebsite());
            response.put("companyOrUniversity", candidate.getCompanyOrUniversity());
            response.put("about", candidate.getAbout());
            
            // Add photo URL if exists
            if (candidate.getPhoto() != null) {
                response.put("photo", baseUrl + candidate.getPhoto());
            }
            
            // Add education
            List<Map<String, Object>> educationList = candidate.getEducation().stream()
                .map(education -> {
                    Map<String, Object> eduMap = new HashMap<>();
                    eduMap.put("id", education.getId());
                    eduMap.put("degree", education.getDegree());
                    eduMap.put("institution", education.getInstitution());
                    eduMap.put("startDate", education.getStartDate());
                    eduMap.put("endDate", education.getEndDate());
                    eduMap.put("description", education.getDescription());
                    return eduMap;
                })
                .collect(Collectors.toList());
            response.put("education", educationList);
            
            // Add certifications
            List<Map<String, Object>> certificationList = candidate.getCertifications().stream()
                .map(certification -> {
                    Map<String, Object> certMap = new HashMap<>();
                    certMap.put("id", certification.getId());
                    certMap.put("name", certification.getName());
                    certMap.put("issuer", certification.getIssuer());
                    certMap.put("date", certification.getDate());
                    certMap.put("credentialId", certification.getCredentialId());
                    certMap.put("url", certification.getUrl());
                    return certMap;
                })
                .collect(Collectors.toList());
            response.put("certifications", certificationList);
            
            // Add experiences
            List<Map<String, Object>> experienceList = candidate.getExperiences().stream()
                .map(experience -> {
                    Map<String, Object> expMap = new HashMap<>();
                    expMap.put("id", experience.getId());
                    expMap.put("title", experience.getTitle());
                    expMap.put("company", experience.getCompany());
                    expMap.put("location", experience.getLocation());
                    expMap.put("startDate", experience.getStartDate());
                    expMap.put("endDate", experience.getEndDate());
                    expMap.put("description", experience.getDescription());
                    return expMap;
                })
                .collect(Collectors.toList());
            response.put("experiences", experienceList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching candidate full profile: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "message", "Error fetching candidate full profile: " + e.getMessage()
                    ));
        }
    }
} 