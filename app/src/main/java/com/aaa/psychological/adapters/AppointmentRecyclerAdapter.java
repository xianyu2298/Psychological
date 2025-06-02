package com.aaa.psychological.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaa.psychological.R;
import com.aaa.psychological.models.Appointment;

import java.util.List;

public class AppointmentRecyclerAdapter extends RecyclerView.Adapter<AppointmentRecyclerAdapter.ViewHolder> {

    private final Context context;
    private final List<Appointment> appointmentList;

    public AppointmentRecyclerAdapter(Context context, List<Appointment> appointments) {
        this.context = context;
        this.appointmentList = appointments;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvUserName, tvStatus, tvTime;

        public ViewHolder(View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUserName = itemView.findViewById(R.id.tvCounselorName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTime = itemView.findViewById(R.id.tvAppointmentTime);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Appointment appt = appointmentList.get(position);
        holder.tvUserName.setText("用户名：" + appt.getCounselorName());
        holder.tvTime.setText("预约时间：" + appt.getTime());

        String status = appt.getStatus();
        holder.tvStatus.setText(status);

        switch (status) {
            case "已预约":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_reserved);
                holder.tvStatus.setTextColor(context.getColor(R.color.orange));
                break;
            case "心理治疗中":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_in_session);
                holder.tvStatus.setTextColor(context.getColor(R.color.blue));
                break;
            case "已完成":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                holder.tvStatus.setTextColor(context.getColor(R.color.green));
                break;
            default:
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_reserved);
                holder.tvStatus.setTextColor(context.getColor(R.color.gray));
                break;
        }

        if (appt.getAvatarBytes() != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(appt.getAvatarBytes(), 0, appt.getAvatarBytes().length);
            holder.imgAvatar.setImageBitmap(bmp);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }


    @Override
    public int getItemCount() {
        return appointmentList.size();
    }
}

