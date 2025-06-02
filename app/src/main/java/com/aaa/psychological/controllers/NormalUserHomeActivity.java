package com.aaa.psychological.controllers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aaa.psychological.R;
import com.aaa.psychological.adapters.AppointmentAdapter;
import com.aaa.psychological.adapters.BannerAdapter;
import com.aaa.psychological.adapters.CounselorAdapter;
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.Appointment;
import com.aaa.psychological.models.Counselor;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.aaa.psychological.models.Counselor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.database.Cursor;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.viewpager2.widget.ViewPager2;


public class NormalUserHomeActivity extends AppCompatActivity {

    private android.os.Handler sliderHandler;
    private Runnable sliderRunnable;
    private ViewPager2 bannerViewPager;


    private Toolbar toolbar;
    private RecyclerView rvCounselorList;
    private TextView tvEmptyPlaceholder;
    private BottomNavigationView bottomNav;

    private DatabaseHelper dbHelper;

    private List<Counselor> counselorList = new ArrayList<>();
    private CounselorAdapter counselorAdapter;

    private ListView lvAppointments;
    private ListView lvMessageList;
    private LinearLayout layoutCounselorList;

    private String currentUsername;

    private NestedScrollView scrollMyProfile;
    private ImageView imgAvatar;
    private TextView tvUsernameProfile;
    private TextView tvAppointmentStatus;

    private Button btnLogout;

    private TextView btnChangeAvatar, btnChangeUsername, btnChangePassword, btnFeedback;

    private static final int REQUEST_CODE_PICK_IMAGE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_user_home);
        lvMessageList = findViewById(R.id.lvMessageList);

        lvAppointments = findViewById(R.id.lvAppointments);
        layoutCounselorList = findViewById(R.id.layoutCounselorList);

        tvAppointmentStatus = findViewById(R.id.tvAppointmentStatus);

        scrollMyProfile = findViewById(R.id.scrollMyProfile);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUsernameProfile = findViewById(R.id.tvUsernameProfile);
        btnLogout = findViewById(R.id.btnLogout);

        btnChangeUsername = findViewById(R.id.btnChangeUsername);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnFeedback = findViewById(R.id.btnFeedback);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);

        bannerViewPager = findViewById(R.id.bannerViewPager);
        List<Integer> bannerImages = Arrays.asList(
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3
        );
        BannerAdapter bannerAdapter = new BannerAdapter(bannerImages);
        bannerViewPager.setAdapter(bannerAdapter);
        // 自动轮播逻辑
        sliderHandler = new android.os.Handler();
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = (bannerViewPager.getCurrentItem() + 1) % bannerImages.size();
                bannerViewPager.setCurrentItem(nextItem, true);
                sliderHandler.postDelayed(this, 2000);
            }
        };
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 2000);
            }
        });
        sliderHandler.postDelayed(sliderRunnable, 2000);

        btnChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        });//修改头像

        btnChangeUsername.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("输入新用户名");

            new AlertDialog.Builder(this)
                    .setTitle("修改用户名")
                    .setView(input)
                    .setPositiveButton("确认", (dialog, which) -> {
                        String newUsername = input.getText().toString().trim();
                        if (!newUsername.isEmpty()) {
                            boolean success = dbHelper.updateUsername(currentUsername, newUsername);
                            if (success) {
                                Toast.makeText(this, "用户名已更新", Toast.LENGTH_SHORT).show();
                                currentUsername = newUsername;
                                tvUsernameProfile.setText("用户名：" + currentUsername);
                            } else {
                                Toast.makeText(this, "更新失败（可能重名）", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });//修改用户名

        btnChangePassword.setOnClickListener(v -> {
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            EditText oldPwd = new EditText(this);
            oldPwd.setHint("原密码");
            EditText newPwd = new EditText(this);
            newPwd.setHint("新密码");
            layout.addView(oldPwd);
            layout.addView(newPwd);

            new AlertDialog.Builder(this)
                    .setTitle("修改密码")
                    .setView(layout)
                    .setPositiveButton("确认", (dialog, which) -> {
                        String oldP = oldPwd.getText().toString();
                        String newP = newPwd.getText().toString();
                        if (dbHelper.checkUserCredentials(currentUsername, oldP) >= 0) {
                            dbHelper.updateUserPassword(currentUsername, newP);
                            Toast.makeText(this, "密码已更新", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "原密码错误", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });//修改密码

        btnFeedback.setOnClickListener(v -> {
            Toast.makeText(this, "点击反馈给咨询师", Toast.LENGTH_SHORT).show();
            // TODO: 跳转选择咨询师并提交反馈
        });


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
                hideAllViews();

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
                    showMessageList();
                    return true;
                }
                // 如果是 “我的” 图标被点了
                else if (id == R.id.nav_profile) {
                    layoutCounselorList.setVisibility(View.GONE);
                    lvAppointments.setVisibility(View.GONE);
                    scrollMyProfile.setVisibility(View.VISIBLE);
                    showMyProfile();
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

    /**
     * 显示我的个人信息
     */
    private void showMyProfile() {
        tvUsernameProfile.setText(currentUsername);

        // 加载头像
        Cursor cursor = dbHelper.getUserByUsername(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            byte[] avatarBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("avatar"));
            if (avatarBytes != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                imgAvatar.setImageBitmap(bmp);
            }

            int role = cursor.getInt(cursor.getColumnIndexOrThrow("role"));
            String status;
            if (role == 0) {
                status = dbHelper.getLatestAppointmentStatusForUser(currentUsername);
            } else {
                status = dbHelper.getLatestAppointmentStatusForCounselor(currentUsername);
            }

            tvAppointmentStatus.setText("预约状态：" + status);
            cursor.close();
        }

        // 设置退出登录按钮
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(NormalUserHomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * 隐藏所有视图
     */
    private void hideAllViews() {
        layoutCounselorList.setVisibility(View.GONE);
        lvAppointments.setVisibility(View.GONE);
        scrollMyProfile.setVisibility(View.GONE);
        lvMessageList.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                byte[] imageBytes = getBytes(inputStream);
                dbHelper.updateUserAvatar(currentUsername, imageBytes);

                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imgAvatar.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "头像更新失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
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

        List<String> counselorNames = dbHelper.getBookedCounselorsForUser(currentUsername);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, counselorNames);
        lvMessageList.setAdapter(adapter);

        lvMessageList.setOnItemClickListener((parent, view, position, id) -> {
            String counselorName = counselorNames.get(position);
            Intent intent = new Intent(NormalUserHomeActivity.this, ChatActivity.class);
            intent.putExtra("sender", currentUsername);
            intent.putExtra("receiver", counselorName);
            startActivity(intent);
        });
    }


}

