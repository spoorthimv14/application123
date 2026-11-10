package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class CompletionPhoto {

    @SerializedName("id")
    private Long id;

    @SerializedName("photoUrl")
    private String photoUrl;

    @SerializedName("uploadedBy")
    private String uploadedBy;

    @SerializedName("uploadedAt")
    private String uploadedAt;

    public CompletionPhoto() {}

    public CompletionPhoto(Long id, String photoUrl, String uploadedBy, String uploadedAt) {
        this.id = id;
        this.photoUrl = photoUrl;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
