package com.cinemabooking.backend.features.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnackDTO {
    public static final String COLLECTION_NAME = "snacks";

    public String snackId;
    public String name;
    public String imageUrl;
    public Double price;
    public String description;
    public String status; // active, inactive
    public Long createdAt;
    public Long updatedAt;
    public Boolean deleted;
}
