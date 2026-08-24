package com.smarturban.app.api;

import com.smarturban.app.storage.TokenManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String path = originalRequest.url().encodedPath();

        // Skip adding token for auth endpoints
        if (path.contains("/api/auth/login") || path.contains("/api/auth/register")) {
            return chain.proceed(originalRequest);
        }

        String token = tokenManager.getToken();
        if (token != null && !token.trim().isEmpty()) {
            Request authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(authenticatedRequest);
        }

        return chain.proceed(originalRequest);
    }
}
