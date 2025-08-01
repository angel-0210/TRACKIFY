package com.example.trackify;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private OnTransactionChangedListener listener;
    private final Context context;
    private final DatabaseHelper databaseHelper;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        this.databaseHelper = new DatabaseHelper(context);
    }

    public void setOnTransactionChangedListener(OnTransactionChangedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.amountTextView.setText("₹ " + String.format("%.2f", transaction.getAmount()));
        holder.categoryTextView.setText(transaction.getCategory());
        holder.dateTextView.setText(transaction.getDate());

        // Edit transaction
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, addExpense.class);
            intent.putExtra("transaction_id", transaction.getId());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Required when context is not an activity
            context.startActivity(intent);
        });

        // Delete transaction
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        boolean deleted = databaseHelper.deleteTransaction(transaction.getId());
                        if (deleted) {
                            transactionList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, transactionList.size());
                            if (listener != null) listener.onTransactionChanged();
                            Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error deleting transaction", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public void updateData(List<Transaction> newTransactionList) {
        this.transactionList = newTransactionList;
        notifyDataSetChanged();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView amountTextView, categoryTextView, dateTextView;
        ImageButton editButton, deleteButton;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    public interface OnTransactionChangedListener {
        void onTransactionChanged();
    }
}

