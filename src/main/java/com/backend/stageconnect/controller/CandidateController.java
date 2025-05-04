package com.backend.stageconnect.controller;

import com.backend.stageconnect.entity.Candidate;
import com.backend.stageconnect.entity.UserType;
import com.backend.stageconnect.repository.CandidateRepository;
import com.backend.stageconnect.repository.UserRepository;
import com.backend.stageconnect.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
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
    public ResponseEntity<Candidate> getCandidateById(@PathVariable Long id) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(id);
        if (candidateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Candidate candidate = candidateOpt.get();
        candidate.setPassword(null); // Don't return password
        
        return ResponseEntity.ok(candidate);
    }

    @GetMapping
    public ResponseEntity<?> getAllCandidates() {
        var candidates = candidateRepository.findAll();
        candidates.forEach(c -> c.setPassword(null));
        return ResponseEntity.ok(candidates);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCandidate(@PathVariable Long id, @RequestBody Candidate updatedCandidate) {
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
        candidate.setPhoto(updatedCandidate.getPhoto());
        
        // Save and return updated candidate without password
        Candidate saved = candidateRepository.save(candidate);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCandidate(@PathVariable Long id) {
        if (!candidateRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        candidateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 