package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Achievement;
import lombok.Data;

@Data
public class AchievementDTO {
    private Long id;
    private String title;
    private String description;
    private String icon;
    private Long companyId;

    public static AchievementDTO fromEntity(Achievement achievement) {
        AchievementDTO dto = new AchievementDTO();
        dto.setId(achievement.getId());
        dto.setTitle(achievement.getTitle());
        dto.setDescription(achievement.getDescription());
        dto.setIcon(achievement.getIcon());
        if (achievement.getCompany() != null) {
            dto.setCompanyId(achievement.getCompany().getId());
        }
        return dto;
    }
} 