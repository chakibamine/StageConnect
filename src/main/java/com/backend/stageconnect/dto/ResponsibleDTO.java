package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Responsible;
import com.backend.stageconnect.entity.UserType;
import lombok.Data;

@Data
public class ResponsibleDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;
    private String department;
    private UserType userType;

    public static ResponsibleDTO fromEntity(Responsible responsible) {
        ResponsibleDTO dto = new ResponsibleDTO();
        dto.setId(responsible.getId());
        dto.setFirstName(responsible.getFirstName());
        dto.setLastName(responsible.getLastName());
        dto.setEmail(responsible.getEmail());
        dto.setPhone(responsible.getPhone());
        dto.setPosition(responsible.getPosition());
        dto.setDepartment(responsible.getDepartment());
        dto.setUserType(responsible.getUserType());
        return dto;
    }
} 