package com.aaa.psychological.controllers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aaa.psychological.R;
import com.aaa.psychological.helpers.DatabaseHelper;

public class EditCounselorInfoActivity extends AppCompatActivity {

    private EditText etExpertise;
    private TextView tvTimeSelector;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private String username;
    private EditText etIntroduction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_counselor_info);

        dbHelper = new DatabaseHelper(this);

        username = getIntent().getStringExtra("username");

        etIntroduction = findViewById(R.id.etIntroduction);
        String intro = dbHelper.getCounselorIntroduction(username);
        etIntroduction.setText(intro);

        etExpertise = findViewById(R.id.etExpertise);
        tvTimeSelector = findViewById(R.id.tvTimeSelector);
        btnSave = findViewById(R.id.btnSave);

        // 读取数据库中已有的擅长方向和可预约时间
        String[] info = dbHelper.getCounselorInfo(username);
        etExpertise.setText(info[0]);
        tvTimeSelector.setText(info[1]);

        // 选择时间
        tvTimeSelector.setOnClickListener(v -> showTimePickerDialog());

        // 保存按钮
        btnSave.setOnClickListener(v -> {
            String e = etExpertise.getText().toString();
            String t = tvTimeSelector.getText().toString();
            String i = etIntroduction.getText().toString();

            dbHelper.updateCounselorInfo(username, e, t);
            dbHelper.updateCounselorIntroduction(username, i);

            Toast.makeText(this, "信息已保存", Toast.LENGTH_SHORT).show();
            finish();
        });

    }

    private void showTimePickerDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_time, null);

        CheckBox[] checkBoxes = new CheckBox[]{
                view.findViewById(R.id.cbMon), view.findViewById(R.id.cbTue), view.findViewById(R.id.cbWed),
                view.findViewById(R.id.cbThu), view.findViewById(R.id.cbFri),
                view.findViewById(R.id.cbSat), view.findViewById(R.id.cbSun)
        };

        TimePicker tpStart = view.findViewById(R.id.tpStartTime);
        TimePicker tpEnd = view.findViewById(R.id.tpEndTime);
        tpStart.setIs24HourView(true);
        tpEnd.setIs24HourView(true);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            int sh = tpStart.getHour(), sm = tpStart.getMinute();
            int eh = tpEnd.getHour(), em = tpEnd.getMinute();

            StringBuilder sb = new StringBuilder();
            String[] days = {"周一","周二","周三","周四","周五","周六","周日"};
            for (int i = 0; i < 7; i++) {
                if (checkBoxes[i].isChecked()) {
                    sb.append(days[i])
                            .append(String.format(" %02d:%02d-%02d:%02d\n", sh, sm, eh, em));
                }
            }

            tvTimeSelector.setText(sb.toString().trim());
            dialog.dismiss();
        });

        dialog.show();

    }
}
