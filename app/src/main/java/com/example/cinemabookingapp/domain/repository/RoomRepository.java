package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Room;

import java.util.List;

public interface RoomRepository {
    void createRoom(Room room, ResultCallback<Room> callback);
    void getRoomById(String roomId, ResultCallback<Room> callback);
    void getRoomsByCinemaId(String cinemaId, ResultCallback<List<Room>> callback);
    void getAllRooms(ResultCallback<List<Room>> callback);
    void updateRoom(Room room, ResultCallback<Room> callback);
    void softDeleteRoom(String roomId, ResultCallback<Void> callback);
}