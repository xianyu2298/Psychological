package com.aaa.psychological.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.aaa.psychological.R;
import com.aaa.psychological.helpers.DatabaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private RadioGroup radioGroupUserType;
    private RadioButton radioNormalUser, radioCounselor;
    private Button buttonRegister, btnSelectAvatar;
    private TextView toggleToLogin;
    private static final int PICK_IMAGE_REQUEST = 1;
    private byte[] avatarBytes = null;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. 初始化控件
        etUsername = findViewById(R.id.editTextUsername);
        etPassword = findViewById(R.id.editTextPassword);
        radioGroupUserType = findViewById(R.id.radioGroupUserType);
        radioNormalUser = findViewById(R.id.radioNormalUser);
        radioCounselor = findViewById(R.id.radioCounselor);
        buttonRegister = findViewById(R.id.buttonRegister);
        btnSelectAvatar = findViewById(R.id.btnSelectAvatar); // 新增：头像选择按钮
        toggleToLogin = findViewById(R.id.toggleToLogin);

        dbHelper = new DatabaseHelper(this);

        // 2. 注册按钮点击事件
        buttonRegister.setOnClickListener(view -> attemptRegister());

        // 3. 跳转登录
        toggleToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // 4. 选择头像（可选）
        btnSelectAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择头像"), PICK_IMAGE_REQUEST);
        });
    }

    /**
     * 注册逻辑处理：校验、角色判断、数据库写入
     */
    private void attemptRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 判断角色类型
        int selectedId = radioGroupUserType.getCheckedRadioButtonId();
        int role = (selectedId == R.id.radioCounselor) ? 1 : 0;

        // 插入数据库（含头像）
        boolean insertSuccess = dbHelper.insertUser(username, password, role, avatarBytes);
        if (insertSuccess) {
            Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "注册失败：用户名可能已存在", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取图片结果并转成 byte[]
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                avatarBytes = getBytes(inputStream);  // 转成 byte[]
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "头像选择失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 工具：InputStream → byte[]
     */
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
