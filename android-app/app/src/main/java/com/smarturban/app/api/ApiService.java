package com.smarturban.app.api;

import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.AuthResponse;
import com.smarturban.app.model.Complaint;
import com.smarturban.app.model.ComplaintStats;
import com.smarturban.app.model.LoginRequest;
import com.smarturban.app.model.RegisterRequest;
import com.smarturban.app.model.UserResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface ApiService {

    @POST("api/auth/register")
    Call<ApiResponse<UserResponse>> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @GET("api/users/me")
    Call<ApiResponse<AuthResponse>> getCurrentUser();

    @GET("api/complaints/categories")
    Call<ApiResponse<List<String>>> getCategories();

    @Multipart
    @POST("api/complaints")
    Call<ApiResponse<Complaint>> createComplaint(
            @Part("data") RequestBody data,
            @Part MultipartBody.Part image
    );

    @GET("api/complaints/my")
    Call<ApiResponse<List<Complaint>>> getMyComplaints();

    @GET("api/complaints/stats")
    Call<ApiResponse<ComplaintStats>> getMyComplaintStats();

    @GET("api/complaints/{id}")
    Call<ApiResponse<Complaint>> getComplaintById(@Path("id") Long id);
}
