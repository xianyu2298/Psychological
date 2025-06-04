package com.aaa.psychological.controllers;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class CounselorHomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DatabaseHelper dbHelper;
    private String currentUsername;

    private LinearLayout layoutCounselorList;
    private RecyclerView rvCounselorList;
    private TextView tvEmptyPlaceholder, btnFeedback,btnViewFeedback;
    private BottomNavigationView bottomNav;

    private NestedScrollView scrollMyProfile;
    private ImageView imgAvatar;
    private TextView tvUsernameProfile, tvAppointmentStatus;
    private TextView btnChangeUsername,btnChangePassword,btnChangeAvatar;
    private Button btnLogout;

    private ListView lvAppointments;

    private Handler sliderHandler;
    private Runnable sliderRunnable;
    private ViewPager2 bannerViewPager;

    private ListView lvMessageList;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001; // 请求码


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

        btnViewFeedback = findViewById(R.id.btnViewFeedback);  // 新增按钮
        btnViewFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(CounselorHomeActivity.this, ViewFeedbacksActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        btnChangeUsername = findViewById(R.id.btnChangeUsername);  // 初始化修改用户名按钮
        btnChangePassword = findViewById(R.id.btnChangePassword);  // 初始化修改密码按钮
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);  // 初始化修改头像按钮


        btnChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        });
        //用户名修改
        btnChangeUsername.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("输入新用户名");

            new AlertDialog.Builder(this)
                    .setTitle("修改用户名")
                    .setView(input)
                    .setPositiveButton("确认", (dialog, which) -> {
                        String newUsername = input.getText().toString().trim();
                        if (!newUsername.isEmpty()) {
                            // 更新数据库中的所有相关表格
                            boolean success = dbHelper.updateUsername(currentUsername, newUsername, getCurrentUserAvatarBytes());
                            if (success) {
                                Toast.makeText(this, "用户名已更新", Toast.LENGTH_SHORT).show();
                                currentUsername = newUsername;
                                tvUsernameProfile.setText("用户名：" + currentUsername);

                                // 头像同步更新
                                loadUserProfile();  // 重新加载用户头像等信息
                            } else {
                                Toast.makeText(this, "更新失败（可能重名）", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

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

                        // 校验旧密码是否正确
                        if (dbHelper.checkUserCredentials(currentUsername, oldP) >= 0) {
                            // 调用 updateUserPassword 方法更新密码
                            dbHelper.updateUserPassword(currentUsername, newP);
                            Toast.makeText(this, "密码已更新", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "原密码错误", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
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
        List<Appointment> all = dbHelper.getAppointmentsByCounselor(currentUsername);

        // 按用户名去重，保留状态为“预约”和“心理治疗中”的最新记录
        LinkedHashMap<String, Appointment> uniqueMap = new LinkedHashMap<>();
        for (Appointment a : all) {
            if ("已预约".equals(a.getStatus())||"心理治疗中".equals(a.getStatus())) {
                if (!uniqueMap.containsKey(a.getName())) {
                    uniqueMap.put(a.getName(), a);  // 保留首个“预约”和“心理治疗中”状态的记录
                }
            }
        }

        List<Appointment> displayList = new ArrayList<>(uniqueMap.values());

        if (displayList.isEmpty()) {
            tvEmptyPlaceholder.setVisibility(View.VISIBLE);
            rvCounselorList.setVisibility(View.GONE);
        } else {
            tvEmptyPlaceholder.setVisibility(View.GONE);
            rvCounselorList.setVisibility(View.VISIBLE);
            AppointmentRecyclerAdapter adapter = new AppointmentRecyclerAdapter(this, displayList);
            adapter.setOnItemClickListener(appt -> showAppointmentDialog(appt));
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
            // 获取当前状态，防止重复治疗
            if ("心理治疗中".equals(appt.getStatus())) {
                Toast.makeText(CounselorHomeActivity.this, "该用户已在治疗中", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            dbHelper.updateAppointmentStatus(appt.getName(), currentUsername, "心理治疗中");
            loadAppointmentsForCounselor();
            Toast.makeText(CounselorHomeActivity.this, "已开始治疗", Toast.LENGTH_SHORT).show();
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

        // 使用去重后的用户列表
        List<String> uniqueUsers = dbHelper.getUniqueBookedUsersForCounselor(currentUsername);

        ChatListAdapter adapter = new ChatListAdapter(this, uniqueUsers, currentUsername);
        lvMessageList.setAdapter(adapter);

        lvMessageList.setOnItemClickListener((parent, view, position, id) -> {
            String user = uniqueUsers.get(position);
            Intent intent = new Intent(CounselorHomeActivity.this, ChatActivity.class);
            intent.putExtra("sender", currentUsername);
            intent.putExtra("receiver", user);
            intent.putExtra("isCounselor", true);
            startActivity(intent);
        });
    }



    protected void onResume() {
        super.onResume();
        if (lvMessageList.getVisibility() == View.VISIBLE) {
            showMessageList(); // 重新加载消息列表
        }
    }
    // 辅助方法：获取当前用户头像的字节数组
    private byte[] getCurrentUserAvatarBytes() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.getUserByUsername(currentUsername);  // 获取用户名为 currentUsername 的用户
        byte[] avatarBytes = null;
        if (cursor != null && cursor.moveToFirst()) {
            // 使用 getColumnIndexOrThrow 确保列名正确
            avatarBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("avatar"));
            cursor.close();
        }
        return avatarBytes;
    }
    public void loadUserProfile() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.getUserByUsername(currentUsername);

        if (cursor != null && cursor.moveToFirst()) {
            // 使用 getColumnIndexOrThrow 确保列名正确
            String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            byte[] avatarBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("avatar"));

            // 更新头像
            ImageView imgAvatar = findViewById(R.id.imgAvatar);
            if (avatarBytes != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                imgAvatar.setImageBitmap(bmp);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_default_avatar);  // 默认头像
            }

            // 更新用户名
            TextView tvUsername = findViewById(R.id.tvUsernameProfile);
            tvUsername.setText("用户名：" + username);
        }
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
}