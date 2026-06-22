package com.cinemabooking.backend.features.cineshop.controller;

import com.cinemabooking.backend.features.cineshop.dto.CineShopOrderRequestDTO;
import com.cinemabooking.backend.features.cineshop.dto.CineShopOrderResponseDTO;
import com.cinemabooking.backend.features.payment.model.Payment;
import com.cinemabooking.backend.features.payment.service.PaymentService;
import com.cinemabooking.backend.shared.dto.ApiResponse;
import com.google.cloud.firestore.Firestore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/cine-shop")
public class CineShopOrderController {

    private static final Logger log = LoggerFactory.getLogger(CineShopOrderController.class);

    @Autowired
    private Firestore firestore;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<CineShopOrderResponseDTO>> createOrder(
            @AuthenticationPrincipal String userId,
            @RequestBody CineShopOrderRequestDTO request
    ) throws ExecutionException, InterruptedException {
        log.info("[CINESHOP_FLOW] Creating CineShop order for user: {}, item: {}, total: {}", 
                userId, request.getItemName(), request.getTotalPrice());

        String orderId = "cso_" + UUID.randomUUID().toString().substring(0, 8);
        long now = System.currentTimeMillis();

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("userId", userId);
        orderData.put("itemName", request.getItemName());
        orderData.put("itemImageUrl", request.getItemImageUrl());
        orderData.put("quantity", request.getQuantity());
        orderData.put("totalPrice", request.getTotalPrice());
        orderData.put("paymentMethod", request.getPaymentMethod());
        orderData.put("status", "PENDING");
        orderData.put("createdAt", now);
        orderData.put("updatedAt", now);

        // Save order to Firestore
        firestore.collection("cine_shop_orders").document(orderId).set(orderData).get();

        // Create pending payment record
        Payment payment = paymentService.createPendingPayment(
                orderId,
                userId,
                request.getPaymentMethod(),
                request.getTotalPrice()
        );

        CineShopOrderResponseDTO responseData = CineShopOrderResponseDTO.builder()
                .orderId(orderId)
                .paymentId(payment.getPaymentId())
                .paymentCode(payment.getPaymentCode())
                .totalPrice(payment.getAmount())
                .paymentMethod(payment.getProvider())
                .createdAt(payment.getCreatedAt())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(responseData, "Đơn hàng CineShop đã được tạo thành công.")
        );
    }
}
