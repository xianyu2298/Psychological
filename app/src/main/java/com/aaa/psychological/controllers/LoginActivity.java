package com.aaa.psychological.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.aaa.psychological.R;
import com.aaa.psychological.helpers.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button loginButton;
    private TextView toggleLoginReg;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 实例化控件
        etUsername = findViewById(R.id.editTextTextEmailAddress);
        etPassword = findViewById(R.id.editTextTextPassword);
        loginButton = findViewById(R.id.loginButton);
        toggleLoginReg = findViewById(R.id.toggleLoginReg);

        dbHelper = new DatabaseHelper(this);

        // 点击 “登录” 按钮
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        // 点击 “立即注册” 文本，跳转到 注册 界面
        toggleLoginReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * 尝试登录：查询数据库校验用户名/密码，成功后根据角色跳转
     */
    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 输入校验
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 调用 DatabaseHelper 校验
        int role = dbHelper.checkUserCredentials(username, password);
        if (role == -1) {
            // 登录失败
            Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
        } else {
            // 登录成功，根据 role 跳转到不同的主页
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            Intent intent;
            if (role == 0) {
                // 普通用户主页（请自行创建 NormalUserHomeActivity）
                intent = new Intent(LoginActivity.this, NormalUserHomeActivity.class);
                intent.putExtra("username", username);
            } else {
                // 咨询师主页（请自行创建 CounselorHomeActivity）
                intent = new Intent(LoginActivity.this, CounselorHomeActivity.class);
                intent.putExtra("username", username);
            }
            startActivity(intent);
            finish();
        }
    }
}
