package com.aaa.psychological.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aaa.psychological.R;
import com.aaa.psychological.models.Counselor;

import java.util.List;

public class CounselorAdapter extends RecyclerView.Adapter<CounselorAdapter.ViewHolder> {

    private final List<Counselor> counselorList;
    private final Context context;

    public CounselorAdapter(List<Counselor> counselorList, Context context) {
        this.counselorList = counselorList;
        this.context = context;
    }

    @NonNull
    @Override
    public CounselorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_counselor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CounselorAdapter.ViewHolder holder, int position) {
        Counselor c = counselorList.get(position);

        holder.tvCounselorName.setText(c.getName());
        holder.tvCounselorInfo.setText(c.getInfo());
        holder.tvCounselorTime.setText("可预约时间：" + c.getAvailable());

        // 头像加载：优先使用数据库中的 byte[] 数据
        if (c.getAvatarBytes() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(c.getAvatarBytes(), 0, c.getAvatarBytes().length);
            holder.imgCounselorAvatar.setImageBitmap(bitmap);
        } else {
            holder.imgCounselorAvatar.setImageResource(c.getAvatarResId());  // 默认头像
        }

        // 预约按钮点击事件
        holder.btnBook.setOnClickListener(v -> {
            // TODO：跳转到预约页面
            // 示例代码（未实现）：
            /*
            Intent intent = new Intent(context, AppointmentDetailActivity.class);
            intent.putExtra("counselorName", c.getName());
            intent.putExtra("counselorAvailable", c.getAvailable());
            context.startActivity(intent);
            */
        });
    }

    @Override
    public int getItemCount() {
        return counselorList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCounselorAvatar;
        TextView tvCounselorName, tvCounselorInfo, tvCounselorTime;
        Button btnBook;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCounselorAvatar = itemView.findViewById(R.id.imgCounselorAvatar);
            tvCounselorName = itemView.findViewById(R.id.tvCounselorName);
            tvCounselorInfo = itemView.findViewById(R.id.tvCounselorInfo);
            tvCounselorTime = itemView.findViewById(R.id.tvCounselorTime);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }
}
