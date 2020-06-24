package com.example.fakedatingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class DiscoverActivity extends AppCompatActivity {

    private RecyclerView mUserListRecyclerView;
    private UserRecyclerAdapter mUserRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        ArrayList<User> nUserList = getIntent().getParcelableArrayListExtra("User List");

        mUserListRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mUserListRecyclerView.setLayoutManager(linearLayoutManager);

        mUserRecyclerAdapter = new UserRecyclerAdapter(this, nUserList);
        mUserListRecyclerView.setAdapter(mUserRecyclerAdapter);

        if(nUserList.size() == 0 || nUserList.size() == 1) {
            Toast.makeText(getApplicationContext(), "Sorry, no users nearby", Toast.LENGTH_SHORT).show();
        }

    }

}