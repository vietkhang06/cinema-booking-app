package com.example.cinemabookingapp.data.dto;

import java.util.List;

public class SeatBookingRequestDTO {
    public String showtimeId;
    public List<String> seatIds;
    public List<SnackOrder> snackOrders;
    public String paymentMethod;
    public String appliedVoucherCode;
    public double totalPrice;

    public SeatBookingRequestDTO() {
    }

    public SeatBookingRequestDTO(String showtimeId, List<String> seatIds, List<SnackOrder> snackOrders, String paymentMethod, String appliedVoucherCode, double totalPrice) {
        this.showtimeId = showtimeId;
        this.seatIds = seatIds;
        this.snackOrders = snackOrders;
        this.paymentMethod = paymentMethod;
        this.appliedVoucherCode = appliedVoucherCode;
        this.totalPrice = totalPrice;
    }

    public static class SnackOrder {
        public String snackId;
        public int quantity;

        public SnackOrder() {
        }

        public SnackOrder(String snackId, int quantity) {
            this.snackId = snackId;
            this.quantity = quantity;
        }
    }
}
