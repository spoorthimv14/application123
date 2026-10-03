package com.smarturban.app.api;

import com.smarturban.app.model.*;
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

    @GET("api/complaints/statuses")
    Call<ApiResponse<List<String>>> getStatuses();

    @GET("api/departments")
    Call<ApiResponse<List<Department>>> getDepartments();

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

    // Admin Complaint Endpoints
    @GET("api/admin/complaints")
    Call<ApiResponse<List<Complaint>>> getAdminComplaints(@Query("status") String statusFilter);

    @GET("api/admin/complaints/stats")
    Call<ApiResponse<ComplaintStats>> getAdminStats();

    @PUT("api/admin/complaints/{id}/status")
    Call<ApiResponse<Complaint>> updateComplaintStatus(
            @Path("id") Long id,
            @Body StatusUpdateRequest request
    );

    @PUT("api/admin/complaints/{id}/assign")
    Call<ApiResponse<Complaint>> assignDepartment(
            @Path("id") Long id,
            @Body AssignDepartmentRequest request
    );

    // Admin User Management Endpoints
    @GET("api/admin/users")
    Call<ApiResponse<List<UserResponse>>> getUsers(
            @Query("search") String search,
            @Query("role") String role,
            @Query("enabled") Boolean enabled
    );

    @GET("api/admin/users/{id}")
    Call<ApiResponse<UserResponse>> getUserById(@Path("id") Long id);

    @POST("api/admin/users")
    Call<ApiResponse<UserResponse>> createUser(@Body CreateUserRequest request);

    @PUT("api/admin/users/{id}")
    Call<ApiResponse<UserResponse>> updateUser(
            @Path("id") Long id,
            @Body UpdateUserRequest request
    );

    @PUT("api/admin/users/{id}/status")
    Call<ApiResponse<UserResponse>> updateUserStatus(
            @Path("id") Long id,
            @Body UpdateUserStatusRequest request
    );

    @PUT("api/admin/users/{id}/password")
    Call<ApiResponse<UserResponse>> resetUserPassword(
            @Path("id") Long id,
            @Body ResetUserPasswordRequest request
    );
}
