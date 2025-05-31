package com.aaa.psychological.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

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


    // 建表语句
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, "
                    + COLUMN_PASSWORD + " TEXT NOT NULL, "
                    + COLUMN_ROLE + " INTEGER NOT NULL,"
                    + COLUMN_AVATAR + " BLOB"
                    + ");";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        db.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果需要升级，先删除旧表，然后创建新表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
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
        String[] columns = {COLUMN_ID, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_ROLE};
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

}
