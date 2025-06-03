package com.aaa.psychological.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaa.psychological.R;
import com.aaa.psychological.models.FeedbackTarget;

import java.util.List;

public class FeedbackAdapter extends BaseAdapter {
    private Context context;
    private List<FeedbackTarget> feedbackTargets;

    public FeedbackAdapter(Context context, List<FeedbackTarget> feedbackTargets) {
        this.context = context;
        this.feedbackTargets = feedbackTargets;
    }

    @Override
    public int getCount() {
        return feedbackTargets.size();
    }

    @Override
    public Object getItem(int position) {
        return feedbackTargets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeedbackTarget item = feedbackTargets.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_feedback_counselor, parent, false);
        }

        TextView tvName = convertView.findViewById(R.id.tvCounselorName);
        TextView tvTime = convertView.findViewById(R.id.tvCompletionTime);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);
        ImageView imgAvatar = convertView.findViewById(R.id.imgCounselorAvatar);

        tvName.setText("咨询师：" + item.getCounselorName());
        tvTime.setText("完成时间：" + item.getCompletedTime());
        tvStatus.setText("状态：已完成");

        if (item.getAvatar() != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(item.getAvatar(), 0, item.getAvatar().length);
            imgAvatar.setImageBitmap(bmp);
        }

        return convertView;
    }
}

