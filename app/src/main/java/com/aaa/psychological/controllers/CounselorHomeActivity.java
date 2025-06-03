package com.aaa.psychological.controllers;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.aaa.psychological.R;
import com.aaa.psychological.adapters.AppointmentAdapter;
import com.aaa.psychological.adapters.AppointmentRecyclerAdapter;
import com.aaa.psychological.adapters.BannerAdapter;
import com.aaa.psychological.adapters.ChatListAdapter;
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.Appointment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CounselorHomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DatabaseHelper dbHelper;
    private String currentUsername;

    private LinearLayout layoutCounselorList;
    private RecyclerView rvCounselorList;
    private TextView tvEmptyPlaceholder, btnFeedback;
    private BottomNavigationView bottomNav;

    private NestedScrollView scrollMyProfile;
    private ImageView imgAvatar;
    private TextView tvUsernameProfile, tvAppointmentStatus;
    private Button btnLogout;

    private ListView lvAppointments;

    private Handler sliderHandler;
    private Runnable sliderRunnable;
    private ViewPager2 bannerViewPager;

    private ListView lvMessageList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counselor_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lvMessageList = findViewById(R.id.lvMessageList);

        lvAppointments = findViewById(R.id.lvAppointments);


        dbHelper = new DatabaseHelper(this);
        currentUsername = getIntent().getStringExtra("username");

        layoutCounselorList = findViewById(R.id.layoutCounselorList);
        rvCounselorList = findViewById(R.id.rvCounselorList);
        tvEmptyPlaceholder = findViewById(R.id.tvEmptyPlaceholder);
        rvCounselorList.setLayoutManager(new LinearLayoutManager(this));

        scrollMyProfile = findViewById(R.id.scrollMyProfile);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUsernameProfile = findViewById(R.id.tvUsernameProfile);
        tvAppointmentStatus = findViewById(R.id.tvAppointmentStatus);
        btnLogout = findViewById(R.id.btnLogout);

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(this::onBottomNavSelected);

        loadAppointmentsForCounselor();
        //轮播图
        ViewPager2 bannerViewPager = findViewById(R.id.bannerViewPager);
        List<Integer> banners = Arrays.asList(
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3
        );
        BannerAdapter bannerAdapter = new BannerAdapter(banners);
        bannerViewPager.setAdapter(bannerAdapter);

        // 自动轮播逻辑
        sliderHandler = new Handler(Looper.getMainLooper());
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = bannerViewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % bannerAdapter.getItemCount();
                bannerViewPager.setCurrentItem(nextItem, true);
                sliderHandler.postDelayed(this, 3000);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 3000);

        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
        //咨询师用户跳转到编辑信息页
        btnFeedback = findViewById(R.id.btnFeedback);
        btnFeedback.setText("修改其他信息");
        btnFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(CounselorHomeActivity.this, EditCounselorInfoActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
    }

    private boolean onBottomNavSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        hideAllViews();

        if (id == R.id.nav_home) {
            layoutCounselorList.setVisibility(LinearLayout.VISIBLE);
            loadAppointmentsForCounselor();
            return true;
        } else if (id == R.id.nav_profile) {
            scrollMyProfile.setVisibility(NestedScrollView.VISIBLE);
            showMyProfile();
            return true;
        } else if (id == R.id.nav_help) {
            lvAppointments.setVisibility(View.VISIBLE);
            showAppointmentRecords();
            return true;
        }else if (id == R.id.nav_messages) {
            showMessageList();
            return true;
        }
        return false;
    }

    private void loadAppointmentsForCounselor() {
        List<Appointment> appointments = dbHelper.getAppointmentsByCounselor(currentUsername);

        if (appointments.isEmpty()) {
            tvEmptyPlaceholder.setVisibility(TextView.VISIBLE);
            rvCounselorList.setVisibility(RecyclerView.GONE);
        } else {
            tvEmptyPlaceholder.setVisibility(TextView.GONE);
            rvCounselorList.setVisibility(RecyclerView.VISIBLE);
            AppointmentRecyclerAdapter adapter = new AppointmentRecyclerAdapter(this, appointments);
            rvCounselorList.setAdapter(adapter);
        }

        AppointmentRecyclerAdapter adapter = new AppointmentRecyclerAdapter(this, appointments);
        adapter.setOnItemClickListener(appt -> showAppointmentDialog(appt));
        rvCounselorList.setAdapter(adapter);

    }

    private void showMyProfile() {
        tvUsernameProfile.setText("用户名：" + currentUsername);
        tvAppointmentStatus.setText("角色：咨询师");

        Cursor cursor = dbHelper.getUserByUsername(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            byte[] avatarBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("avatar"));
            if (avatarBytes != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                imgAvatar.setImageBitmap(bmp);
            }
            cursor.close();
        }

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(CounselorHomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void hideAllViews() {
        layoutCounselorList.setVisibility(LinearLayout.GONE);
        scrollMyProfile.setVisibility(NestedScrollView.GONE);
        lvAppointments.setVisibility(View.GONE);
        lvMessageList.setVisibility(View.GONE);
    }

    private void showAppointmentDialog(Appointment appt) {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_detail, null);

        ImageView imgUser = dialogView.findViewById(R.id.imgUser);
        TextView tvName = dialogView.findViewById(R.id.tvUserName);
        TextView tvTime = dialogView.findViewById(R.id.tvTime);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnTreat = dialogView.findViewById(R.id.btnTreat);

        tvName.setText("预约人：" + appt.getName());
        tvTime.setText("预约时间：" + appt.getTime());
        if (appt.getAvatarBytes() != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(appt.getAvatarBytes(), 0, appt.getAvatarBytes().length);
            imgUser.setImageBitmap(bmp);
        }

        AlertDialog dialog = builder.setView(dialogView).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnTreat.setOnClickListener(v -> {
            dbHelper.updateAppointmentStatus(appt.getName(), currentUsername, "心理治疗中");
            loadAppointmentsForCounselor();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showAppointmentRecords() {
        List<Appointment> all = dbHelper.getAppointmentsByCounselor(currentUsername);
        List<Appointment> filtered = new java.util.ArrayList<>();

        for (Appointment a : all) {
            if ("心理治疗中".equals(a.getStatus()) || "已完成".equals(a.getStatus())) {
                filtered.add(a);
            }
        }

        AppointmentAdapter adapter = new AppointmentAdapter(this, filtered);
        lvAppointments.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    private void showMessageList() {
        lvMessageList.setVisibility(View.VISIBLE);

        List<Appointment> appointments = dbHelper.getAppointmentsByCounselor(currentUsername);
        List<String> activeUsers = new ArrayList<>();

        for (Appointment a : appointments) {
            if ("心理治疗中".equals(a.getStatus())|| "已完成".equals(a.getStatus())) {
                activeUsers.add(a.getName());  //  正确字段
            }
        }

        ChatListAdapter adapter = new ChatListAdapter(this, activeUsers, currentUsername);
        lvMessageList.setAdapter(adapter);

        lvMessageList.setOnItemClickListener((parent, view, position, id) -> {
            String user = activeUsers.get(position);
            Intent intent = new Intent(CounselorHomeActivity.this, ChatActivity.class);
            intent.putExtra("sender", currentUsername);  // 咨询师是发送者
            intent.putExtra("receiver", user);           // 用户是接收者
            intent.putExtra("isCounselor", true);
            startActivity(intent);
        });
    }


}