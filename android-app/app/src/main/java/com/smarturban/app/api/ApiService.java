package com.smarturban.app.api;

import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.AuthResponse;
import com.smarturban.app.model.LoginRequest;
import com.smarturban.app.model.RegisterRequest;
import com.smarturban.app.model.UserResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/auth/register")
    Call<ApiResponse<UserResponse>> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @GET("api/users/me")
    Call<ApiResponse<AuthResponse>> getCurrentUser();
}
