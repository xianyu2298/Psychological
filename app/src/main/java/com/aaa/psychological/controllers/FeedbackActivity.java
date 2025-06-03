package com.aaa.psychological.controllers;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aaa.psychological.R;
import com.aaa.psychological.adapters.FeedbackAdapter;
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.Appointment;
import com.aaa.psychological.models.FeedbackTarget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedbackActivity extends AppCompatActivity {

    private ListView lvCounselors;
    private DatabaseHelper dbHelper;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        lvCounselors = findViewById(R.id.lvFeedbackCounselors);
        dbHelper = new DatabaseHelper(this);
        currentUsername = getIntent().getStringExtra("username");

        loadCompletedCounselors();
    }

    private void loadCompletedCounselors() {
        List<FeedbackTarget> list = new ArrayList<>();
        List<Appointment> completed = dbHelper.getAppointmentsByUser(currentUsername);

        for (Appointment a : completed) {
            if ("已完成".equals(a.getStatus())) {
                list.add(new FeedbackTarget(a.getName(), a.getTime(), a.getAvatarBytes()));
            }
        }

        FeedbackAdapter adapter = new FeedbackAdapter(this, list);
        lvCounselors.setAdapter(adapter);

        lvCounselors.setOnItemClickListener((parent, view, position, id) -> {
            FeedbackTarget selected = list.get(position);
            showFeedbackDialog(selected.getCounselorName());
        });
    }

    private void showFeedbackDialog(String counselorName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("给" + counselorName+"评价");

        final EditText input = new EditText(this);
        input.setHint("写下你的反馈...");
        input.setMinLines(3);
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String comment = input.getText().toString().trim();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            dbHelper.insertFeedback(currentUsername, counselorName, comment, timestamp);
            Toast.makeText(this, "评价已提交", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}

