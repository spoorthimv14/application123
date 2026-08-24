package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("errors")
    private Map<String, String> errors;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public Map<String, String> getErrors() { return errors; }
}
