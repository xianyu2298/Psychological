package com.aaa.psychological.models;

public class Appointment {
    private String counselorName; // 实际也可表示 user_name
    private String time;
    private String status;
    private byte[] avatarBytes;

    public Appointment(String counselorName, String time, String status, byte[] avatarBytes) {
        this.counselorName = counselorName;
        this.time = time;
        this.status = status;
        this.avatarBytes = avatarBytes;
    }

    public String getCounselorName() { return counselorName; } // 实际为 user_name
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public byte[] getAvatarBytes() { return avatarBytes; }
}

