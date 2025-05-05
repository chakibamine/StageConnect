package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Project;
import lombok.Data;

import java.util.List;

@Data
public class ProjectDTO {
    private Long id;
    private String title;
    private String description;
    private List<String> tags;
    private Long companyId;

    public static ProjectDTO fromEntity(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setTags(project.getTags());
        if (project.getCompany() != null) {
            dto.setCompanyId(project.getCompany().getId());
        }
        return dto;
    }
} 