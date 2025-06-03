package com.aaa.psychological.models;

public class Appointment {
    private String userName;        // 普通用户用户名（用于咨询师端）
    private String counselorName;   // 咨询师用户名（用于用户端）
    private String time;
    private String status;
    private byte[] avatarBytes;

    // 用于用户端（显示咨询师信息）
    public Appointment(String counselorName, String time, String status, byte[] avatarBytes) {
        this.counselorName = counselorName;
        this.time = time;
        this.status = status;
        this.avatarBytes = avatarBytes;
    }

    // 用于咨询师端（显示用户信息）
    public Appointment(String userName, String time, String status, byte[] avatarBytes, boolean isUserSide) {
        this.userName = userName;
        this.time = time;
        this.status = status;
        this.avatarBytes = avatarBytes;
    }

    public String getUserName() {
        return userName;
    }

    public String getCounselorName() {
        return counselorName;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public byte[] getAvatarBytes() {
        return avatarBytes;
    }
}
