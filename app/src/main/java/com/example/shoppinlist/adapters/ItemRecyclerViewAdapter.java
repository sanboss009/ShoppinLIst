package com.example.shoppinlist.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppinlist.R;
import com.example.shoppinlist.activities.ItemDetailView;
import com.example.shoppinlist.models.Item;

import java.util.ArrayList;

import com.example.shoppinlist.models.ShoppinListConstants;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder> {
    ArrayList<Item> items ;
    Context mContext;
    String TAG = ShoppinListConstants.CONSTANT_LOG_TAG;
    public ItemRecyclerViewAdapter(ArrayList<Item> items, Context mContext) {
        this.items =items;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_recycler_view_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemRecyclerViewAdapter.ViewHolder holder, int position) {
        final Item item = items.get(position);
        Log.d(TAG,item.getName()+" added in " +item.getStatus()+" recycler view");
        TextView textView = holder.itemNameTextView;
        final CheckBox checkBox = holder.itemCheckBox;
        Button deleteButton = holder.deleteItem;
        textView.setText(item.getName());
        if(item.getStatus().equals(ShoppinListConstants.CONSTANT_ITEM_STATUS_COMPLETED_STRING)){
            checkBox.setChecked(true);
        }

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mContext instanceof ItemDetailView){
                    if(checkBox.isChecked()){
                        Log.d(TAG,"item "+item.getName()+" is checked");
                        item.setStatus(ShoppinListConstants.CONSTANT_ITEM_STATUS_COMPLETED_STRING);
                    }else {
                        Log.d(TAG, "item " + item.getName() + " is unchecked");
                        item.setStatus(ShoppinListConstants.CONSTANT_ITEM_STATUS_ACTIVE_STRING);
                    }
                    ((ItemDetailView) mContext).updateStatus(item);
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletedItemAlert(view,item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        CheckBox itemCheckBox;
        Button deleteItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.item_text_view);
            itemCheckBox = itemView.findViewById(R.id.item_checkbox);
            deleteItem = itemView.findViewById(R.id.delete_item);
        }
    }

    public void deletedItemAlert(View view, final Item item){
        Log.d(TAG, "Delete item " + item.getName() + " ?");
        final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
        materialAlertDialogBuilder
                .setTitle("Confirm")
                .setMessage("Are you sure you want to delete item?")
                .setCancelable(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "item " + item.getName() + " is not deleting");
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(mContext instanceof ItemDetailView){
                            ((ItemDetailView) mContext).deleteItem(item);
                        }
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }


}

