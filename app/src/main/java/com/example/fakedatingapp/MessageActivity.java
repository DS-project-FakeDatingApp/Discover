package com.example.fakedatingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.imperiumlabs.geofirestore.GeoFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageActivity extends AppCompatActivity {

    private FirebaseFirestore mDb;
    private User mUser;
    private FirebaseUser fUser;
    private CollectionReference mCr;
    Intent intent;

    Button send;
    EditText message;

    MessageRecyclerAdapter mMessageRecyclerAdapter;
    private ArrayList<Message> mMessageList;
    private RecyclerView mMessageListRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        send = findViewById(R.id.send);
        message = findViewById(R.id.message);

        mDb = FirebaseFirestore.getInstance();
        mMessageList = new ArrayList<>();

        mMessageListRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMessageListRecyclerView.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        final String userId = intent.getStringExtra("userId");
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message.getText().toString();
                if(!msg.equals("")) {
                    sendMessage(fUser.getUid(), userId, msg);
                }
                else {
                    Toast.makeText(MessageActivity.this, "No message to be sent", Toast.LENGTH_SHORT).show();
                }
                message.setText("");

            }
        });

        readMessage(fUser.getUid(), userId);


    }

    private void sendMessage(String sender, String receiver, String message) {
        Message messageElem = new Message(sender, receiver, message);

        mDb.collection("Chats")
                .add(messageElem)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        System.out.println("added*@#*)#");
                        Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error adding document", e);
                    }
                });
    }

    private void readMessage(final String myId, final String userId) {

        mDb.collection("Chats")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        for(QueryDocumentSnapshot query : queryDocumentSnapshots) {
                            Message message = query.toObject(Message.class);

                            if(message.getReceiver().equals(myId) && message.getSender().equals(userId) ||
                            message.getReceiver().equals(userId) && message.getSender().equals(myId)) {
                                mMessageList.add(message);
                            }

                            mMessageRecyclerAdapter = new MessageRecyclerAdapter(MessageActivity.this, mMessageList);
                            mMessageListRecyclerView.setAdapter(mMessageRecyclerAdapter);

                        }
                    }
                });

    }
}