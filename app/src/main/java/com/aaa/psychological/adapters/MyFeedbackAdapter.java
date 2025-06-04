package com.aaa.psychological.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaa.psychological.R;
import com.aaa.psychological.models.MyFeedbackItem;

import java.util.List;

public class MyFeedbackAdapter extends ArrayAdapter<MyFeedbackItem> {

    private Context context;
    private List<MyFeedbackItem> feedbackList;

    public MyFeedbackAdapter(Context context, List<MyFeedbackItem> feedbackList) {
        super(context, R.layout.item_feedback_card, feedbackList);
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 获取数据项
        MyFeedbackItem item = getItem(position);

        // 使用已有的 item_feedback_card 布局
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_feedback_card, parent, false);
        }

        // 获取控件
        ImageView imgUserAvatar = convertView.findViewById(R.id.imgUserAvatar);
        TextView tvUserName = convertView.findViewById(R.id.tvUserName);
        TextView tvFeedbackContent = convertView.findViewById(R.id.tvFeedbackContent);
        TextView tvFeedbackTime = convertView.findViewById(R.id.tvFeedbackTime);

        // 设置数据
        tvUserName.setText("给"+item.getCounselorName()+"的评价");  // 显示咨询师名字
        tvFeedbackContent.setText("评价内容："+item.getContent());  // 显示评价内容
        tvFeedbackTime.setText("评价时间"+item.getTimestamp());  // 显示评价时间

        // 设置头像
        if (item.getAvatar() != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(item.getAvatar(), 0, item.getAvatar().length);
            imgUserAvatar.setImageBitmap(bmp);
        } else {
            imgUserAvatar.setImageResource(R.drawable.ic_default_avatar);  // 默认头像
        }

        return convertView;
    }
}

