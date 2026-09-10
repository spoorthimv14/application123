package com.smarturban.dto;

public class ComplaintStatsResponse {

    private long total;
    private long pending;
    private long assigned;
    private long inProgress;
    private long resolved;
    private long rejected;

    public ComplaintStatsResponse() {}

    public ComplaintStatsResponse(long total, long pending, long inProgress, long resolved, long rejected) {
        this(total, pending, inProgress, resolved, rejected, 0);
    }

    public ComplaintStatsResponse(long total, long pending, long inProgress, long resolved, long rejected, long assigned) {
        this.total = total;
        this.pending = pending;
        this.inProgress = inProgress;
        this.resolved = resolved;
        this.rejected = rejected;
        this.assigned = assigned;
    }

    public long getAssigned() {
        return assigned;
    }

    public void setAssigned(long assigned) {
        this.assigned = assigned;
    }

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
