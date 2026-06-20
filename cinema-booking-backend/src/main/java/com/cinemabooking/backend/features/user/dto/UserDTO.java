package com.cinemabooking.backend.features.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    public static final String COLLECTION_NAME = "users";

    private String uid;
    private String email;
    private String phone;
    private String name;
    private String birthDate;
    private String gender;
    private String avatarUrl;
    private String role; // customer, staff, admin
    private String status; // active, inactive
    private String memberLevel; // BRONZE, SILVER, GOLD, PLATINUM
    private String cinemaId; // specific for staff
    private String cinemaName; // specific for staff
    private String internalNotes; // specific for staff
    private long createdAt;
    private long updatedAt;
    private boolean deleted;
    private int loginCount;
}
