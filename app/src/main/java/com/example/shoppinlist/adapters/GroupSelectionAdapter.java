package com.example.shoppinlist.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppinlist.R;
import com.example.shoppinlist.activities.MainActivity;
import com.example.shoppinlist.models.Group;
import com.example.shoppinlist.models.ShoppinListConstants;
import com.example.shoppinlist.models.UserGroup;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class GroupSelectionAdapter extends RecyclerView.Adapter<GroupSelectionAdapter.ViewHolder> {
    ArrayList<UserGroup> userGroups = new ArrayList<>();
    Context context;
    Activity activity;

    public GroupSelectionAdapter(ArrayList<UserGroup> userGroups, Context context, Activity activity) {
        this.userGroups = userGroups;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View groupView = inflater.inflate(R.layout.group_item, parent, false);
        ViewHolder holder = new ViewHolder(groupView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final UserGroup userGroup = userGroups.get(position);
        Button button = holder.groupItem;
        button.setText(userGroup.getName());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = context.getSharedPreferences(ShoppinListConstants.CONSTANT_SHOPPING_LIST_SHARED_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(ShoppinListConstants.CONSTANT_GROUP_ID_STRING, userGroup.getGroup_id());
                editor.apply();
                editor.commit();

                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra(ShoppinListConstants.CONSTANT_GROUP_ID_STRING, userGroup.getGroup_id());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                activity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userGroups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button groupItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupItem = itemView.findViewById(R.id.group_item_button);
        }
    }
}
