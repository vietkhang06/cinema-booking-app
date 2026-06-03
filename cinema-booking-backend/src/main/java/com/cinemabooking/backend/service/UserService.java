package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.UserDTO;
import com.cinemabooking.backend.dto.request.UpdateProfileRequestDTO;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

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

    private UserDTO mapToDTO(DocumentSnapshot doc) {
        return UserDTO.builder()
                .uid(doc.getId())
                .email(doc.getString("email"))
                .phone(doc.getString("phone"))
                .name(doc.getString("name"))
                .birthDate(doc.getString("birthDate"))
                .gender(doc.getString("gender"))
                .avatarUrl(doc.getString("avatarUrl"))
                .role(doc.getString("role"))
                .status(doc.getString("status"))
                .memberLevel(doc.getString("memberLevel"))
                .createdAt(doc.get("createdAt") instanceof Number ? doc.getLong("createdAt") : 0L)
                .updatedAt(doc.get("updatedAt") instanceof Number ? doc.getLong("updatedAt") : 0L)
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .build();
    }

    public List<UserDTO> getAllStaffs() throws ExecutionException, InterruptedException {
        return firestore.collection(UserDTO.COLLECTION_NAME)
                .whereEqualTo("role", "staff")
                .get()
                .get().toObjects(UserDTO.class);
    }
}
