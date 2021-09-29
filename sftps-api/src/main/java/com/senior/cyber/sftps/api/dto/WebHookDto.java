package com.senior.cyber.sftps.api.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WebHookDto {

    @Expose
    @SerializedName("event")
    private String event;

    @Expose
    @SerializedName("file")
    private String file;

    @Expose
    @SerializedName("file")
    private long size;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

}
