package com.aaa.psychological.controllers;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.aaa.psychological.R;
import com.aaa.psychological.adapters.MessageAdapter;
import com.aaa.psychological.helpers.DatabaseHelper;
import com.aaa.psychological.models.Message;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ListView lvMessages;
    private EditText etMessage;
    private Button btnSend;
    private DatabaseHelper dbHelper;
    private String sender, receiver;
    private MessageAdapter adapter;
    private List<Message> messageList;

    private boolean isCounselor;
    private Toolbar chatToolbar;

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

        chatToolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(chatToolbar);

        sender = getIntent().getStringExtra("sender");
        receiver = getIntent().getStringExtra("receiver");
        isCounselor = getIntent().getBooleanExtra("isCounselor", false); // 用于判断角色

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("与 " + receiver + " 聊天");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isCounselor) {
            getMenuInflater().inflate(R.menu.menu_chat, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_end_treatment) {
            new AlertDialog.Builder(this)
                    .setTitle("确认")
                    .setMessage("确定要结束治疗吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        DatabaseHelper dbHelper = new DatabaseHelper(this);
                        dbHelper.updateAppointmentStatus(receiver, sender, "已完成"); // receiver 是用户，sender 是咨询师
                        Toast.makeText(this, "治疗已结束", Toast.LENGTH_SHORT).show();
                        finish(); // 可选：返回上一页面
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadMessages() {
        messageList = dbHelper.getMessagesBetween(sender, receiver);
        adapter = new MessageAdapter(this, messageList, sender);
        lvMessages.setAdapter(adapter);
        lvMessages.setSelection(adapter.getCount() - 1);
    }
}
