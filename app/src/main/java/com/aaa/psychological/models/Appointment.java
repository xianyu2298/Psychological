package com.aaa.psychological.models;

public class Appointment {
    private String name; // 统一命名为 name，用于显示（user 或 counselor）
    private String time;
    private String status;
    private byte[] avatarBytes;

    public Appointment(String name, String time, String status, byte[] avatarBytes) {
        this.name = name;
        this.time = time;
        this.status = status;
        this.avatarBytes = avatarBytes;
    }

    public String getName() {
        return name;
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

