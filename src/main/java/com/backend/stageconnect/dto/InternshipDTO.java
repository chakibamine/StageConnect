package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Internship;
import lombok.Data;

@Data
public class InternshipDTO {
    private Long id;
    private String title;
    private String department;
    private String location;
    private String workType;
    private String duration;
    private String compensation;
    private Integer applicantsCount;
    private String status;
    private String postedDate;
    private String deadline;
    private Long companyId;

    public static InternshipDTO fromEntity(Internship internship) {
        InternshipDTO dto = new InternshipDTO();
        dto.setId(internship.getId());
        dto.setTitle(internship.getTitle());
        dto.setDepartment(internship.getDepartment());
        dto.setLocation(internship.getLocation());
        dto.setWorkType(internship.getWorkType());
        dto.setDuration(internship.getDuration());
        dto.setCompensation(internship.getCompensation());
        dto.setApplicantsCount(internship.getApplicantsCount());
        dto.setStatus(internship.getStatus());
        dto.setPostedDate(internship.getPostedDate());
        dto.setDeadline(internship.getDeadline());
        if (internship.getCompany() != null) {
            dto.setCompanyId(internship.getCompany().getId());
        }
        return dto;
    }
} 