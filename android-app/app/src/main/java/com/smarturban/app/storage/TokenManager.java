package com.smarturban.app.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.smarturban.app.model.AuthResponse;

public class TokenManager {
    private static final String PREF_NAME = "SmartUrbanPrefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_ADDRESS = "user_address";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(AuthResponse authResponse) {
        SharedPreferences.Editor editor = prefs.edit();
        if (authResponse.getToken() != null) {
            editor.putString(KEY_TOKEN, authResponse.getToken());
        }
        if (authResponse.getUserId() != null) {
            editor.putLong(KEY_USER_ID, authResponse.getUserId());
        }
        if (authResponse.getFullName() != null) {
            editor.putString(KEY_USER_NAME, authResponse.getFullName());
        }
        if (authResponse.getEmail() != null) {
            editor.putString(KEY_USER_EMAIL, authResponse.getEmail());
        }
        if (authResponse.getPhone() != null) {
            editor.putString(KEY_USER_PHONE, authResponse.getPhone());
        }
        if (authResponse.getRole() != null) {
            editor.putString(KEY_USER_ROLE, authResponse.getRole());
        }
        if (authResponse.getAddress() != null) {
            editor.putString(KEY_USER_ADDRESS, authResponse.getAddress());
        }
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public Long getUserId() {
        long id = prefs.getLong(KEY_USER_ID, -1);
        return id != -1 ? id : null;
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "User");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().trim().isEmpty();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
