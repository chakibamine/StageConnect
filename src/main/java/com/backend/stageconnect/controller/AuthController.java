package com.backend.stageconnect.controller;

import com.backend.stageconnect.entity.Candidate;
import com.backend.stageconnect.entity.Company;
import com.backend.stageconnect.entity.Responsible;
import com.backend.stageconnect.entity.User;
import com.backend.stageconnect.entity.UserType;
import com.backend.stageconnect.repository.CandidateRepository;
import com.backend.stageconnect.repository.CompanyRepository;
import com.backend.stageconnect.repository.ResponsibleRepository;
import com.backend.stageconnect.repository.UserRepository;
import com.backend.stageconnect.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private ResponsibleRepository responsibleRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Creates a response with the structure for a Candidate entity that will be modified
     * to look like a RESPONSIBLE entity to maintain frontend compatibility
     */
    private Map<String, Object> createResponsibleResponse(Responsible responsible, String jwtToken) {
        Map<String, Object> response = new HashMap<>();
        
        // Create a candidate-like profile for responsible
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", responsible.getId());
        profile.put("firstName", responsible.getFirstName());
        profile.put("lastName", responsible.getLastName());
        profile.put("email", responsible.getEmail());
        profile.put("password", responsible.getPassword());
        profile.put("enabled", responsible.isEnabled());
        profile.put("userType", "RESPONSIBLE"); // Keep the real user type
        
        // Add null fields that exist in Candidate but not in Responsible
        profile.put("photo", null);
        profile.put("phone", null);
        profile.put("location", null);
        profile.put("title", null);
        profile.put("university", null);
        profile.put("about", null);
        
        // Set the standard response fields
        response.put("success", true);
        response.put("message", "Login successful");
        response.put("token", jwtToken);
        response.put("id", responsible.getId());
        response.put("firstName", responsible.getFirstName());
        response.put("lastName", responsible.getLastName());
        response.put("email", responsible.getEmail());
        response.put("userType", "employer"); // Keep employer for top-level
        response.put("profile", profile);
        
        return response;
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userType = (String) request.get("role");
            
            if (userType == null) {
                response.put("success", false);
                response.put("message", "User type is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            String firstName = (String) request.get("firstName");
            String lastName = (String) request.get("lastName");
            
            // Validate basic fields
            if (email == null || password == null || firstName == null || lastName == null) {
                response.put("success", false);
                response.put("message", "Email, password, firstName, and lastName are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(email)) {
                response.put("success", false);
                response.put("message", "Email already in use");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Handle different user types
            if (userType.equalsIgnoreCase("student")) {
                // Create candidate entity
                Candidate candidate = Candidate.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .userType(UserType.CANDIDATE)
                        .enabled(true)
                        .build();
                
                // Save candidate
                Candidate savedCandidate = candidateRepository.save(candidate);
                
                // Generate JWT token
                String jwtToken = jwtService.generateToken(savedCandidate);
                
                response.put("success", true);
                response.put("message", "Student registered successfully");
                response.put("token", jwtToken);
                response.put("id", savedCandidate.getId());
                response.put("firstName", savedCandidate.getFirstName());
                response.put("lastName", savedCandidate.getLastName());
                response.put("email", savedCandidate.getEmail());
                response.put("userType", "student"); // frontend role
                response.put("profile", savedCandidate);
                
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
                
            } else if (userType.equalsIgnoreCase("employer")) {
                // For employers, we need to check if company exists or create a new one
                String companyName = (String) request.get("companyName");
                
                if (companyName == null || companyName.isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Company name is required for employer registration");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // Try to find company by name or create a new one
                Optional<Company> companyOpt = companyRepository.findByName(companyName);
                Company company;
                
                if (companyOpt.isEmpty()) {
                    company = Company.builder()
                            .name(companyName)
                            .build();
                    company = companyRepository.save(company);
                } else {
                    company = companyOpt.get();
                }
                
                // Create responsible entity
                Responsible responsible = Responsible.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .userType(UserType.RESPONSIBLE)
                        .company(company)
                        .enabled(true)
                        .build();
                
                // Save responsible
                Responsible savedResponsible = responsibleRepository.save(responsible);
                
                // Generate JWT token
                String jwtToken = jwtService.generateToken(savedResponsible);
                
                // Create a consistent response format
                Map<String, Object> formattedResponse = createResponsibleResponse(savedResponsible, jwtToken);
                formattedResponse.put("message", "Employer registered successfully");
                
                // Add company info for backward compatibility
                formattedResponse.put("companyId", company.getId());
                formattedResponse.put("companyName", company.getName());
                
                return ResponseEntity.status(HttpStatus.CREATED).body(formattedResponse);
            } else {
                response.put("success", false);
                response.put("message", "Invalid user type: " + userType);
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
            
            // Find user by email
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = userOpt.get();
            
            // Check password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Generate JWT token
            String jwtToken = jwtService.generateToken(user);
            
            // Return user-specific information based on user type
            if (user.getUserType() == UserType.CANDIDATE) {
                Optional<Candidate> candidateOpt = candidateRepository.findByEmail(email);
                if (candidateOpt.isPresent()) {
                    Candidate candidate = candidateOpt.get();
                    response.put("success", true);
                    response.put("message", "Login successful");
                    response.put("token", jwtToken);
                    response.put("id", candidate.getId());
                    response.put("firstName", candidate.getFirstName());
                    response.put("lastName", candidate.getLastName());
                    response.put("email", candidate.getEmail());
                    response.put("userType", "student"); // Map to frontend role
                    response.put("profile", candidate);
                }
            } else if (user.getUserType() == UserType.RESPONSIBLE) {
                Optional<Responsible> responsibleOpt = responsibleRepository.findByEmail(email);
                if (responsibleOpt.isPresent()) {
                    Responsible responsible = responsibleOpt.get();
                    
                    // Use the custom response mapper for responsible users
                    response = createResponsibleResponse(responsible, jwtToken);
                    
                    // Add company info for backward compatibility
                    if (responsible.getCompany() != null) {
                        response.put("companyId", responsible.getCompany().getId());
                        response.put("companyName", responsible.getCompany().getName());
                    }
                }
            } else {
                response.put("success", false);
                response.put("message", "Invalid user type");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestBody(required = false) Map<String, String> requestBody
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // First try to get the token from the Authorization header
            String jwt = null;
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            } 
            // If not in header, try to get it from request body
            else if (requestBody != null && requestBody.containsKey("token")) {
                jwt = requestBody.get("token");
            }
            
            // If no token provided, return unauthorized
            if (jwt == null) {
                response.put("success", false);
                response.put("message", "No valid token provided");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userEmail = jwtService.extractUsername(jwt);
            
            if (userEmail != null) {
                Optional<User> userOpt = userRepository.findByEmail(userEmail);
                
                if (userOpt.isPresent() && jwtService.isTokenValid(jwt, userOpt.get())) {
                    User user = userOpt.get();
                    
                    // Map backend UserType to frontend role consistently
                    if (user.getUserType() == UserType.CANDIDATE) {
                        Optional<Candidate> candidateOpt = candidateRepository.findByEmail(userEmail);
                        if (candidateOpt.isPresent()) {
                            Candidate candidate = candidateOpt.get();
                            
                            response.put("success", true);
                            response.put("message", "Token is valid");
                            response.put("id", candidate.getId());
                            response.put("email", candidate.getEmail());
                            response.put("firstName", candidate.getFirstName());
                            response.put("lastName", candidate.getLastName());
                            response.put("userType", "student");
                            response.put("profile", candidate);
                        }
                    } else if (user.getUserType() == UserType.RESPONSIBLE) {
                        Optional<Responsible> responsibleOpt = responsibleRepository.findByEmail(userEmail);
                        if (responsibleOpt.isPresent()) {
                            Responsible responsible = responsibleOpt.get();
                            
                            // Use the custom response mapper for responsible users
                            response = createResponsibleResponse(responsible, jwt);
                            response.put("message", "Token is valid");
                            
                            // Add company info for backward compatibility
                            if (responsible.getCompany() != null) {
                                response.put("companyId", responsible.getCompany().getId());
                                response.put("companyName", responsible.getCompany().getName());
                            }
                        }
                    } else if (user.getUserType() == UserType.ADMIN) {
                        response.put("success", true);
                        response.put("message", "Token is valid");
                        response.put("id", user.getId());
                        response.put("email", user.getEmail());
                        response.put("firstName", user.getFirstName());
                        response.put("lastName", user.getLastName());
                        response.put("userType", "admin");
                    } else {
                        // Default fallback
                        response.put("success", true);
                        response.put("message", "Token is valid");
                        response.put("id", user.getId());
                        response.put("email", user.getEmail());
                        response.put("firstName", user.getFirstName());
                        response.put("lastName", user.getLastName());
                        response.put("userType", user.getUserType().toString().toLowerCase());
                    }
                    
                    return ResponseEntity.ok(response);
                }
            }
            
            response.put("success", false);
            response.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Token validation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 