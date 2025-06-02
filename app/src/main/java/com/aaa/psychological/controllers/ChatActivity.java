package com.aaa.psychological.controllers;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.aaa.psychological.R;
import com.aaa.psychological.adapters.MessageAdapter;
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.Message;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ListView lvMessages;
    private EditText etMessage;
    private Button btnSend;
    private DatabaseHelper dbHelper;
    private String sender, receiver;
    private MessageAdapter adapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sender = getIntent().getStringExtra("sender");
        receiver = getIntent().getStringExtra("receiver");

        dbHelper = new DatabaseHelper(this);
        lvMessages = findViewById(R.id.lvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        loadMessages();

        btnSend.setOnClickListener(v -> {
            String content = etMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                dbHelper.insertMessage(sender, receiver, content);
                etMessage.setText("");
                loadMessages(); // 重新加载
            }
        });
    }

    private void loadMessages() {
        messageList = dbHelper.getMessagesBetween(sender, receiver);
        adapter = new MessageAdapter(this, messageList, sender);
        lvMessages.setAdapter(adapter);
        lvMessages.setSelection(adapter.getCount() - 1);
    }
}
