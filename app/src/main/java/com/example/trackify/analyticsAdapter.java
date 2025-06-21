package com.example.trackify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class analyticsAdapter extends RecyclerView.Adapter<analyticsAdapter.ViewHolder> {

    private List<CategoryData> categoryDataList;

    public analyticsAdapter(List<CategoryData> categoryDataList) {
        this.categoryDataList = categoryDataList;
    }

    @NonNull
    @Override
    public analyticsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull analyticsAdapter.ViewHolder holder, int position) {
        CategoryData data = categoryDataList.get(position);
        holder.categoryName.setText(data.getCategory());
        holder.categoryTotal.setText("₹" + String.format("%.2f", data.getTotalAmount()));
    }

    @Override
    public int getItemCount() {
        return categoryDataList.size();
    }

    public void updateData(List<CategoryData> newData) {
        categoryDataList = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, categoryTotal;

        ViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            categoryTotal = itemView.findViewById(R.id.categoryTotal);
        }
    }
}
