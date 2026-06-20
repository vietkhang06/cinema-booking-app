package com.cinemabooking.backend.features.user.service;

import com.cinemabooking.backend.features.user.dto.AttendanceDTO;
import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.cinemabooking.backend.features.user.repository.AttendanceRepository;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ViolationService violationService;

    // Helper to format date in Vietnam local timezone (GMT+7)
    private String getVietnamDateString(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        return sdf.format(new Date(time));
    }

    private String getVietnamTimeString(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        return sdf.format(new Date(time));
    }

    public AttendanceDTO getTodayAttendance(String staffId) throws ExecutionException, InterruptedException {
        String today = getVietnamDateString(System.currentTimeMillis());
        List<QueryDocumentSnapshot> docs = attendanceRepository.findByStaffIdAndDate(staffId, today);
        if (docs.isEmpty()) {
            return null;
        }
        return docs.get(0).toObject(AttendanceDTO.class);
    }

    public AttendanceDTO checkIn(String staffId, String shiftName, String cinemaId, String cinemaName) throws Exception {
        UserDTO staff = userService.getUserById(staffId);
        if (staff == null || !"staff".equalsIgnoreCase(staff.getRole())) {
            throw new IllegalArgumentException("Chỉ nhân viên mới được phép thao tác.");
        }

        if (!"active".equalsIgnoreCase(staff.getStatus())) {
            throw new IllegalStateException("Tài khoản của bạn đã bị vô hiệu hóa, không thể điểm danh!");
        }

        long now = System.currentTimeMillis();
        String todayDate = getVietnamDateString(now);

        List<QueryDocumentSnapshot> existingLogs = attendanceRepository.findByStaffIdAndDate(staffId, todayDate);
        if (!existingLogs.isEmpty()) {
            throw new IllegalStateException("Bạn đã điểm danh check-in ngày hôm nay rồi!");
        }

        // Calculate lateness based on shift
        long lateMinutes = 0;
        String timeStr = getVietnamTimeString(now);
        String[] timeParts = timeStr.split(":");
        int currentHour = Integer.parseInt(timeParts[0]);
        int currentMinute = Integer.parseInt(timeParts[1]);
        int currentTotalMinutes = currentHour * 60 + currentMinute;

        int shiftStartHour = 8;
        if ("Ca Chiều".equalsIgnoreCase(shiftName)) {
            shiftStartHour = 12;
        } else if ("Ca Tối".equalsIgnoreCase(shiftName)) {
            shiftStartHour = 17;
        }
        int shiftStartTotalMinutes = shiftStartHour * 60;

        if (currentTotalMinutes > shiftStartTotalMinutes) {
            lateMinutes = currentTotalMinutes - shiftStartTotalMinutes;
        }

        String status = "present";
        if (lateMinutes > 5) {
            status = "late";
        }

        String docId = "att_" + UUID.randomUUID().toString();
        AttendanceDTO attendance = AttendanceDTO.builder()
                .id(docId)
                .staffId(staffId)
                .staffName(staff.getName() != null ? staff.getName() : staff.getEmail())
                .cinemaId(cinemaId)
                .cinemaName(cinemaName)
                .shiftName(shiftName)
                .date(todayDate)
                .checkInTime(now)
                .checkOutTime(0L)
                .durationMinutes(0L)
                .lateMinutes(lateMinutes)
                .earlyLeaveMinutes(0L)
                .status(status)
                .notes("")
                .build();

        attendanceRepository.save(attendance);

        // Auto-create a violation for being late
        if (lateMinutes > 5) {
            violationService.autoCreateViolation(staffId, staff.getName(), "LATE", 
                    "Đi trễ " + lateMinutes + " phút ở " + shiftName + " (" + todayDate + ")", 
                    50000.0, 1);
        }

        return attendance;
    }

    public AttendanceDTO checkOut(String staffId) throws Exception {
        UserDTO staff = userService.getUserById(staffId);
        if (staff == null || !"staff".equalsIgnoreCase(staff.getRole())) {
            throw new IllegalArgumentException("Chỉ nhân viên mới được phép thao tác.");
        }
        if (!"active".equalsIgnoreCase(staff.getStatus())) {
            throw new IllegalStateException("Tài khoản của bạn đã bị vô hiệu hóa, không thể thao tác!");
        }

        String todayDate = getVietnamDateString(System.currentTimeMillis());

        List<QueryDocumentSnapshot> logs = attendanceRepository.findByStaffIdAndDate(staffId, todayDate);
        if (logs.isEmpty() || logs.get(0).toObject(AttendanceDTO.class).getCheckOutTime() > 0) {
            throw new IllegalStateException("Lỗi: Bạn chưa check-in hoặc đã check-out rồi!");
        }

        DocumentSnapshot doc = logs.get(0);
        AttendanceDTO attendance = doc.toObject(AttendanceDTO.class);
        
        long now = System.currentTimeMillis();
        attendance.setCheckOutTime(now);

        // Compute working duration
        long durationMs = now - attendance.getCheckInTime();
        long durationMinutes = durationMs / (1000 * 60);
        attendance.setDurationMinutes(durationMinutes);

        // Determine early leave
        long earlyLeaveMinutes = 0;
        String timeStr = getVietnamTimeString(now);
        String[] timeParts = timeStr.split(":");
        int currentHour = Integer.parseInt(timeParts[0]);
        int currentMinute = Integer.parseInt(timeParts[1]);
        int currentTotalMinutes = currentHour * 60 + currentMinute;

        int shiftEndHour = 12;
        if ("Ca Chiều".equalsIgnoreCase(attendance.getShiftName())) {
            shiftEndHour = 17;
        } else if ("Ca Tối".equalsIgnoreCase(attendance.getShiftName())) {
            shiftEndHour = 22;
        }
        int shiftEndTotalMinutes = shiftEndHour * 60;

        if (currentTotalMinutes < shiftEndTotalMinutes) {
            earlyLeaveMinutes = shiftEndTotalMinutes - currentTotalMinutes;
        }
        attendance.setEarlyLeaveMinutes(earlyLeaveMinutes);

        // Set final status
        String finalStatus = "completed";
        if (attendance.getLateMinutes() > 5 && earlyLeaveMinutes > 5) {
            finalStatus = "late & early_leave";
        } else if (attendance.getLateMinutes() > 5) {
            finalStatus = "late";
        } else if (earlyLeaveMinutes > 5) {
            finalStatus = "early_leave";
        }
        attendance.setStatus(finalStatus);

        attendanceRepository.save(attendance);

        // Auto-create violation for early leave
        if (earlyLeaveMinutes > 5) {
            violationService.autoCreateViolation(staffId, attendance.getStaffName(), "EARLY_LEAVE",
                    "Về sớm " + earlyLeaveMinutes + " phút ở " + attendance.getShiftName() + " (" + todayDate + ")",
                    50000.0, 1);
        }

        return attendance;
    }

    public List<AttendanceDTO> getMyAttendanceHistory(String staffId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = attendanceRepository.findByStaffId(staffId);
        List<AttendanceDTO> list = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            list.add(doc.toObject(AttendanceDTO.class));
        }
        // Sort descending by checkInTime
        list.sort((a1, a2) -> Long.compare(a2.getCheckInTime(), a1.getCheckInTime()));
        return list;
    }

    public List<AttendanceDTO> getAllAttendancesWithFilters(String staffId, String date, String cinemaId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = attendanceRepository.findWithFilters(staffId, date, cinemaId);
        List<AttendanceDTO> attendances = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            attendances.add(doc.toObject(AttendanceDTO.class));
        }
        attendances.sort((a1, a2) -> Long.compare(a2.getCheckInTime(), a1.getCheckInTime()));
        return attendances;
    }
}
