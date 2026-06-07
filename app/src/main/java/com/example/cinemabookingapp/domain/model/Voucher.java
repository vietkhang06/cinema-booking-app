package com.example.cinemabookingapp.domain.model;

import java.io.Serializable;

public class Voucher implements Serializable {
    public String voucherId;
    public String userId;
    public String code;
    public int discountPercent;
    public long expiredAt;
    public String status;
    public long createdAt;
    public long updatedAt;

    public Voucher() {
    }
}
