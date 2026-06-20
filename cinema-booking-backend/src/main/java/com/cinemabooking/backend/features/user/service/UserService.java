package com.cinemabooking.backend.features.user.service;

import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.cinemabooking.backend.features.user.request.UpdateProfileRequestDTO;
import com.cinemabooking.backend.features.user.repository.UserRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public UserDTO getUserById(String uid) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = userRepository.findById(uid);
        if (doc.exists()) {
            return mapToDTO(doc);
        }
        return null;
    }

    public UserDTO updateUserProfile(String uid, UpdateProfileRequestDTO data) throws ExecutionException, InterruptedException {
        DocumentReference usersRef = userRepository.getDocumentReference(uid);

        UserDTO user = userRepository.getFirestore().runTransaction(transaction -> {
            UserDTO t_user = transaction.get(usersRef).get().toObject(UserDTO.class);
            if (t_user == null)
                throw new RuntimeException("User not found with UID: " + uid);

            if(data.getName() != null && !data.getName().isBlank())
                t_user.setName(data.getName());
            if(data.getPhone() != null && !data.getPhone().isBlank())
                t_user.setPhone(data.getPhone());
            if(data.getAvatarUrl() != null && !data.getAvatarUrl().isBlank())
                t_user.setAvatarUrl(data.getAvatarUrl());
            if(data.getBirthDate() != null && !data.getBirthDate().isBlank())
                t_user.setBirthDate(data.getBirthDate());
            if(data.getGender() != null && !data.getGender().isBlank())
                t_user.setGender(data.getGender());

            transaction.update(usersRef,
                    "name", t_user.getName(),
                    "phone", t_user.getPhone(),
                    "avatarUrl", t_user.getAvatarUrl(),
                    "birthDate", t_user.getBirthDate(),
                    "gender", t_user.getGender()
            );

            return t_user;
        }).get();
        return user;
    }

    public UserDTO createStaff(UserDTO staff, String password) throws Exception {
        logger.info("Starting staff creation for email: {}", staff.getEmail());
        
        // Sanitize & Format phone number to E.164
        String formattedPhone = null;
        if (staff.getPhone() != null && !staff.getPhone().isBlank()) {
            String rawPhone = staff.getPhone().trim();
            if (rawPhone.startsWith("0") && rawPhone.length() == 10) {
                formattedPhone = "+84" + rawPhone.substring(1);
            } else if (rawPhone.startsWith("+")) {
                formattedPhone = rawPhone;
            }
            if (formattedPhone != null) {
                staff.setPhone(formattedPhone);
            }
        }

        // 1. Create in Firebase Auth using Firebase Admin SDK
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(staff.getEmail())
                .setPassword(password)
                .setDisplayName(staff.getName());
        
        if (formattedPhone != null) {
            try {
                createRequest.setPhoneNumber(formattedPhone);
            } catch (Exception e) {
                logger.warn("Failed to set phone number in Firebase Auth creation request: {}", e.getMessage());
            }
        }
        
        String uid = null;
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);
            uid = userRecord.getUid();
            logger.info("Firebase Auth user created successfully with UID: {}", uid);
            
            // 2. Set fields
            staff.setUid(uid);
            staff.setRole("staff");
            staff.setStatus("active");
            staff.setDeleted(false);
            staff.setCreatedAt(System.currentTimeMillis());
            staff.setUpdatedAt(System.currentTimeMillis());
            
            // 3. Save to Firestore
            logger.info("Saving staff document to Firestore for UID: {}", uid);
            userRepository.save(uid, staff);
            logger.info("Firestore document saved successfully for UID: {}", uid);
            return staff;
        } catch (Exception e) {
            logger.error("Error occurred during staff creation flow: {}", e.getMessage(), e);
            if (uid != null) {
                try {
                    logger.info("Rolling back Firebase Auth user for UID: {}", uid);
                    FirebaseAuth.getInstance().deleteUser(uid);
                    logger.info("Rollback completed for UID: {}", uid);
                } catch (Exception rollbackEx) {
                    logger.error("Rollback failed to delete Firebase Auth user: {}", rollbackEx.getMessage(), rollbackEx);
                }
            }
            throw e;
        }
    }

    public UserDTO updateStaff(String uid, UserDTO staff) throws Exception {
        logger.info("Updating staff info for UID: {}", uid);
        DocumentSnapshot doc = userRepository.findById(uid);
        if (!doc.exists()) {
            throw new RuntimeException("Staff user not found");
        }

        String role = staff.getRole() != null ? staff.getRole().toLowerCase() : "staff";
        String status = staff.getStatus() != null ? staff.getStatus().toLowerCase() : "active";

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", staff.getName());
        updates.put("phone", staff.getPhone());
        updates.put("status", status);
        updates.put("cinemaId", staff.getCinemaId());
        updates.put("cinemaName", staff.getCinemaName());
        updates.put("internalNotes", staff.getInternalNotes());
        updates.put("role", role);
        updates.put("updatedAt", System.currentTimeMillis());

        userRepository.updateFields(uid, updates);
        logger.info("Firestore document updated successfully for UID: {}", uid);

        // Also update Auth profile name and disabled status if changed
        try {
            boolean disable = "inactive".equalsIgnoreCase(status);
            UserRecord.UpdateRequest authUpdate = new UserRecord.UpdateRequest(uid)
                    .setDisplayName(staff.getName())
                    .setDisabled(disable);
            FirebaseAuth.getInstance().updateUser(authUpdate);
            logger.info("Firebase Auth user synced (Name: {}, Disabled: {})", staff.getName(), disable);
        } catch (Exception e) {
            logger.warn("Could not sync updated info with Firebase Auth: {}", e.getMessage());
        }

        return getUserById(uid);
    }

    public void deleteStaff(String uid) throws Exception {
        logger.info("Soft deleting staff account for UID: {}", uid);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("deleted", true);
        updates.put("status", "inactive");
        updates.put("updatedAt", System.currentTimeMillis());

        userRepository.updateFields(uid, updates);

        // Also disable account in Firebase Auth so they cannot log in anymore
        try {
            UserRecord.UpdateRequest authUpdate = new UserRecord.UpdateRequest(uid)
                    .setDisabled(true);
            FirebaseAuth.getInstance().updateUser(authUpdate);
            logger.info("Firebase Auth user disabled for UID: {}", uid);
        } catch (Exception e) {
            logger.warn("Could not disable user in Firebase Auth on soft delete: {}", e.getMessage());
        }
    }

    public void resetPassword(String uid, String newPassword) throws Exception {
        logger.info("Resetting password for UID: {}", uid);
        UserRecord.UpdateRequest authUpdate = new UserRecord.UpdateRequest(uid)
                .setPassword(newPassword);
        FirebaseAuth.getInstance().updateUser(authUpdate);
        logger.info("Password reset successfully in Firebase Auth for UID: {}", uid);
    }

    private String getSafeString(DocumentSnapshot doc, String field) {
        try {
            Object val = doc.get(field);
            return val != null ? val.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean getSafeBoolean(DocumentSnapshot doc, String field, boolean defaultValue) {
        try {
            Boolean val = doc.getBoolean(field);
            return val != null ? val : defaultValue;
        } catch (Exception e) {
            Object val = doc.get(field);
            if (val instanceof String) {
                return Boolean.parseBoolean((String) val);
            } else if (val instanceof Number) {
                return ((Number) val).intValue() != 0;
            }
            return defaultValue;
        }
    }

    private Long getSafeLong(DocumentSnapshot doc, String field, long defaultValue) {
        try {
            Object val = doc.get(field);
            if (val instanceof Number) {
                return ((Number) val).longValue();
            } else if (val instanceof com.google.cloud.Timestamp) {
                return ((com.google.cloud.Timestamp) val).toDate().getTime();
            } else if (val instanceof String) {
                return Long.parseLong((String) val);
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private UserDTO mapToDTO(DocumentSnapshot doc) {
        return UserDTO.builder()
                .uid(doc.getId())
                .email(getSafeString(doc, "email"))
                .phone(getSafeString(doc, "phone"))
                .name(getSafeString(doc, "name"))
                .birthDate(getSafeString(doc, "birthDate"))
                .gender(getSafeString(doc, "gender"))
                .avatarUrl(getSafeString(doc, "avatarUrl"))
                .role(getSafeString(doc, "role"))
                .status(getSafeString(doc, "status"))
                .memberLevel(getSafeString(doc, "memberLevel"))
                .cinemaId(getSafeString(doc, "cinemaId"))
                .cinemaName(getSafeString(doc, "cinemaName"))
                .internalNotes(getSafeString(doc, "internalNotes"))
                .createdAt(getSafeLong(doc, "createdAt", 0L))
                .updatedAt(getSafeLong(doc, "updatedAt", 0L))
                .deleted(getSafeBoolean(doc, "deleted", false))
                .loginCount(getSafeLong(doc, "loginCount", 0L).intValue())
                .build();
    }

    public void incrementLoginCount(String uid) throws ExecutionException, InterruptedException {
        logger.info("Incrementing loginCount for UID: {}", uid);
        DocumentReference ref = userRepository.getDocumentReference(uid);
        userRepository.getFirestore().runTransaction(transaction -> {
            DocumentSnapshot doc = transaction.get(ref).get();
            if (doc.exists()) {
                long currentCount = getSafeLong(doc, "loginCount", 0L);
                transaction.update(ref, "loginCount", currentCount + 1);
            }
            return null;
        }).get();
    }

    public List<UserDTO> getAllStaffs() throws ExecutionException, InterruptedException {
        logger.info("Fetching all staff/admin users from Firestore (optimized query)");
        List<UserDTO> list = new ArrayList<>();
        try {
            List<QueryDocumentSnapshot> documents = userRepository.findAllStaffsAndAdmins();
            logger.info("Found {} staff/admin documents in Firestore", documents.size());
            for (DocumentSnapshot doc : documents) {
                Boolean deleted = getSafeBoolean(doc, "deleted", false);
                if (deleted) continue;
                list.add(mapToDTO(doc));
            }
        } catch (Exception e) {
            logger.error("Error fetching staff list from Firestore: {}", e.getMessage(), e);
            throw e;
        }
        return list;
    }

    public List<UserDTO> getAllUsers() throws ExecutionException, InterruptedException {
        List<UserDTO> list = new ArrayList<>();
        List<QueryDocumentSnapshot> documents = userRepository.findAllUsers();
        for (DocumentSnapshot doc : documents) {
            list.add(mapToDTO(doc));
        }
        return list;
    }
}
