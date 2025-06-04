package com.aaa.psychological.controllers;


import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aaa.psychological.R;
import com.aaa.psychological.adapters.MyFeedbackAdapter;
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.MyFeedbackItem;

import java.util.List;

public class ViewMyFeedbackActivity extends AppCompatActivity {

    private ListView lvFeedbackList;
    private DatabaseHelper dbHelper;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_feedback);

        lvFeedbackList = findViewById(R.id.lvFeedbackList);
        dbHelper = new DatabaseHelper(this);
        currentUsername = getIntent().getStringExtra("username");

        loadUserFeedbacks();
    }

    private void loadUserFeedbacks() {
        List<MyFeedbackItem> feedbackList = dbHelper.getFeedbacksForUser(currentUsername);

        if (feedbackList.isEmpty()) {
            Toast.makeText(this, "暂无评价", Toast.LENGTH_SHORT).show();
        } else {
            MyFeedbackAdapter adapter = new MyFeedbackAdapter(this, feedbackList);
            lvFeedbackList.setAdapter(adapter);
        }
    }
}


