package com.aaa.psychological.controllers;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aaa.psychological.R;
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.FeedbackItem;

import java.util.List;

public class CounselorDetailActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvUsername, tvAvailableTime, tvExpertise, tvIntro;
    private Button btnBack;
    private DatabaseHelper dbHelper;
    private String counselorUsername;

    private String counselorName;

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

        LinearLayout layoutFeedbackList = findViewById(R.id.layoutFeedbackList);
        List<String> feedbacks = dbHelper.getFeedbacksForCounselor(counselorUsername);

        layoutFeedbackList.removeAllViews(); // 清空旧内容

        if (feedbacks.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("暂无评价");
            tv.setTextSize(14);
            tv.setTextColor(Color.GRAY);
            layoutFeedbackList.addView(tv);
        } else {
            for (String item : feedbacks) {
                TextView tv = new TextView(this);
                tv.setText(item);
                tv.setTextSize(14);
                tv.setPadding(8, 8, 8, 8);
                tv.setBackgroundResource(R.drawable.bg_feedback_item); // 可选背景
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 8, 0, 8);
                tv.setLayoutParams(params);
                layoutFeedbackList.addView(tv);
            }
        }
        displayFeedbacks(counselorUsername); //


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
    private void displayFeedbacks(String counselorName) {
        List<FeedbackItem> feedbackList = dbHelper.getFeedbackItemsForCounselor(counselorName);
        LinearLayout layoutContainer = findViewById(R.id.layoutFeedbackList);
        layoutContainer.removeAllViews(); // 清除旧内容

        for (FeedbackItem item : feedbackList) {
            View card = getLayoutInflater().inflate(R.layout.item_feedback_card, null);

            ImageView avatar = card.findViewById(R.id.imgUserAvatar);
            TextView tvName = card.findViewById(R.id.tvUserName);
            TextView tvContent = card.findViewById(R.id.tvFeedbackContent);
            TextView tvTime = card.findViewById(R.id.tvFeedbackTime);

            tvName.setText(item.getUsername());
            tvContent.setText(item.getContent());
            tvTime.setText(item.getTimestamp());

            if (item.getAvatar() != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(item.getAvatar(), 0, item.getAvatar().length);
                avatar.setImageBitmap(bmp);
            } else {
                avatar.setImageResource(R.drawable.ic_default_avatar);
            }

            layoutContainer.addView(card);
        }
    }

}

