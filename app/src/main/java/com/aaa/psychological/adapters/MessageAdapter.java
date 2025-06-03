package com.aaa.psychological.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.*;
import android.widget.*;
import com.aaa.psychological.R;
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.Message;

import java.util.List;

public class MessageAdapter extends BaseAdapter {
    private final List<Message> messages;
    private final LayoutInflater inflater;
    private final String currentUser;
    private final Context context;

    public MessageAdapter(Context context, List<Message> messages, String currentUser) {
        this.context = context;
        this.messages = messages;
        this.inflater = LayoutInflater.from(context);
        this.currentUser = currentUser;
    }

    @Override
    public int getCount() { return messages.size(); }

    @Override
    public Object getItem(int position) { return messages.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message msg = messages.get(position);
        boolean isSelf = msg.getSender().equals(currentUser);

        if (isSelf) {
            convertView = inflater.inflate(R.layout.item_message_right, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.item_message_left, parent, false);
        }

        TextView tvContent = convertView.findViewById(R.id.tvMessage);
        ImageView imgAvatar = convertView.findViewById(R.id.imgAvatar);
        tvContent.setText(msg.getContent());

        // 获取头像（示例从 DatabaseHelper 中查找用户头像）
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Cursor cursor = dbHelper.getUserByUsername(msg.getSender());

        if (cursor != null && cursor.moveToFirst()) {
            byte[] avatarBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("avatar"));
            if (avatarBytes != null && avatarBytes.length > 0) {
                Bitmap bmp = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                imgAvatar.setImageBitmap(bmp);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_default_avatar); // 默认头像
            }
            cursor.close();
        } else {
            imgAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        return convertView;
    }

}
