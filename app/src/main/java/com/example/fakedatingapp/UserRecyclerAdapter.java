package com.example.fakedatingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder> {

    private ArrayList<User> mUserList;

    public UserRecyclerAdapter(ArrayList<User> mUserList) {
        this.mUserList = mUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ((ViewHolder)holder).name.setText(mUserList.get(position).getName() + ",");
        ((ViewHolder)holder).age.setText(Integer.toString(mUserList.get(position).getAge()));
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, age;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.fullName);
            age = itemView.findViewById(R.id.age);
        }
    }
}
