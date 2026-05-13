package com.cinemabooking.backend.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SnackOrderSnapshot {
    String snackId;
    String snackName;
    String snackImgURL;
    double price;
    int quantity;
}