package com.aaa.psychological.controllers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aaa.psychological.R;
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.FeedbackItem;

import java.util.List;

public class ViewFeedbacksActivity extends AppCompatActivity {

    private LinearLayout layoutFeedbackContainer;
    private DatabaseHelper dbHelper;
    private String counselorUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_feedbacks);  // 你可以复制 counselor_detail 的布局作为基础

        dbHelper = new DatabaseHelper(this);
        counselorUsername = getIntent().getStringExtra("username");

        layoutFeedbackContainer = findViewById(R.id.layoutFeedbackList);
        displayFeedbacks();

    }

    private void displayFeedbacks() {
        List<FeedbackItem> feedbackList = dbHelper.getFeedbackItemsForCounselor(counselorUsername);
        layoutFeedbackContainer.removeAllViews();

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

            layoutFeedbackContainer.addView(card);
        }
    }
}

