package com.angkorteam.internet;

import java.util.Date;

public class Status {

    private Date lastError;

    private Date lastSuccess;

    private long counter;

    public Date getLastError() {
        return lastError;
    }

    public void setLastError(Date lastError) {
        this.lastError = lastError;
    }

    public Date getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(Date lastSuccess) {
        this.lastSuccess = lastSuccess;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }
}
