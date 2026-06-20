package com.cinemabooking.backend.features.user.service;

import com.cinemabooking.backend.features.user.dto.ViolationDTO;
import com.cinemabooking.backend.features.user.repository.ViolationRepository;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ViolationService {

    @Autowired
    private ViolationRepository violationRepository;

    public void autoCreateViolation(String staffId, String staffName, String type, String desc, double amount, int points) throws ExecutionException, InterruptedException {
        String id = "vio_" + UUID.randomUUID().toString();
        ViolationDTO v = ViolationDTO.builder()
                .id(id)
                .staffId(staffId)
                .staffName(staffName)
                .violationType(type)
                .description(desc)
                .severity("LOW")
                .createdAt(System.currentTimeMillis())
                .createdBy("system")
                .createdByName("Hệ thống")
                .status("PENDING")
                .penaltyAmount(amount)
                .penaltyPoints(points)
                .notes("Tự động ghi nhận bởi hệ thống điểm danh.")
                .deleted(false)
                .build();
        violationRepository.save(v);
    }

    public List<ViolationDTO> getMyViolationHistory(String staffId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = violationRepository.findByStaffId(staffId);
        List<ViolationDTO> list = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            list.add(doc.toObject(ViolationDTO.class));
        }
        list.sort((v1, v2) -> Long.compare(v2.getCreatedAt(), v1.getCreatedAt()));
        return list;
    }

    public List<ViolationDTO> getAllViolationsWithFilters(String staffId, String status, String severity) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = violationRepository.findWithFilters(staffId, status, severity);
        List<ViolationDTO> violations = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            violations.add(doc.toObject(ViolationDTO.class));
        }
        violations.sort((v1, v2) -> Long.compare(v2.getCreatedAt(), v1.getCreatedAt()));
        return violations;
    }

    public ViolationDTO createViolation(String adminUid, String adminName, ViolationDTO violation) throws ExecutionException, InterruptedException {
        String id = "vio_" + UUID.randomUUID().toString();
        violation.setId(id);
        violation.setCreatedAt(System.currentTimeMillis());
        violation.setCreatedBy(adminUid);
        violation.setCreatedByName(adminName);
        violation.setDeleted(false);
        if (violation.getStatus() == null) {
            violation.setStatus("PENDING");
        }
        violationRepository.save(violation);
        return violation;
    }

    public ViolationDTO updateViolation(String id, ViolationDTO updateData) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = violationRepository.findById(id);
        if (!doc.exists()) {
            return null;
        }

        Map<String, Object> fields = new HashMap<>();
        fields.put("status", updateData.getStatus());
        fields.put("penaltyAmount", updateData.getPenaltyAmount());
        fields.put("penaltyPoints", updateData.getPenaltyPoints());
        fields.put("notes", updateData.getNotes());
        fields.put("violationType", updateData.getViolationType());
        fields.put("description", updateData.getDescription());
        fields.put("severity", updateData.getSeverity());

        violationRepository.update(id, fields);
        return violationRepository.findById(id).toObject(ViolationDTO.class);
    }

    public boolean deleteViolation(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = violationRepository.findById(id);
        if (!doc.exists()) {
            return false;
        }
        violationRepository.softDelete(id);
        return true;
    }
}
