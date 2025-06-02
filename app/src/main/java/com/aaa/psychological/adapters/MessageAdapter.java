package com.aaa.psychological.adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;
import com.aaa.psychological.R;
import com.aaa.psychological.models.Message;

import java.util.List;

public class MessageAdapter extends BaseAdapter {
    private final List<Message> messages;
    private final LayoutInflater inflater;
    private final String currentUser;

    public MessageAdapter(Context context, List<Message> messages, String currentUser) {
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
        if (msg.getSender().equals(currentUser)) {
            convertView = inflater.inflate(R.layout.item_message_right, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.item_message_left, parent, false);
        }

        TextView tvContent = convertView.findViewById(R.id.tvContent);
        tvContent.setText(msg.getContent());

        return convertView;
    }
}
