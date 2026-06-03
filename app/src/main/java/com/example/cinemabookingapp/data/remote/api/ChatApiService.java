package com.example.cinemabookingapp.data.remote.api;

import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.request.SendMessageRequest;
import com.example.cinemabookingapp.domain.model.ChatMessage;
import com.example.cinemabookingapp.domain.model.ChatThread;
import com.example.cinemabookingapp.domain.model.Conversation;
import com.example.cinemabookingapp.domain.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApiService {
    @POST("chat/messages")
    Call<ApiResponse<ChatMessage>> sendMessage(@Body SendMessageRequest message);

//    @POST("chat/conversations/{userId}/open")
//
    @POST("chat/conversations/{convoId}/read")
    Call<Void> markAsRead(@Path("convoId") String convoId);

    @GET("chat/conversations")
    Call<ApiResponse<List<Conversation>>> getMyConversations();

    @GET("chat/users/staff")
    Call<ApiResponse<List<User>>> getAllStaffs();

    @GET("chat/users/{userId}/messages")
    Call<ApiResponse<List<ChatMessage>>> getMessagesByReceiverId(@Path("userId") String userId);

    @GET("chat/conversations/{userId}/users")
    Call<ApiResponse<Conversation>> getConversationByReceiverId(@Path("userId") String userId);

    @GET("chat/conversations/{convoId}/messages")
    Call<ApiResponse<List<ChatMessage>>> getMessagesByConvoId(@Path("convoId") String convoId);




}
