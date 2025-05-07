package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Internship;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, Object> company;

    public static InternshipDTO fromEntity(Internship internship) {
        InternshipDTO dto = new InternshipDTO();
        dto.setId(internship.getId());
        dto.setTitle(internship.getTitle());
        dto.setDepartment(internship.getDepartment());
        dto.setLocation(internship.getLocation());
        dto.setWorkType(internship.getWorkType());
        dto.setDuration(internship.getDuration());
        dto.setCompensation(internship.getCompensation());
        dto.setStatus(internship.getStatus());
        dto.setPostedDate(internship.getPostedDate());
        dto.setDeadline(internship.getDeadline());
        dto.setApplicantsCount(internship.getApplicantsCount());
        
        if (internship.getCompany() != null) {
            Map<String, Object> companyMap = new HashMap<>();
            companyMap.put("id", internship.getCompany().getId());
            companyMap.put("name", internship.getCompany().getName());
            dto.setCompany(companyMap);
        } else {
            Map<String, Object> defaultCompany = new HashMap<>();
            defaultCompany.put("id", 0);
            defaultCompany.put("name", "Unknown Company");
            dto.setCompany(defaultCompany);
        }
        
        return dto;
    }
} 