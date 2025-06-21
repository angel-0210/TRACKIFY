package com.example.trackify;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HomeFragment extends Fragment {

    private TextView greetingTextView, balanceTextView, dailyTotalTextView, weeklyTotalTextView, monthlyTotalTextView, recentTransactionsTitle;
    private RecyclerView recentTransactionsRecyclerView;
    private FloatingActionButton addExpenseFab;
    private DatabaseHelper databaseHelper;
    private TransactionAdapter transactionAdapter;
    private int userId;
    private String userName;
    private ActivityResultLauncher<Intent> addExpenseLauncher;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Button profileButton = view.findViewById(R.id.profileButton);

        greetingTextView = view.findViewById(R.id.greetingText);
        balanceTextView = view.findViewById(R.id.balanceText);
        recentTransactionsTitle = view.findViewById(R.id.recentTransactionsTitle);
        dailyTotalTextView = view.findViewById(R.id.dailyTotalTextView);
        weeklyTotalTextView = view.findViewById(R.id.weeklyTotalTextView);
        monthlyTotalTextView = view.findViewById(R.id.monthlyTotalTextView);
        recentTransactionsRecyclerView = view.findViewById(R.id.recentTransactionsRecycler);
        addExpenseFab = view.findViewById(R.id.addExpense);

        databaseHelper = new DatabaseHelper(requireContext());
        userId = SessionManager.getUserId(requireContext());
        userName = SessionManager.getUserName(requireContext());

        setupRecyclerView();
        loadDashboardData();

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AccountFragment.class);
            startActivity(intent);
        });

        addExpenseLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        reloadData();
                    }
                }
        );

        addExpenseFab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), addExpense.class);
            addExpenseLauncher.launch(intent);
        });

        // Personalized greeting
        greetingTextView.setText("Hello, \n" + userName);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadData(); // Always reload when HomeFragment is visible again
    }

    private void setupRecyclerView() {
        recentTransactionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<Transaction> transactionList = databaseHelper.getRecentTransactions(userId, 10);
        transactionAdapter = new TransactionAdapter(requireContext(), transactionList);
        transactionAdapter.setOnTransactionChangedListener(this::loadDashboardData);
        recentTransactionsRecyclerView.setAdapter(transactionAdapter);
    }

    private void reloadData() {
        loadDashboardData();

        List<Transaction> transactionList = databaseHelper.getRecentTransactions(userId, 10);
        if (transactionAdapter == null) {
            transactionAdapter = new TransactionAdapter(requireContext(), transactionList);
            recentTransactionsRecyclerView.setAdapter(transactionAdapter);
        } else {
            transactionAdapter.updateData(transactionList);
        }
    }

    private void loadDashboardData() {
        double totalIncome = databaseHelper.getTotalByType(userId, "income");
        double totalExpense = databaseHelper.getTotalByType(userId, "expense");
        double balance = totalIncome - totalExpense;

        balanceTextView.setText("BALANCE \n₹" + String.format("%.2f", balance));

        double dailyTotal = databaseHelper.getExpenseForPeriod(userId, "daily");
        double weeklyTotal = databaseHelper.getExpenseForPeriod(userId, "weekly");
        double monthlyTotal = databaseHelper.getExpenseForPeriod(userId, "month");

        dailyTotalTextView.setText("Today: \n₹" + String.format("%.2f", dailyTotal));
        weeklyTotalTextView.setText("Week: \n₹" + String.format("%.2f", weeklyTotal));
        monthlyTotalTextView.setText("Month: \n₹" + String.format("%.2f", monthlyTotal));
    }
}
