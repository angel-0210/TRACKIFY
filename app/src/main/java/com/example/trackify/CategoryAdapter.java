package com.example.trackify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<CategoryTotal> categoryList;

    public CategoryAdapter(List<CategoryTotal> categoryList) {
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_total, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryTotal categoryTotal = categoryList.get(position);
        holder.categoryName.setText(categoryTotal.getCategory());
        holder.totalAmount.setText("\n ₹" + String.format("%.2f", categoryTotal.getTotalAmount()));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void updateList(List<CategoryTotal> newList) {
        categoryList = newList;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, totalAmount;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            totalAmount = itemView.findViewById(R.id.totalAmount);
        }
    }
}
