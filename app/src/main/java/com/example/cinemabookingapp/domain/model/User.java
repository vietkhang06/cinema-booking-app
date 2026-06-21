package com.example.cinemabookingapp.domain.model;

public class User {
    public String uid;
    public String name;
    public String email;
    public String phone;
    public String avatarUrl;
    public String role;
    public String status;
    public String memberLevel;
    public Integer points;
    public String fcmToken;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;
    public String birthDate;
    public String gender;

    public String cinemaId;
    public String cinemaName;
    public String internalNotes;
    public Integer loginCount;

    public User() {
    }

    public String getFormattedCode() {
        if (uid == null) return "";
        String prefix = "KH";
        if ("admin".equalsIgnoreCase(role)) {
            prefix = "QTV";
        } else if ("staff".equalsIgnoreCase(role)) {
            prefix = "NV";
        }
        int hash = Math.abs(uid.hashCode());
        int num = (hash % 99) + 1;
        return prefix + String.format("%02d", num);
    }
}