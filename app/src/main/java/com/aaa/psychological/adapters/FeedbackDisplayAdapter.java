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
import com.aaa.psychological.models.FeedbackItem;

import java.util.List;

public class FeedbackDisplayAdapter extends BaseAdapter {
    private Context context;
    private List<FeedbackItem> feedbackList;

    public FeedbackDisplayAdapter(Context context, List<FeedbackItem> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @Override
    public int getCount() {
        return feedbackList.size();
    }

    @Override
    public Object getItem(int position) {
        return feedbackList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeedbackItem item = feedbackList.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_feedback_card, parent, false);
        }

        ImageView avatar = convertView.findViewById(R.id.imgUserAvatar);
        TextView tvName = convertView.findViewById(R.id.tvUserName);
        TextView tvContent = convertView.findViewById(R.id.tvFeedbackContent);
        TextView tvTime = convertView.findViewById(R.id.tvFeedbackTime);

        tvName.setText(item.getUsername());
        tvContent.setText(item.getContent());
        tvTime.setText(item.getTimestamp());

        if (item.getAvatar() != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(item.getAvatar(), 0, item.getAvatar().length);
            avatar.setImageBitmap(bmp);
        } else {
            avatar.setImageResource(R.drawable.ic_default_avatar);
        }

        return convertView;
    }
}

