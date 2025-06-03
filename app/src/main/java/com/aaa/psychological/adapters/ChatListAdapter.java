package com.aaa.psychological.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaa.psychological.R;
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.Message;

import java.util.List;

public class ChatListAdapter extends BaseAdapter {
    private final Context context;
    private final List<String> counselorNames;
    private final String currentUsername;
    private final DatabaseHelper dbHelper;

    public ChatListAdapter(Context context, List<String> names, String currentUsername) {
        this.context = context;
        this.counselorNames = names;
        this.currentUsername = currentUsername;
        this.dbHelper = new DatabaseHelper(context);
    }

    @Override
    public int getCount() {
        return counselorNames.size();
    }

    @Override
    public Object getItem(int i) {
        return counselorNames.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    static class ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvLastMessage, tvTime;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_preview, parent, false);
            holder = new ViewHolder();
            holder.imgAvatar = convertView.findViewById(R.id.imgAvatar);
            holder.tvName = convertView.findViewById(R.id.tvName);
            holder.tvLastMessage = convertView.findViewById(R.id.tvLastMessage);
            holder.tvTime = convertView.findViewById(R.id.tvTime);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String counselorName = counselorNames.get(i);
        holder.tvName.setText(counselorName);

        // 加载头像
        Cursor cursor = dbHelper.getUserByUsername(counselorName);
        if (cursor != null && cursor.moveToFirst()) {
            byte[] avatarBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("avatar"));
            if (avatarBytes != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                holder.imgAvatar.setImageBitmap(bmp);
            }
            cursor.close();
        }

        // 获取最近消息
        List<Message> messages = dbHelper.getMessagesBetween(currentUsername, counselorName);
        if (!messages.isEmpty()) {
            Message last = messages.get(messages.size() - 1);
            holder.tvLastMessage.setText(last.getContent());
            holder.tvTime.setText(last.getTimestamp().substring(11, 16)); // 显示 HH:mm
        } else {
            holder.tvLastMessage.setText("暂无消息");
            holder.tvTime.setText("");
        }

        return convertView;
    }
}
