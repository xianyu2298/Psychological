package com.aaa.psychological.controllers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
            showFeedbackDialog(selected);
        });
    }

    private void showFeedbackDialog(FeedbackTarget target) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_feedback_input, null);

        ImageView avatar = view.findViewById(R.id.imgCounselorAvatar);
        TextView tvName = view.findViewById(R.id.tvCounselorName);
        EditText etFeedback = view.findViewById(R.id.etFeedbackInput);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        // 填充内容
        tvName.setText("咨询师：" + target.getCounselorName());
        if (target.getAvatarBytes() != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(target.getAvatarBytes(), 0, target.getAvatarBytes().length);
            avatar.setImageBitmap(bmp);
        }

        // 构造 AlertDialog
        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 按钮逻辑
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String comment = etFeedback.getText().toString().trim();
            if (!comment.isEmpty()) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                dbHelper.insertFeedback(currentUsername, target.getCounselorName(), comment, timestamp);
                Toast.makeText(this, "评价已提交", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "请输入评价内容", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

}

