package com.cinemabooking.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnackOrderSnapshot {
    String snackId;
    String snackName;
    String snackImgURL;
    double price;
    int quantity;
}