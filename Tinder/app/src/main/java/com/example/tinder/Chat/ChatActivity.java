package com.example.tinder.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.tinder.Chat.ChatObject;
import com.example.tinder.MainActivity;
import com.example.tinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;
    private ArrayList<ChatObject> resultChat = new ArrayList<>();

    private EditText mSendEditText;

    private Button mSendButton;

    private String currentUserId, matchId, currentUserGender, currentOppositeGender, chatId;

    private DatabaseReference mDataBaseUser, mDatabaseChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUserGender = MainActivity.userSex;
        currentOppositeGender = MainActivity.oppositeUserSex;

        matchId = getIntent().getExtras().getString("matchId");

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDataBaseUser = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserGender).child(currentUserId).child("connections").child("matches").child(matchId).child("chatId");
        mDatabaseChat = FirebaseDatabase.getInstance().getReference().child("chat");

        getChatId();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mRecyclerView.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new ChatAdapter(getDataSetChat(), ChatActivity.this);
        mRecyclerView.setAdapter(mChatAdapter);

        mSendEditText = findViewById(R.id.message);
        mSendButton = findViewById(R.id.send);

        mSendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                sendMessage();

            }
        });
    }

    private void sendMessage() {
        String sendMessageText = mSendEditText.getText().toString();
        if (!sendMessageText.isEmpty()) {
            DatabaseReference newMessageDb = mDatabaseChat.push();

            Map newMessage = new HashMap();
            newMessage.put("createdByUser", currentUserId);
            newMessage.put("text", sendMessageText);

            newMessageDb.setValue(newMessage);

            mSendEditText.setText(null);
        }
    }

    private void getChatId() {
        mDataBaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    chatId = dataSnapshot.getValue().toString();
                    mDatabaseChat = mDatabaseChat.child(chatId);
                    getChatMessages();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getChatMessages() {
        mDatabaseChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    String message = null;
                    String createdByUser = null;
                    if(dataSnapshot.child("text").getValue()!=null){
                        message = dataSnapshot.child("text").getValue().toString();
                    }
                    if(dataSnapshot.child("createdByUser").getValue()!=null){
                        createdByUser = dataSnapshot.child("createdByUser").getValue().toString();
                    }

                    if(message!=null && createdByUser!=null){
                        Boolean currentUserBoolean=false;
                        if(createdByUser.equals(currentUserId)){
                            currentUserBoolean=true;
                        }

                        ChatObject newMessage = new ChatObject(message,currentUserBoolean);
                        resultChat.add(newMessage);
                        mChatAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private List<ChatObject> getDataSetChat() {
        return resultChat;
    }
}
