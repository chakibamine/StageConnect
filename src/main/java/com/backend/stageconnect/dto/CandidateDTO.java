package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Candidate;
import com.backend.stageconnect.entity.UserType;
import lombok.Data;
import java.util.List;

@Data
public class CandidateDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String location;
    private String title;
    private String website;
    private String companyOrUniversity;
    private String about;
    private String photo;
    private UserType userType;
    private List<EducationDTO> education;
    private List<CertificationDTO> certifications;
    private List<ExperienceDTO> experiences;

    public static CandidateDTO fromEntity(Candidate candidate) {
        CandidateDTO dto = new CandidateDTO();
        dto.setId(candidate.getId());
        dto.setFirstName(candidate.getFirstName());
        dto.setLastName(candidate.getLastName());
        dto.setEmail(candidate.getEmail());
        dto.setPhone(candidate.getPhone());
        dto.setLocation(candidate.getLocation());
        dto.setTitle(candidate.getTitle());
        dto.setWebsite(candidate.getWebsite());
        dto.setCompanyOrUniversity(candidate.getCompanyOrUniversity());
        dto.setAbout(candidate.getAbout());
        dto.setPhoto(candidate.getPhoto());
        dto.setUserType(candidate.getUserType());
        
        // Convert education list to DTOs
        if (candidate.getEducation() != null) {
            dto.setEducation(candidate.getEducation().stream()
                .map(EducationDTO::fromEntity)
                .toList());
        }
        
        // Convert certification list to DTOs
        if (candidate.getCertifications() != null) {
            dto.setCertifications(candidate.getCertifications().stream()
                .map(CertificationDTO::fromEntity)
                .toList());
        }
        
        return dto;
    }
} 