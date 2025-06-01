package com.aaa.psychological.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aaa.psychological.R;
import com.aaa.psychological.models.Appointment;

import java.util.List;

public class AppointmentAdapter extends ArrayAdapter<Appointment> {

    private final Context context;
    private final List<Appointment> appointments;

    public AppointmentAdapter(Context context, List<Appointment> appointments) {
        super(context, 0, appointments);
        this.context = context;
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Appointment appt = appointments.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        }

        ImageView imgAvatar = convertView.findViewById(R.id.imgAvatar);
        TextView tvName = convertView.findViewById(R.id.tvCounselorName);
        TextView tvTime = convertView.findViewById(R.id.tvAppointmentTime);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);

        tvName.setText(appt.getCounselorName());
        tvTime.setText("预约时间：" + appt.getTime());
        tvStatus.setText(appt.getStatus());

        if (appt.getAvatarBytes() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(appt.getAvatarBytes(), 0, appt.getAvatarBytes().length);
            imgAvatar.setImageBitmap(bitmap);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        return convertView;
    }
}
