package com.cinemabooking.backend.features.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDTO {
    public static final String COLLECTION_NAME = "vouchers";

    private String voucherId;
    private String userId;
    private String code;
    private Integer discountPercent;
    private long expiredAt;
    private String status; // ACTIVE, USED, EXPIRED
    private long createdAt;
    private long updatedAt;
}
