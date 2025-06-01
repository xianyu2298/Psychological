package com.aaa.psychological.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aaa.psychological.R;
import com.aaa.psychological.adapters.AppointmentAdapter;
import com.aaa.psychological.adapters.CounselorAdapter;
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.Appointment;
import com.aaa.psychological.models.Counselor;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import com.aaa.psychological.models.Counselor;

public class NormalUserHomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvCounselorList;
    private TextView tvEmptyPlaceholder;
    private BottomNavigationView bottomNav;

    private DatabaseHelper dbHelper;

    private List<Counselor> counselorList = new ArrayList<>();
    private CounselorAdapter counselorAdapter;

    private ListView lvAppointments;
    private LinearLayout layoutCounselorList;

    private String currentUsername;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_user_home);

        lvAppointments = findViewById(R.id.lvAppointments);
        layoutCounselorList = findViewById(R.id.layoutCounselorList);


        currentUsername = getIntent().getStringExtra("username");
        dbHelper = new DatabaseHelper(this);

        // 1. 绑定 Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 如果想隐藏右侧默认标题图标，可：
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // 2. 绑定 RecyclerView、EmptyView
        rvCounselorList = findViewById(R.id.rvCounselorList);
        tvEmptyPlaceholder = findViewById(R.id.tvEmptyPlaceholder);
        rvCounselorList.setLayoutManager(new LinearLayoutManager(this));

        // 3. 初始化并设置 Adapter（这里用自定义的 CounselorAdapter）
        counselorAdapter = new CounselorAdapter(counselorList, this, currentUsername);
        rvCounselorList.setAdapter(counselorAdapter);

        // 4. 绑定 BottomNavigationView 并监听切换
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // 如果是 “首页” 图标被点了
                if (id == R.id.nav_home) {
                    lvAppointments.setVisibility(View.GONE);
                    layoutCounselorList.setVisibility(View.VISIBLE);
                    loadCounselorList();  // 可刷新数据
                    return true;
                }
                // 如果是 “预约记录” 图标被点了
                else if (id == R.id.nav_records) {
                    showAppointmentRecords();
                    return true;
                }

                // 如果是 “消息” 图标被点了
                else if (id == R.id.nav_messages) {
                    // 跳转到 ChatListActivity
                    // Intent intent = new Intent(NormalUserHomeActivity.this, ChatListActivity.class);
                    // startActivity(intent);
                    return true;
                }
                // 如果是 “我的” 图标被点了
                else if (id == R.id.nav_profile) {
                    // 跳转到 ProfileActivity
                    // Intent intent = new Intent(NormalUserHomeActivity.this, ProfileActivity.class);
                    // startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        // 5. 第一次默认加载咨询师列表
        loadCounselorList();
    }

    /**
     * 加载咨询师列表数据
     */
    private void loadCounselorList() {
        counselorList.clear();
        counselorList.addAll(dbHelper.getAllCounselors());

        if (counselorList.isEmpty()) {
            tvEmptyPlaceholder.setVisibility(View.VISIBLE);
            rvCounselorList.setVisibility(View.GONE);
        } else {
            tvEmptyPlaceholder.setVisibility(View.GONE);
            rvCounselorList.setVisibility(View.VISIBLE);
            counselorAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 显示预约记录
     */
    private void showAppointmentRecords() {
        layoutCounselorList.setVisibility(View.GONE);
        lvAppointments.setVisibility(View.VISIBLE);

        List<Appointment> records = dbHelper.getAppointmentsDetailedByUser(currentUsername);
        AppointmentAdapter adapter = new AppointmentAdapter(this, records);
        lvAppointments.setAdapter(adapter);
    }


}
