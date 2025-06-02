package com.aaa.psychological.controllers;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.*;
import androidx.annotation.NonNull;
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
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.Appointment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

public class CounselorHomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DatabaseHelper dbHelper;
    private String currentUsername;

    private LinearLayout layoutCounselorList;
    private RecyclerView rvCounselorList;
    private TextView tvEmptyPlaceholder;
    private BottomNavigationView bottomNav;

    private NestedScrollView scrollMyProfile;
    private ImageView imgAvatar;
    private TextView tvUsernameProfile, tvAppointmentStatus;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counselor_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
    }
}
