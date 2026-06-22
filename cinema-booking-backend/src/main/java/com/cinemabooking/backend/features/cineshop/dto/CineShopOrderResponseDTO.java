package com.cinemabooking.backend.features.cineshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CineShopOrderResponseDTO {
    private String orderId;
    private String paymentId;
    private String paymentCode;
    private double totalPrice;
    private String paymentMethod;
    private long createdAt;
}
