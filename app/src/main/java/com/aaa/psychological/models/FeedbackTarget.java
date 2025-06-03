package com.aaa.psychological.models;

public class FeedbackTarget {
    private String counselorName;
    private String completedTime;
    private byte[] avatar, avatarBytes;

    public FeedbackTarget(String counselorName, String completedTime, byte[] avatar) {
        this.counselorName = counselorName;
        this.completedTime = completedTime;
        this.avatar = avatar;
        this.avatarBytes = avatar;
    }

    public String getCounselorName() { return counselorName; }
    public String getCompletedTime() { return completedTime; }
    public byte[] getAvatar() { return avatar; }

    public byte[] getAvatarBytes() {
        return avatarBytes;
    }
}

