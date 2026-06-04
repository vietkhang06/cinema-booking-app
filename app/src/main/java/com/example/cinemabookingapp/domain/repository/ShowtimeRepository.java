package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Showtime;

import java.util.List;

public interface ShowtimeRepository {
    void createShowtime(Showtime showtime, ResultCallback<Showtime> callback);
    void getShowtimeById(String showtimeId, ResultCallback<Showtime> callback);
    void getAllShowtimes(ResultCallback<List<Showtime>> callback);
    void getShowtimesByMovieId(String movieId, ResultCallback<List<Showtime>> callback);
    void getShowtimesByCinemaId(String cinemaId, ResultCallback<List<Showtime>> callback);
    void getShowtimesByDateRange(long startAt, long endAt, ResultCallback<List<Showtime>> callback);
    void updateShowtime(Showtime showtime, ResultCallback<Showtime> callback);
    void changeShowtimeStatus(String showtimeId, String status, ResultCallback<Showtime> callback);
    void softDeleteShowtime(String showtimeId, ResultCallback<Void> callback);
    void createShowtimeSchedule(Showtime showtime, ResultCallback<Showtime> callback);
    void getAllShowtimeSchedules(ResultCallback<List<Showtime>> callback);
    void deleteShowtimeSchedule(String scheduleId, ResultCallback<Void> callback);
    void cancelShowtime(String showtimeId, String adminId, ResultCallback<String> callback);
}