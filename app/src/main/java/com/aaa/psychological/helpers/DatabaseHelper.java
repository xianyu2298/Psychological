package com.aaa.psychological.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.aaa.psychological.models.Appointment;
import com.aaa.psychological.models.Counselor;
import com.aaa.psychological.models.FeedbackItem;
import com.aaa.psychological.models.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * SQLiteHelper：管理用户表（users），用于保存用户名、密码、角色类型。
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // 数据库名称和版本
    private static final String DATABASE_NAME = "psychological_db.db";
    private static final int DATABASE_VERSION = 2;

    // 表名
    public static final String TABLE_USERS = "users";

    // 列名
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role"; // 0 = 普通用户，1 = 咨询师

    public static final String COLUMN_AVATAR = "avatar";

    private static final String TABLE_APPOINTMENTS = "appointments";

    private static final String TABLE_FEEDBACK = "feedbacks";


    // 建表语句
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_ROLE + " INTEGER NOT NULL, " +
                    COLUMN_AVATAR + " BLOB, " +
                    "expertise TEXT, " +
                    "available_time TEXT," +
                    "introduction TEXT"+
                    ");";

    private static final String CREATE_TABLE_APPOINTMENTS =
            "CREATE TABLE " + TABLE_APPOINTMENTS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_name TEXT NOT NULL, " +
                    "counselor_name TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT '已预约', " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "counselor_avatar BLOB," +
                    "user_avatar BLOB" +
                    ");";

    private static final String CREATE_TABLE_FEEDBACK =
            "CREATE TABLE feedbacks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_name TEXT NOT NULL, " +
                    "counselor_name TEXT NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL)";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_APPOINTMENTS);
        db.execSQL(CREATE_TABLE_FEEDBACK);
        db.execSQL("CREATE TABLE IF NOT EXISTS messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender TEXT NOT NULL, " +
                "receiver TEXT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果需要升级，先删除旧表，然后创建新表
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEEDBACK);
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
     * @return Cursor：包含列（id, username, password, role...），如果用户名不存在，则 cursor.getCount() == 0
     */
    public Cursor getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COLUMN_ID, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_ROLE,
                COLUMN_AVATAR, "expertise", "available_time", "introduction"
        };
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
                new String[]{COLUMN_USERNAME, COLUMN_AVATAR, "expertise", "available_time"},
                COLUMN_ROLE + " = ?",
                new String[]{"1"},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                byte[] avatarBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_AVATAR));
                String expertise = cursor.getString(cursor.getColumnIndexOrThrow("expertise"));
                String available = cursor.getString(cursor.getColumnIndexOrThrow("available_time"));

                if (expertise == null || expertise.isEmpty()) {
                    expertise = "未填写";
                }
                if (available == null || available.isEmpty()) {
                    available = "待定";
                }

                list.add(new Counselor(
                        name,
                        "擅长方向:" + expertise,//擅长
                        "" + available,//时间
                        avatarBytes
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return list;
    }


    /**
     * 预约方法
     */
    public boolean insertAppointment(String userName, String counselorName, byte[] userAvatar, byte[] counselorAvatar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_name", userName);
        values.put("counselor_name", counselorName);
        values.put("status", "已预约");
        values.put("user_avatar", userAvatar);
        values.put("counselor_avatar", counselorAvatar);

        // 获取当前北京时间并插入
        String beijingTime = getCurrentBeijingTime();  // 获取北京时间
        values.put("timestamp", beijingTime);  // 将北京时间插入到数据库中

        long result = db.insert("appointments", null, values);
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
                "user_name = ? AND counselor_name = ? AND status != ?",
                new String[]{username, counselorName, "已完成"},
                null, null, null);
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }


    /*
     * 查询用户的预约记录
     */
    public List<Appointment> getAppointmentsByUser(String username) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT a.counselor_name, a.status, a.timestamp, a.counselor_avatar " +
                "FROM appointments a " +
                "WHERE a.user_name = ? " +
                "ORDER BY a.timestamp DESC";

        Cursor cursor = db.rawQuery(query, new String[]{username});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String status = cursor.getString(1);
                String time = cursor.getString(2);
                byte[] avatar = cursor.getBlob(3);

                list.add(new Appointment(name, time, status, avatar));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }



    /**
     * 查询预约记录并返回结构化对象列表（包含头像）
     */
    public List<Appointment> getAppointmentsDetailedByUser(String username) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT a.counselor_name, a.status, a.timestamp, a.counselor_avatar " +
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
    public boolean updateUsername(String oldUsername, String newUsername, byte[] avatarBytes) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 更新 users 表中的用户名
        ContentValues values = new ContentValues();
        values.put("username", newUsername);
        int affected = db.update("users", values, "username = ?", new String[]{oldUsername});

        if (affected > 0) {
            // 更新 appointments 表中的用户名
            db.execSQL("UPDATE appointments SET user_name = ? WHERE user_name = ?", new String[]{newUsername, oldUsername});

            // 更新 messages 表中的发送者和接收者
            db.execSQL("UPDATE messages SET sender = ?, receiver = ? WHERE sender = ? OR receiver = ?",
                    new String[]{newUsername, newUsername, oldUsername, oldUsername});

            // 更新 feedbacks 表中的用户名
            db.execSQL("UPDATE feedbacks SET user_name = ? WHERE user_name = ?", new String[]{newUsername, oldUsername});

            // 更新头像
            updateUserAvatar(newUsername, avatarBytes);

            return true;
        }
        return false;
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

        String query = "SELECT a.user_name, a.status, a.timestamp, a.user_avatar " +
                "FROM appointments a " +
                "WHERE a.counselor_name = ? " +
                "ORDER BY a.timestamp DESC";

        Cursor cursor = db.rawQuery(query, new String[]{counselorName});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String status = cursor.getString(1);
                String time = cursor.getString(2);
                byte[] avatar = cursor.getBlob(3);

                list.add(new Appointment(name, time, status, avatar));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }


    // 咨询师更新预约状态
    public void updateAppointmentStatus(String userName, String counselorName, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 获取当前最新一条记录的状态
        Cursor cursor = db.rawQuery(
                "SELECT id, status FROM appointments WHERE user_name = ? AND counselor_name = ? ORDER BY id DESC LIMIT 1",
                new String[]{userName, counselorName});

        if (cursor != null && cursor.moveToFirst()) {
            String currentStatus = cursor.getString(cursor.getColumnIndexOrThrow("status"));
            int latestId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

            cursor.close();

            // 如果状态已经是目标状态，则不更新，避免重复治疗
            if (newStatus.equals(currentStatus)) {
                return;
            }

            // 执行更新：只更新最新一条记录
            ContentValues values = new ContentValues();
            values.put("status", newStatus);
            db.update("appointments", values, "id = ?", new String[]{String.valueOf(latestId)});
        } else if (cursor != null) {
            cursor.close();
        }
    }


    //普通用户取消预约
    public void deleteAppointment(String username, String counselorName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("appointments", "user_name = ? AND counselor_name = ?",
                new String[]{username, counselorName});
        db.close();
    }

    public List<String> getBookedCounselorsForUser(String userName) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT counselor_name FROM appointments " +
                        "WHERE user_name = ? AND status IN ('已预约', '心理治疗中', '已完成')",
                new String[]{userName}
        );
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }



    // 添加一条消息
    public void insertMessage(String sender, String receiver, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sender", sender);
        values.put("receiver", receiver);
        values.put("content", content);

        // 获取当前北京时间并插入
        String beijingTime = getCurrentBeijingTime();  // 获取北京时间
        values.put("timestamp", beijingTime);  // 将北京时间插入到数据库中

        db.insert("messages", null, values);
        db.close();
    }


    // 查询两人之间所有聊天记录（按时间排序）
    public List<Message> getMessagesBetween(String user1, String user2) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT sender, content, timestamp FROM messages WHERE " +
                        "(sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) " +
                        "ORDER BY timestamp ASC",
                new String[]{user1, user2, user2, user1});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String sender = cursor.getString(0);
                String content = cursor.getString(1);
                String timestamp = cursor.getString(2);
                messages.add(new Message(sender, content, timestamp));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return messages;
    }


    public void updateCounselorInfo(String username, String expertise, String availableTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("expertise", expertise);
        values.put("available_time", availableTime);
        db.update("users", values, "username = ?", new String[]{username});
        db.close();
    }

    public String[] getCounselorInfo(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{"expertise", "available_time"},
                "username = ?",
                new String[]{username}, null, null, null);
        String[] result = new String[]{"", ""};
        if (cursor != null && cursor.moveToFirst()) {
            result[0] = cursor.getString(0); // expertise
            result[1] = cursor.getString(1); // available_time
            cursor.close();
        }
        return result;
    }

    public String getCounselorAvailableTime(String counselorName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users",
                new String[]{"available_time"},
                "username = ?",
                new String[]{counselorName},
                null, null, null);
        String result = "";
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndexOrThrow("available_time"));
            cursor.close();
        }
        return result;
    }

    public void updateCounselorIntroduction(String username, String intro) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("introduction", intro);
        db.update("users", values, "username = ?", new String[]{username});
    }

    public String getCounselorExpertise(String username) {
        Cursor cursor = getUserByUsername(username);
        if (cursor != null && cursor.moveToFirst()) {
            String result = cursor.getString(cursor.getColumnIndexOrThrow("expertise"));
            cursor.close();
            return result;
        }
        return null;
    }

    public String getCounselorIntroduction(String username) {
        Cursor cursor = getUserByUsername(username);
        if (cursor != null && cursor.moveToFirst()) {
            String result = cursor.getString(cursor.getColumnIndexOrThrow("introduction"));
            cursor.close();
            return result;
        }
        return null;
    }

    public void insertFeedback(String userName, String counselorName, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_name", userName);
        values.put("counselor_name", counselorName);
        values.put("content", content);

        // 获取当前北京时间并插入
        String beijingTime = getCurrentBeijingTime();  // 获取北京时间
        values.put("timestamp", beijingTime);  // 将北京时间插入到数据库中

        db.insert("feedbacks", null, values);
        db.close();
    }


    public List<String> getFeedbacksForCounselor(String counselorName) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT user_name, content, timestamp FROM feedbacks WHERE counselor_name = ? ORDER BY timestamp DESC",
                new String[]{counselorName});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String user = cursor.getString(0);
                String content = cursor.getString(1);
                String time = cursor.getString(2);
                list.add(user + "：" + content + "\n时间：" + time);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return list;
    }

    public List<FeedbackItem> getFeedbackItemsForCounselor(String counselorName) {
        List<FeedbackItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT f.user_name, f.content, f.timestamp, u.avatar " +
                "FROM feedbacks f " +
                "LEFT JOIN users u ON f.user_name = u.username " +
                "WHERE f.counselor_name = ? ORDER BY f.timestamp DESC";

        Cursor cursor = db.rawQuery(query, new String[]{counselorName});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String user = cursor.getString(0);
                String content = cursor.getString(1);
                String time = cursor.getString(2);
                byte[] avatar = cursor.getBlob(3);
                list.add(new FeedbackItem(user, content, time, avatar));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return list;
    }

    public List<String> getUniqueBookedUsersForCounselor(String counselorName) {
        List<String> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT user_name FROM appointments " +
                        "WHERE counselor_name = ? AND (status = '心理治疗中' OR status = '已完成')",
                new String[]{counselorName}
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return users;
    }

    public List<Counselor> searchCounselorsByName(String keyword) {
        List<Counselor> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE role = 1 AND username LIKE ?",
                new String[]{"%" + keyword + "%"});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String available = getCounselorAvailableTime(name);
                String expertise = getCounselorExpertise(name);
                byte[] avatar = cursor.getBlob(cursor.getColumnIndexOrThrow("avatar"));

                // 设置默认值，保证前端显示格式一致
                if (available == null || available.trim().isEmpty()) {
                    available = "待定";
                }
                if (expertise == null || expertise.trim().isEmpty()) {
                    expertise = "未填写";
                }

                result.add(new Counselor(name, expertise, available, avatar));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return result;
    }

    public String getCurrentBeijingTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));  // 设置为北京时间（UTC+8）
        return sdf.format(new Date());
    }

}
