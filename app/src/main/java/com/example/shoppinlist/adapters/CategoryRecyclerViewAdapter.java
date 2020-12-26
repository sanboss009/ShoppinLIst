package com.example.shoppinlist.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppinlist.R;
import com.example.shoppinlist.activities.ItemDetailView;
import com.example.shoppinlist.models.Category;

import java.util.ArrayList;

public class CategoryRecyclerViewAdapter extends RecyclerView.Adapter<CategoryRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Category> categories = new ArrayList<>();
    Context context ;
    public CategoryRecyclerViewAdapter(ArrayList<Category> categories,Context context) {
        this.categories = categories;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View categoryView  = inflater.inflate(R.layout.category_recycler_view_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(categoryView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryRecyclerViewAdapter.ViewHolder holder, int position) {
        final Category category = categories.get(position);
        TextView textView = holder.nameTextView;
        LinearLayout layout = holder.categoryLayout;
        textView.setText(category.getName());
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ItemDetailView.class);
                intent.putExtra("collectionId",category.getId());
                intent.putExtra("categoryName",category.getName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView ;
        LinearLayout categoryLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.category_list_textview);
            categoryLayout = itemView.findViewById(R.id.category_list_linerlayout);
        }
    }
}
