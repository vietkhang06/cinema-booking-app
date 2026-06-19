package com.example.cinemabookingapp.data.dto;

import java.util.List;

public class BookingDTO {
    public String bookingId;
    public String userId;
    public String movieId;
    public String cinemaId;
    public String roomId;
    public String showtimeId;
    public String movieTitleSnapshot;
    public String movieImageUrlSnapshot;
    public String cinemaNameSnapshot;
    public String roomNameSnapshot;
    public long showtimeStartAtSnapshot;
    public List<String> seatCodes;
    public List<String> seatIds;
    public String snackOrderId;
    public double subtotal;
    public double discount;
    public double total;
    public String paymentMethod;
    public String paymentStatus;
    public String bookingStatus;
    public String paymentCode;
    public long paymentAt;
    public String qrCodeValue;
    public long checkInAt;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public String appliedVoucherCode;
    public List<SnackOrderSnapshotDTO> snackOrder;

    public static class SnackOrderSnapshotDTO {
        public String snackId;
        public String snackName;
        public String snackImgURL;
        public double price;
        public int quantity;
    }

    public BookingDTO() {
    }
}