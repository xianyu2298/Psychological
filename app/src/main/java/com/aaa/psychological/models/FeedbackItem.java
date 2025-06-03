package com.aaa.psychological.models;

public class FeedbackItem {
    private String username;
    private String content;
    private String timestamp;
    private byte[] avatar;

    public FeedbackItem(String username, String content, String timestamp, byte[] avatar) {
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
        this.avatar = avatar;
    }

    public String getUsername() { return username; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public byte[] getAvatar() { return avatar; }
}
