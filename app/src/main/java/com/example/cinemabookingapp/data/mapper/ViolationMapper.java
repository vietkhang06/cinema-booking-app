package com.example.cinemabookingapp.data.mapper;

import com.example.cinemabookingapp.data.dto.ViolationDTO;
import com.example.cinemabookingapp.domain.model.Violation;

public final class ViolationMapper {
    private ViolationMapper() {
    }

    public static Violation toDomain(ViolationDTO dto) {
        if (dto == null) return null;
        Violation model = new Violation();
        model.id = dto.id;
        model.staffId = dto.staffId;
        model.staffName = dto.staffName;
        model.violationType = dto.violationType;
        model.description = dto.description;
        model.severity = dto.severity;
        model.createdAt = dto.createdAt;
        model.createdBy = dto.createdBy;
        model.createdByName = dto.createdByName;
        model.status = dto.status;
        model.penaltyAmount = dto.penaltyAmount;
        model.penaltyPoints = dto.penaltyPoints;
        model.notes = dto.notes;
        model.deleted = dto.deleted;
        return model;
    }

    public static ViolationDTO toDTO(Violation model) {
        if (model == null) return null;
        ViolationDTO dto = new ViolationDTO();
        dto.id = model.id;
        dto.staffId = model.staffId;
        dto.staffName = model.staffName;
        dto.violationType = model.violationType;
        dto.description = model.description;
        dto.severity = model.severity;
        dto.createdAt = model.createdAt;
        dto.createdBy = model.createdBy;
        dto.createdByName = model.createdByName;
        dto.status = model.status;
        dto.penaltyAmount = model.penaltyAmount;
        dto.penaltyPoints = model.penaltyPoints;
        dto.notes = model.notes;
        dto.deleted = model.deleted;
        return dto;
    }
}
