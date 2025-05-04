package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Experience;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ExperienceDTO {
    private Long id;
    private String title;
    private String company;
    private String location;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private String description;
    private Long candidateId;

    public static ExperienceDTO fromEntity(Experience experience) {
        ExperienceDTO dto = new ExperienceDTO();
        dto.setId(experience.getId());
        dto.setTitle(experience.getTitle());
        dto.setCompany(experience.getCompany());
        dto.setLocation(experience.getLocation());
        dto.setStartDate(experience.getStartDate());
        dto.setEndDate(experience.getEndDate());
        dto.setDescription(experience.getDescription());
        if (experience.getCandidate() != null) {
            dto.setCandidateId(experience.getCandidate().getId());
        }
        return dto;
    }
} 