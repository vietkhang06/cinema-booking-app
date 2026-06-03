package com.example.cinemabookingapp.data.mapper;

import com.example.cinemabookingapp.data.dto.AttendanceDTO;
import com.example.cinemabookingapp.domain.model.Attendance;

public final class AttendanceMapper {
    private AttendanceMapper() {
    }

    public static Attendance toDomain(AttendanceDTO dto) {
        if (dto == null) return null;
        Attendance model = new Attendance();
        model.id = dto.id;
        model.staffId = dto.staffId;
        model.staffName = dto.staffName;
        model.cinemaId = dto.cinemaId;
        model.cinemaName = dto.cinemaName;
        model.shiftName = dto.shiftName;
        model.date = dto.date;
        model.checkInTime = dto.checkInTime;
        model.checkOutTime = dto.checkOutTime;
        model.durationMinutes = dto.durationMinutes;
        model.lateMinutes = dto.lateMinutes;
        model.earlyLeaveMinutes = dto.earlyLeaveMinutes;
        model.status = dto.status;
        model.notes = dto.notes;
        return model;
    }

    public static AttendanceDTO toDTO(Attendance model) {
        if (model == null) return null;
        AttendanceDTO dto = new AttendanceDTO();
        dto.id = model.id;
        dto.staffId = model.staffId;
        dto.staffName = model.staffName;
        dto.cinemaId = model.cinemaId;
        dto.cinemaName = model.cinemaName;
        dto.shiftName = model.shiftName;
        dto.date = model.date;
        dto.checkInTime = model.checkInTime;
        dto.checkOutTime = model.checkOutTime;
        dto.durationMinutes = model.durationMinutes;
        dto.lateMinutes = model.lateMinutes;
        dto.earlyLeaveMinutes = model.earlyLeaveMinutes;
        dto.status = model.status;
        dto.notes = model.notes;
        return dto;
    }
}
