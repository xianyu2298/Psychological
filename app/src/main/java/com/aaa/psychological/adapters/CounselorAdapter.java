package com.aaa.psychological.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaa.psychological.R;
import com.aaa.psychological.controllers.CounselorDetailActivity;
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.Counselor;

import java.util.List;

public class CounselorAdapter extends RecyclerView.Adapter<CounselorAdapter.ViewHolder> {

    private final List<Counselor> counselorList;
    private final Context context;
    private final String currentUsername;

    public CounselorAdapter(List<Counselor> list, Context context, String currentUsername) {
        this.counselorList = list;
        this.context = context;
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_counselor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Counselor c = counselorList.get(position);
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        holder.tvCounselorName.setText(c.getName());
        holder.tvCounselorInfo.setText(c.getInfo());
        holder.tvCounselorTime.setText("可预约时间：" + c.getAvailable());

        // 显示咨询师头像
        if (c.getAvatarBytes() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(c.getAvatarBytes(), 0, c.getAvatarBytes().length);
            holder.imgCounselorAvatar.setImageBitmap(bitmap);
        } else {
            holder.imgCounselorAvatar.setImageResource(c.getAvatarResId());
        }

        // 判断是否已预约
        boolean alreadyBooked = dbHelper.hasUserAlreadyBooked(currentUsername, c.getName());
        updateButtonState(holder.btnBook, alreadyBooked);

        // 点击按钮逻辑
        holder.btnBook.setOnClickListener(v -> {
            boolean booked = dbHelper.hasUserAlreadyBooked(currentUsername, c.getName());

            if (booked) {
                dbHelper.deleteAppointment(currentUsername, c.getName());
                updateButtonState(holder.btnBook, false);
                Toast.makeText(context, "已取消预约", Toast.LENGTH_SHORT).show();
            } else {
                // 校验当前时间是否在可预约时间段
                String availableTime = dbHelper.getCounselorAvailableTime(c.getName());
                if (!isWithinAvailableTime(availableTime)) {
                    Toast.makeText(context, "当前时间不在咨询师可预约时间段内", Toast.LENGTH_LONG).show();
                    return;
                }

                // 获取用户头像
                Cursor cursor = dbHelper.getUserByUsername(currentUsername);
                byte[] userAvatar = null;
                if (cursor != null && cursor.moveToFirst()) {
                    userAvatar = cursor.getBlob(cursor.getColumnIndexOrThrow("avatar"));
                    cursor.close();
                }

                boolean success = dbHelper.insertAppointment(currentUsername, c.getName(), userAvatar, c.getAvatarBytes());
                if (success) {
                    updateButtonState(holder.btnBook, true);
                    Toast.makeText(context, "预约成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "预约失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CounselorDetailActivity.class);
            intent.putExtra("username", c.getName()); // 咨询师用户名
            context.startActivity(intent);
        });


    }

    private void updateButtonState(TextView button, boolean booked) {
        if (booked) {
            button.setText("已预约");
            button.setBackgroundResource(R.drawable.bg_status_reserved);
            button.setTextColor(context.getResources().getColor(R.color.orange));
        } else {
            button.setText("预约");
            button.setBackgroundResource(R.drawable.bg_button_book);
            button.setTextColor(context.getResources().getColor(android.R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return counselorList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCounselorAvatar;
        TextView tvCounselorName, tvCounselorInfo, tvCounselorTime;
        TextView btnBook;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCounselorAvatar = itemView.findViewById(R.id.imgCounselorAvatar);
            tvCounselorName = itemView.findViewById(R.id.tvCounselorName);
            tvCounselorInfo = itemView.findViewById(R.id.tvCounselorInfo);
            tvCounselorTime = itemView.findViewById(R.id.tvCounselorTime);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }

    private boolean isWithinAvailableTime(String availableTime) {
        if (availableTime == null || availableTime.trim().isEmpty()) return false;

        java.util.Calendar now = java.util.Calendar.getInstance();
        int dayOfWeek = now.get(java.util.Calendar.DAY_OF_WEEK);
        int hour = now.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = now.get(java.util.Calendar.MINUTE);
        int currentMinutes = hour * 60 + minute;
        String currentDay = getChineseWeekday(dayOfWeek);

        String[] lines = availableTime.split("\n");
        for (String line : lines) {
            if (!line.startsWith(currentDay)) continue;

            // 例如 line: 周一 09:00-17:00
            line = line.replace(currentDay, "").trim(); // 得到 09:00-17:00
            if (!line.contains("-")) continue;

            String[] range = line.split("-");
            if (range.length != 2) continue;

            int startMinutes = timeToMinutes(range[0].trim());
            int endMinutes = timeToMinutes(range[1].trim());

            if (currentMinutes >= startMinutes && currentMinutes <= endMinutes) {
                return true;
            }
        }
        return false;
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        if (parts.length != 2) return -1;
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }


    private String getChineseWeekday(int day) {
        switch (day) {
            case java.util.Calendar.MONDAY: return "周一";
            case java.util.Calendar.TUESDAY: return "周二";
            case java.util.Calendar.WEDNESDAY: return "周三";
            case java.util.Calendar.THURSDAY: return "周四";
            case java.util.Calendar.FRIDAY: return "周五";
            case java.util.Calendar.SATURDAY: return "周六";
            case java.util.Calendar.SUNDAY: return "周日";
            default: return "";
        }
    }

}
