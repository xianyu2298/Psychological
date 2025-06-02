package com.aaa.psychological.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.aaa.psychological.models.Appointment;
import com.aaa.psychological.models.Counselor;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLiteHelper：管理用户表（users），用于保存用户名、密码、角色类型。
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // 数据库名称和版本
    private static final String DATABASE_NAME = "psychological_db.db";
    private static final int DATABASE_VERSION = 1;

    // 表名
    public static final String TABLE_USERS = "users";

    // 列名
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role"; // 0 = 普通用户，1 = 咨询师

    public static final String COLUMN_AVATAR = "avatar";

    private static final String TABLE_APPOINTMENTS = "appointments";



    // 建表语句
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, "
                    + COLUMN_PASSWORD + " TEXT NOT NULL, "
                    + COLUMN_ROLE + " INTEGER NOT NULL,"
                    + COLUMN_AVATAR + " BLOB"
                    + ");";

    private static final String CREATE_TABLE_APPOINTMENTS =
            "CREATE TABLE " + TABLE_APPOINTMENTS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_name TEXT NOT NULL, " +
                    "counselor_name TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT '已预约', " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "avatar BLOB" +
                    ");";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_APPOINTMENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果需要升级，先删除旧表，然后创建新表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        onCreate(db);
    }

    /**
     * 插入一个新用户
     * @param username 用户名
     * @param password 密码（建议在后续项目中做哈希或加盐处理；此处为演示，先以明文存储）
     * @param role     0=普通用户，1=咨询师
     * @return true: 插入成功；false: 插入失败（如用户名已存在等）
     */
    public boolean insertUser(String username, String password, int role, byte[] avatar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);
        values.put(COLUMN_AVATAR, avatar); // 允许为 null

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1; // 如果 result == -1，则表示插入失败
    }

    /**
     * 根据用户名查询用户信息
     * @param username 要查询的用户名
     * @return Cursor：包含列（id, username, password, role），如果用户名不存在，则 cursor.getCount() == 0
     */
    public Cursor getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_ROLE, COLUMN_AVATAR};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = { username };

        return db.query(
                TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    /**
     * 校验用户名/密码是否匹配
     * @param username 用户名
     * @param password 密码
     * @return 如果匹配，返回用户角色（0 或 1）；否则返回 -1
     */
    public int checkUserCredentials(String username, String password) {
        Cursor cursor = getUserByUsername(username);
        int role = -1;
        if (cursor != null && cursor.moveToFirst()) {
            // 已找到该用户名
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            if (storedPassword.equals(password)) {
                role = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ROLE));
            }
        }
        if (cursor != null) cursor.close();
        return role;
    }

    /**
     * 获取所有咨询师的信息
     * @return List<Counselor>
     */
    public List<Counselor> getAllCounselors() {
        List<Counselor> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USERNAME, COLUMN_AVATAR},
                COLUMN_ROLE + " = ?",
                new String[]{"1"},  // 1 表示咨询师
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                byte[] avatarBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_AVATAR));
                list.add(new Counselor(name, "擅长：未填写", "时间：待定", avatarBytes));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return list;
    }

    /**
     * 预约方法
     */
    public boolean insertAppointment(String userName, String counselorName, byte[] avatar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_name", userName);
        values.put("counselor_name", counselorName);
        values.put("status", "已预约");
        values.put("avatar", avatar);
        long result = db.insert(TABLE_APPOINTMENTS, null, values);
        db.close();
        return result != -1;
    }


    /**
     * 查询用户的预约记录
     */
    public boolean hasUserAlreadyBooked(String username, String counselorName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("appointments",
                new String[]{"id"},
                "user_name = ? AND counselor_name = ?",
                new String[]{username, counselorName},
                null, null, null);
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }

    /*
     * 查询用户的预约记录
     * @param username 用户名
     * @return List<String>：每个元素是一条预约记录的字符串表示
     */
    public List<String> getAppointmentsByUser(String username) {
        List<String> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("appointments",
                new String[]{"counselor_name", "status", "timestamp"},
                "user_name = ?",
                new String[]{username},
                null, null, "timestamp DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String status = cursor.getString(1);
                String time = cursor.getString(2);
                records.add("咨询师：" + name + "\n状态：" + status + "\n时间：" + time);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return records;
    }

    /**
     * 查询预约记录并返回结构化对象列表（包含头像）
     */
    public List<Appointment> getAppointmentsDetailedByUser(String username) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT a.counselor_name, a.status, a.timestamp, u.avatar " +
                "FROM " + TABLE_APPOINTMENTS + " a " +
                "LEFT JOIN " + TABLE_USERS + " u ON a.counselor_name = u.username " +
                "WHERE a.user_name = ? " +
                "ORDER BY a.timestamp DESC";

        Cursor cursor = db.rawQuery(query, new String[]{username});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String counselorName = cursor.getString(0);
                String status = cursor.getString(1);
                String time = cursor.getString(2);
                byte[] avatar = cursor.getBlob(3);

                Appointment appt = new Appointment(counselorName, time, status, avatar);
                list.add(appt);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return list;
    }

    // 获取用户最近一条预约状态（普通用户）
    public String getLatestAppointmentStatusForUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String status = "未预约"; // 默认状态

        Cursor cursor = db.query("appointments",
                new String[]{"status"},
                "user_name = ?",
                new String[]{username},
                null, null,
                "timestamp DESC",
                "1");

        if (cursor != null && cursor.moveToFirst()) {
            status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
            cursor.close();
        }

        return status;
    }

    // 获取咨询师最近一条预约状态（作为 counselor）
    public String getLatestAppointmentStatusForCounselor(String counselorName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String status = "暂无预约"; // 默认状态

        Cursor cursor = db.query("appointments",
                new String[]{"status"},
                "counselor_name = ?",
                new String[]{counselorName},
                null, null,
                "timestamp DESC",
                "1");

        if (cursor != null && cursor.moveToFirst()) {
            status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
            cursor.close();
        }

        return status;
    }
    // 更新用户头像
    public void updateUserAvatar(String username, byte[] avatarBytes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("avatar", avatarBytes);
        db.update("users", values, "username = ?", new String[]{username});
    }

    // 更新用户名
    public boolean updateUsername(String oldUsername, String newUsername) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", newUsername);
        int affected = db.update("users", values, "username = ?", new String[]{oldUsername});
        return affected > 0;
    }

    // 更新用户密码
    public void updateUserPassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        db.update("users", values, "username = ?", new String[]{username});
    }

    /*
     * 查询咨询师的预约记录
     */
    public List<Appointment> getAppointmentsByCounselor(String counselorName) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT a.user_name, a.status, a.timestamp, u.avatar " +
                "FROM appointments a " +
                "LEFT JOIN users u ON a.user_name = u.username " +
                "WHERE a.counselor_name = ? " +
                "ORDER BY a.timestamp DESC";

        Cursor cursor = db.rawQuery(query, new String[]{counselorName});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String username = cursor.getString(0);
                String status = cursor.getString(1);
                String time = cursor.getString(2);
                byte[] avatar = cursor.getBlob(3);

                Appointment appt = new Appointment(username, time, status, avatar); // 使用 user_name 替代 counselorName
                list.add(appt);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return list;
    }


}
