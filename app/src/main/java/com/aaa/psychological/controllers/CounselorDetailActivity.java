package com.aaa.psychological.controllers;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aaa.psychological.R;
import com.aaa.psychological.helpers.DatabaseHelper;

public class CounselorDetailActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvUsername, tvAvailableTime, tvExpertise, tvIntro;
    private Button btnBack;
    private DatabaseHelper dbHelper;
    private String counselorUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counselor_detail);

        dbHelper = new DatabaseHelper(this);
        counselorUsername = getIntent().getStringExtra("username");

        imgAvatar = findViewById(R.id.imgAvatar);
        tvUsername = findViewById(R.id.tvUsername);
        tvAvailableTime = findViewById(R.id.tvAvailableTime);
        tvExpertise = findViewById(R.id.tvExpertise);
        tvIntro = findViewById(R.id.tvIntro);
        btnBack = findViewById(R.id.btnBack);

        // 绑定数据
        loadCounselorData();

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadCounselorData() {
        Cursor cursor = dbHelper.getUserByUsername(counselorUsername);
        if (cursor != null && cursor.moveToFirst()) {
            String availableTime = dbHelper.getCounselorAvailableTime(counselorUsername);
            String expertise = dbHelper.getCounselorExpertise(counselorUsername);
            String intro = dbHelper.getCounselorIntroduction(counselorUsername);
            byte[] avatar = cursor.getBlob(cursor.getColumnIndexOrThrow("avatar"));

            tvUsername.setText("用户名：" + counselorUsername);
            tvAvailableTime.setText("可预约时间：" + (availableTime == null ? "未填写" : availableTime));
            tvExpertise.setText("擅长方向：" + (expertise == null ? "未填写" : expertise));
            tvIntro.setText("简介：" + (intro == null ? "未填写" : intro));

            if (avatar != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
                imgAvatar.setImageBitmap(bmp);
            }
            cursor.close();
        }
    }
}

