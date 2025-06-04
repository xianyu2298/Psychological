package com.aaa.psychological.models;

public class MyFeedbackItem {
    private String counselorName;
    private String content;
    private String timestamp;
    private byte[] avatar;

    public MyFeedbackItem(String counselorName, String content, String timestamp, byte[] avatar) {
        this.counselorName = counselorName;
        this.content = content;
        this.timestamp = timestamp;
        this.avatar = avatar;
    }

    public String getCounselorName() {
        return counselorName;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public byte[] getAvatar() {
        return avatar;
    }
}

