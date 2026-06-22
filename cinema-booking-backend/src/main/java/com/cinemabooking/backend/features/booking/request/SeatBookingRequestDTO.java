package com.cinemabooking.backend.features.booking.request;

import com.cinemabooking.backend.shared.common.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatBookingRequestDTO {
    private String showtimeId;
    private List<String> seatIds;
    private List<SnackOrder> snackOrders;

    private PaymentMethod paymentMethod;
    private String appliedVoucherCode;
    private double totalPrice;
    private String promoCode;
    private double discountVoucher;

    public record SnackOrder(String snackId, int quantity) { }
}
