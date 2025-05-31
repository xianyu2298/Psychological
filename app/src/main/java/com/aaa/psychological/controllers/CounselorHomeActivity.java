package com.aaa.psychological.controllers;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.aaa.psychological.R;

public class CounselorHomeActivity extends AppCompatActivity {

    private TextView tvWelcomeCounselor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counselor_home);

        tvWelcomeCounselor = findViewById(R.id.tvWelcomeCounselor);
        String username = getIntent().getStringExtra("username");
        tvWelcomeCounselor.setText("欢迎，" + username + "（咨询师）！");

        // TODO: 在这里实现 咨询师 的功能模块（查看预约、修改工作时间、查看反馈、修改用户状态、双向聊天等）
    }
}
