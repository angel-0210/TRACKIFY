package com.example.trackify;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class analyticsFragment extends Fragment {

    private TextView totalIncomeText, totalExpenseText;
    private Spinner filterSpinner, timePeriodSpinner;
    private PieChart pieChart;
    private RecyclerView recyclerView;
    private DatabaseHelper databaseHelper;
    private analyticsAdapter analyticsAdapter;
    private int userId;

    public analyticsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        // Initialize UI
        totalIncomeText = view.findViewById(R.id.totalIncomeText);
        totalExpenseText = view.findViewById(R.id.totalExpenseText);
        filterSpinner = view.findViewById(R.id.filterSpinner);
        timePeriodSpinner = view.findViewById(R.id.timePeriodSpinner);
        pieChart = view.findViewById(R.id.pieChart);
        recyclerView = view.findViewById(R.id.recyclerViewTransactions);
        FloatingActionButton addExpenseFab = view.findViewById(R.id.addExpense);

        databaseHelper = new DatabaseHelper(requireContext());
        userId = SessionManager.getUserId(requireContext());

        setupSpinners();
        setupRecyclerView();

        loadTotals();        // Show current totals
        updateAnalytics();   // Initial chart + list population

        addExpenseFab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), addExpense.class);
            startActivity(intent);
        });

        return view;
    }

    private void setupSpinners() {
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Income", "Expense"});
        filterSpinner.setAdapter(typeAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Daily", "Weekly", "Monthly", "Yearly"});
        timePeriodSpinner.setAdapter(timeAdapter);

        AdapterView.OnItemSelectedListener selectionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAnalytics();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

        filterSpinner.setOnItemSelectedListener(selectionListener);
        timePeriodSpinner.setOnItemSelectedListener(selectionListener);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        analyticsAdapter = new analyticsAdapter(new ArrayList<>());
        recyclerView.setAdapter(analyticsAdapter);
    }

    private void updateAnalytics() {
        if (filterSpinner.getSelectedItem() == null || timePeriodSpinner.getSelectedItem() == null) return;

        String selectedType = filterSpinner.getSelectedItem().toString().toLowerCase(); // income or expense
        String selectedPeriod = timePeriodSpinner.getSelectedItem().toString().toLowerCase(); // daily, weekly, monthly, yearly

        List<CategoryData> categoryDataList = databaseHelper.getCategoryData(userId, selectedType, selectedPeriod);

        updatePieChart(categoryDataList);
        analyticsAdapter.updateData(categoryDataList);
    }

    private void updatePieChart(List<CategoryData> dataList) {
        List<PieEntry> entries = new ArrayList<>();

        for (CategoryData data : dataList) {
            entries.add(new PieEntry((float) data.getTotalAmount(), data.getCategory()));
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No data available for selected filters.");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Categories");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    private void loadTotals() {
        double totalIncome = databaseHelper.getTotalByType(userId, "income");
        double totalExpense = databaseHelper.getTotalByType(userId, "expense");

        totalIncomeText.setText("₹ " + String.format("%.2f", totalIncome));
        totalExpenseText.setText("₹ " + String.format("%.2f", totalExpense));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTotals();
        updateAnalytics(); // Refresh on return to fragment
    }
}
