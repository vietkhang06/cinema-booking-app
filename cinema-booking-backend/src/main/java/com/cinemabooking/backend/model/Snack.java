package com.cinemabooking.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Snack {
    public String snackId;
    public String categoryId;
    public String name;
    public String description;
    public double price;
    public String imageUrl;
    public boolean isAvailable;
    public String status;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;
}
