package com.aaa.psychological.controllers;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.aaa.psychological.R;
import com.aaa.psychological.helpers.DatabaseHelper;

import java.util.List;

public class AppointmentRecordsActivity extends AppCompatActivity {

    private ListView lvAppointments;
    private DatabaseHelper dbHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_records);

        username = getIntent().getStringExtra("username");
        dbHelper = new DatabaseHelper(this);
        lvAppointments = findViewById(R.id.lvAppointments);

        List<String> records = dbHelper.getAppointmentsByUser(username);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, records);
        lvAppointments.setAdapter(adapter);
    }
}

