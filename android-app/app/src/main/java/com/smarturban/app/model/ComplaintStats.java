package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class ComplaintStats {

    @SerializedName("total")
    private long total;

    @SerializedName("pending")
    private long pending;

    @SerializedName("inProgress")
    private long inProgress;

    @SerializedName("resolved")
    private long resolved;

    @SerializedName("rejected")
    private long rejected;

    public ComplaintStats() {}

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getInProgress() {
        return inProgress;
    }

    public void setInProgress(long inProgress) {
        this.inProgress = inProgress;
    }

    public long getResolved() {
        return resolved;
    }

    public void setResolved(long resolved) {
        this.resolved = resolved;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }
}
