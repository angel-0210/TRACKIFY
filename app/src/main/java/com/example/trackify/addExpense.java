package com.example.trackify;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class addExpense extends AppCompatActivity {

    private EditText amountInput, descriptionInput;
    private Spinner categorySpinner, typeSpinner;
    private TextView dateTextView;
    private Button saveBtn, deleteBtn;
    private DatabaseHelper dbHelper;
    private Calendar calendar;
    private int userId, transactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addexpense);

        dbHelper = new DatabaseHelper(this);
        userId = SessionManager.getUserId(this);

        amountInput = findViewById(R.id.amountEditText);
        descriptionInput = findViewById(R.id.descriptionEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        typeSpinner = findViewById(R.id.typeSpinner);
        dateTextView = findViewById(R.id.dateEditText);
        saveBtn = findViewById(R.id.saveButton);
        deleteBtn = findViewById(R.id.deleteBtn);

        calendar = Calendar.getInstance();
        updateDateLabel();

        dateTextView.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            int year = now.get(Calendar.YEAR);
            int month = now.get(Calendar.MONTH);
            int day = now.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    addExpense.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(selectedYear, selectedMonth, selectedDay);
                        updateDateLabel();
                    },
                    year, month, day
            );

            // Restrict future dates
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        // Populate type spinner
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Income", "Expense"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                populateCategorySpinner(typeSpinner.getSelectedItem().toString().toLowerCase());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Check if editing an existing transaction
        if (getIntent().hasExtra("transaction_id")) {
            transactionId = getIntent().getIntExtra("transaction_id", -1);
            if (transactionId != -1) {
                Transaction transaction = dbHelper.getTransactionById(transactionId);
                if (transaction != null) {
                    loadTransactionData(transaction);
                    deleteBtn.setVisibility(View.VISIBLE);
                }
            }
        } else {
            deleteBtn.setVisibility(View.GONE);
        }

        saveBtn.setOnClickListener(v -> saveTransaction());
        deleteBtn.setOnClickListener(v -> {
            if (transactionId != -1) {
                dbHelper.deleteTransaction(transactionId);
                Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        dateTextView.setText(sdf.format(calendar.getTime()));
    }

    private void populateCategorySpinner(String type) {
        int categoriesRes = type.equals("income") ? R.array.income_categories : R.array.expense_categories;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, categoriesRes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void loadTransactionData(Transaction transaction) {
        amountInput.setText(String.valueOf(transaction.getAmount()));
        descriptionInput.setText(transaction.getDescription());
        dateTextView.setText(transaction.getDate());

        // Set type and category
        String type = transaction.getType();
        typeSpinner.setSelection(type.equalsIgnoreCase("income") ? 0 : 1);
        populateCategorySpinner(type.toLowerCase());

        // Set category after spinner is populated
        categorySpinner.post(() -> {
            for (int i = 0; i < categorySpinner.getCount(); i++) {
                if (categorySpinner.getItemAtPosition(i).toString().equalsIgnoreCase(transaction.getCategory())) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
        });
    }

    private void saveTransaction() {
        String type = typeSpinner.getSelectedItem().toString().toLowerCase();
        String category = categorySpinner.getSelectedItem().toString();
        String date = dateTextView.getText().toString();
        String description = descriptionInput.getText().toString();
        double amount;

        try {
            amount = Double.parseDouble(amountInput.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success;
        if (transactionId != -1) {
            success = dbHelper.updateTransaction(transactionId, amount, category, date, type, description);
        } else {
            success = dbHelper.insertTransaction(userId, amount, category, date, type, description);
        }

        if (success) {
            Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);  // Notify parent activity/fragment
            finish();
        } else {
            Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show();
        }
    }
}

