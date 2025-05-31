package com.aaa.psychological.models;

public class Counselor {
    private String name;
    private String info;           // 原 specialty
    private String available;      // 原 schedule
    private int avatarResId;
    private byte[] avatarBytes;

    public Counselor(String name, String info, String available, int avatarResId) {
        this.name = name;
        this.info = info;
        this.available = available;
        this.avatarResId = avatarResId;
    }

    public Counselor(String name, String info, String available, byte[] avatarBytes) {
        this.name = name;
        this.info = info;
        this.available = available;
        this.avatarBytes = avatarBytes;
    }

    public String getName() { return name; }
    public String getInfo() { return info; }
    public String getAvailable() { return available; }
    public int getAvatarResId() { return avatarResId; }
    public byte[] getAvatarBytes() { return avatarBytes; }
}
