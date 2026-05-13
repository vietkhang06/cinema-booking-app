package com.example.cinemabookingapp.data.repository;

import com.example.cinemabookingapp.data.remote.datasource.BookingRemoteDataSource;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.domain.repository.BookingRepository;

import java.util.List;

public class BookingRepositoryImpl implements BookingRepository {
    private final BookingRemoteDataSource remoteDataSource;

    public BookingRepositoryImpl() {
        this.remoteDataSource = new BookingRemoteDataSource();
    }

    public BookingRepositoryImpl(BookingRemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public void createBooking(Booking booking, ResultCallback<Booking> callback) {
        // Not implemented in this phase (uses existing flow if any)
        if (callback != null) callback.onError("Tính năng tạo vé mới đang được bảo trì.");
    }

    @Override
    public void getBookingById(String bookingId, ResultCallback<Booking> callback) {
        remoteDataSource.getBookingById(bookingId, callback);
    }

    @Override
    public void getBookingsByUserId(String userId, ResultCallback<List<Booking>> callback) {
        // In the context of Customer flow, we always fetch current user's bookings via /my
        remoteDataSource.getMyBookings(callback);
    }

    @Override
    public void getBookingsByShowtimeId(String showtimeId, ResultCallback<List<Booking>> callback) {
        if (callback != null) callback.onError("Tính năng chưa được hỗ trợ qua API.");
    }

    @Override
    public void getAllBookings(ResultCallback<List<Booking>> callback) {
        if (callback != null) callback.onError("Chỉ Admin mới có quyền truy cập tất cả vé.");
    }

    @Override
    public void updateBookingStatus(String bookingId, String status, ResultCallback<Booking> callback) {
        if (callback != null) callback.onError("Tính năng cập nhật trạng thái chưa được hỗ trợ qua API.");
    }

    @Override
    public void cancelBooking(String bookingId, ResultCallback<Booking> callback) {
        if (callback != null) callback.onError("Tính năng hủy vé chưa được hỗ trợ qua API.");
    }

    @Override
    public void softDeleteBooking(String bookingId, ResultCallback<Void> callback) {
        if (callback != null) callback.onError("Tính năng xóa vé chưa được hỗ trợ qua API.");
    }
}
