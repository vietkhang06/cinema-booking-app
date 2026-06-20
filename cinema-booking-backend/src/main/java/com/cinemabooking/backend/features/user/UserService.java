package com.cinemabooking.backend.features.user;

import com.cinemabooking.backend.features.user.UserDTO;
import com.cinemabooking.backend.features.user.request.UpdateProfileRequestDTO;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "users";

    public UserDTO getUserById(String uid) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION).document(uid).get().get();
        if (doc.exists()) {
            return mapToDTO(doc);
        }
        return null;
    }

    public UserDTO updateUserProfile(String uid, UpdateProfileRequestDTO data) throws ExecutionException, InterruptedException {
        DocumentReference usersRef = firestore.collection(COLLECTION).document(uid);

        UserDTO user = firestore.runTransaction(transaction -> {
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

    public UserDTO createUser(UserDTO user, String password) throws Exception {
        logger.info("Starting user creation for email: {}", user.getEmail());
        
        // Sanitize & Format phone number to E.164
        String formattedPhone = null;
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            String rawPhone = user.getPhone().trim();
            if (rawPhone.startsWith("0") && rawPhone.length() == 10) {
                formattedPhone = "+84" + rawPhone.substring(1);
            } else if (rawPhone.startsWith("+")) {
                formattedPhone = rawPhone;
            }
            if (formattedPhone != null) {
                user.setPhone(formattedPhone);
            }
        }

        // 1. Create in Firebase Auth using Firebase Admin SDK
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(user.getEmail())
                .setPassword(password)
                .setDisplayName(user.getName());
        
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
            user.setUid(uid);
            String role = user.getRole();
            if (role == null || role.isBlank()) {
                role = "customer";
            }
            user.setRole(role);
            user.setStatus("active");
            user.setDeleted(false);
            user.setCreatedAt(System.currentTimeMillis());
            user.setUpdatedAt(System.currentTimeMillis());
            
            // 3. Save to Firestore
            logger.info("Saving user document to Firestore for UID: {}", uid);
            firestore.collection(COLLECTION).document(uid).set(user).get();
            logger.info("Firestore document saved successfully for UID: {}", uid);
            return user;
        } catch (Exception e) {
            logger.error("Error occurred during user creation flow: {}", e.getMessage(), e);
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

    public UserDTO updateUser(String uid, UserDTO user) throws Exception {
        logger.info("Updating user info for UID: {}", uid);
        DocumentReference ref = firestore.collection(COLLECTION).document(uid);
        DocumentSnapshot doc = ref.get().get();
        if (!doc.exists()) {
            throw new RuntimeException("User not found");
        }

        String role = user.getRole() != null ? user.getRole().toLowerCase() : "customer";
        String status = user.getStatus() != null ? user.getStatus().toLowerCase() : "active";

        ref.update(
                "name", user.getName(),
                "phone", user.getPhone(),
                "status", status,
                "cinemaId", user.getCinemaId(),
                "cinemaName", user.getCinemaName(),
                "internalNotes", user.getInternalNotes(),
                "role", role,
                "updatedAt", System.currentTimeMillis()
        ).get();
        logger.info("Firestore document updated successfully for UID: {}", uid);

        // Also update Auth profile name and disabled status if changed
        try {
            boolean disable = "inactive".equalsIgnoreCase(status);
            UserRecord.UpdateRequest authUpdate = new UserRecord.UpdateRequest(uid)
                    .setDisplayName(user.getName())
                    .setDisabled(disable);
            FirebaseAuth.getInstance().updateUser(authUpdate);
            logger.info("Firebase Auth user synced (Name: {}, Disabled: {})", user.getName(), disable);
        } catch (Exception e) {
            logger.warn("Could not sync updated info with Firebase Auth: {}", e.getMessage());
        }

        return getUserById(uid);
    }

    public void deleteUser(String uid) throws Exception {
        logger.info("Soft deleting user account for UID: {}", uid);
        DocumentReference ref = firestore.collection(COLLECTION).document(uid);
        ref.update(
                "deleted", true,
                "status", "inactive",
                "updatedAt", System.currentTimeMillis()
        ).get();

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
        DocumentReference ref = firestore.collection(COLLECTION).document(uid);
        firestore.runTransaction(transaction -> {
            DocumentSnapshot doc = transaction.get(ref).get();
            if (doc.exists()) {
                long currentCount = getSafeLong(doc, "loginCount", 0L);
                transaction.update(ref, "loginCount", currentCount + 1);
            }
            return null;
        }).get();
    }

    public List<UserDTO> getAllUsers() throws ExecutionException, InterruptedException {
        logger.info("Fetching all users from Firestore");
        List<UserDTO> list = new ArrayList<>();
        try {
            CollectionReference coll = firestore.collection(COLLECTION);
            List<QueryDocumentSnapshot> documents = coll.get().get().getDocuments();
            logger.info("Found {} documents in Firestore", documents.size());
            for (DocumentSnapshot doc : documents) {
                Boolean deleted = getSafeBoolean(doc, "deleted", false);
                if (deleted) continue;
                list.add(mapToDTO(doc));
            }
        } catch (Exception e) {
            logger.error("Error fetching user list from Firestore: {}", e.getMessage(), e);
            throw e;
        }
        return list;
    }

    public List<UserDTO> getAllAdmins() throws ExecutionException, InterruptedException {
        logger.info("Fetching all admin users from Firestore (optimized query)");
        List<UserDTO> list = new ArrayList<>();
        try {
            // Optimized query: only fetch users with role 'admin'
            Query query = firestore.collection(COLLECTION).whereEqualTo("role", "admin");
            List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
            logger.info("Found {} admin documents in Firestore", documents.size());
            for (DocumentSnapshot doc : documents) {
                Boolean deleted = getSafeBoolean(doc, "deleted", false);
                if (deleted) continue;
                list.add(mapToDTO(doc));
            }
        } catch (Exception e) {
            logger.error("Error fetching admin list from Firestore: {}", e.getMessage(), e);
            throw e;
        }
        return list;
    }
}
