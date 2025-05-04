package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Education;
import lombok.Data;

@Data
public class EducationDTO {
    private Long id;
    private String degree;
    private String institution;
    private String startDate;
    private String endDate;
    private String description;
    private Long candidateId;

    public static EducationDTO fromEntity(Education education) {
        EducationDTO dto = new EducationDTO();
        dto.setId(education.getId());
        dto.setDegree(education.getDegree());
        dto.setInstitution(education.getInstitution());
        dto.setStartDate(education.getStartDate());
        dto.setEndDate(education.getEndDate());
        dto.setDescription(education.getDescription());
        if (education.getCandidate() != null) {
            dto.setCandidateId(education.getCandidate().getId());
        }
        return dto;
    }
} 