package com.cinemabooking.backend.features.cineshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CineShopOrderRequestDTO {
    private String itemName;
    private String itemImageUrl;
    private int quantity;
    private double totalPrice;
    private String paymentMethod;
    private String promoCode;
}
