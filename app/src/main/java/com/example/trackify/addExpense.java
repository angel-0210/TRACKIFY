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

//package com.example.trackify;
//
//import android.app.DatePickerDialog;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.*;
//import androidx.appcompat.app.AppCompatActivity;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Locale;
//
//public class addExpense extends AppCompatActivity {
//
//    private EditText amountInput, descriptionInput;
//    private Spinner categorySpinner, typeSpinner;
//    private TextView dateTextView;
//    private Button saveBtn, deleteBtn;
//    private DatabaseHelper dbHelper;
//    private Calendar calendar;
//    private int userId, transactionId = -1;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.addexpense);
//
//        dbHelper = new DatabaseHelper(this);
//        userId = SessionManager.getUserId(this);
//
//        amountInput = findViewById(R.id.amountEditText);
//        descriptionInput = findViewById(R.id.descriptionEditText);
//        categorySpinner = findViewById(R.id.categorySpinner);
//        typeSpinner = findViewById(R.id.typeSpinner);
//        dateTextView = findViewById(R.id.dateEditText);
//        saveBtn = findViewById(R.id.saveButton);
//        deleteBtn = findViewById(R.id.deleteBtn);
//
//        calendar = Calendar.getInstance();
//        updateDateLabel();
//
//        dateTextView.setOnClickListener(v -> {
//            final Calendar calendar = Calendar.getInstance();
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH);
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//            DatePickerDialog datePickerDialog = new DatePickerDialog(
//                    addExpense.this,
//                    (view, selectedYear, selectedMonth, selectedDay) -> {
//                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
//                        dateTextView.setText(selectedDate);
//                    },
//                    year, month, day
//            );
//
//            // Restrict future dates
//            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
//            datePickerDialog.show();
//        });
////        // Show DatePicker on dateTextView click
////        dateTextView.setOnClickListener(v -> {
////            new DatePickerDialog(this, (view, year, month, day) -> {
////                calendar.set(year, month, day);
////                updateDateLabel();
////            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
////        });
////        DatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
//        // Populate type spinner
//        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_spinner_item, new String[]{"Income", "Expense"});
//        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        typeSpinner.setAdapter(typeAdapter);
//
//        // Load data if editing
//        if (getIntent().hasExtra("transaction")) {
//            Transaction transaction = (Transaction) getIntent().getSerializableExtra("transaction");
//            transactionId = transaction.getId();
//            loadTransactionData(transaction);
//        } else {
//            deleteBtn.setVisibility(View.GONE);
//        }
//
//        saveBtn.setOnClickListener(v -> saveTransaction());
//        deleteBtn.setOnClickListener(v -> {
//            if (transactionId != -1) {
//                dbHelper.deleteTransaction(transactionId);
//                finish();
//            }
//        });
//
//        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                populateCategorySpinner(typeSpinner.getSelectedItem().toString().toLowerCase());
//            }
//            @Override public void onNothingSelected(AdapterView<?> parent) {}
//        });
//    }
//
//    private void updateDateLabel() {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        dateTextView.setText(sdf.format(calendar.getTime()));
//    }
//
//    private void populateCategorySpinner(String type) {
//        int categoriesRes = type.equals("income") ? R.array.income_categories : R.array.expense_categories;
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, categoriesRes, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        categorySpinner.setAdapter(adapter);
//    }
//
//    private void loadTransactionData(Transaction transaction) {
//        amountInput.setText(String.valueOf(transaction.getAmount()));
//        descriptionInput.setText(transaction.getDescription());
//        dateTextView.setText(transaction.getDate());
//
//        // Set type and category
//        String type = transaction.getType();
//        typeSpinner.setSelection(type.equalsIgnoreCase("income") ? 0 : 1);
//        populateCategorySpinner(type.toLowerCase());
//
//        // Set category after spinner is populated
//        categorySpinner.post(() -> {
//            for (int i = 0; i < categorySpinner.getCount(); i++) {
//                if (categorySpinner.getItemAtPosition(i).toString().equals(transaction.getCategory())) {
//                    categorySpinner.setSelection(i);
//                    break;
//                }
//            }
//        });
//    }
//
//    private void saveTransaction() {
//        String type = typeSpinner.getSelectedItem().toString().toLowerCase();
//        String category = categorySpinner.getSelectedItem().toString();
//        String date = dateTextView.getText().toString();
//        String description = descriptionInput.getText().toString();
//        double amount;
//
//        try {
//            amount = Double.parseDouble(amountInput.getText().toString());
//        } catch (NumberFormatException e) {
//            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        boolean success;
//        if (transactionId != -1) {
//            success = dbHelper.updateTransaction(transactionId, amount, category, date, type, description);
//        } else {
//            success = dbHelper.insertTransaction(userId, amount, category, date, type, description);
//        }
//
//        if (success) {
//            Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show();
//            finish();
//        } else {
//            Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show();
//        }
//    }
//}

//package com.example.trackify;
//
//
//import android.app.Activity;
//import android.app.DatePickerDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.*;
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.Calendar;
//
//public class addExpense extends AppCompatActivity {
//
//    private EditText amountEditText, descriptionEditText, dateEditText;
//    private Spinner categorySpinner, typeSpinner;
//    private Button saveButton;
//    private DatabaseHelper dbHelper;
//    private int userId;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.addexpense);
//
//        dbHelper = new DatabaseHelper(this);
//        userId = SessionManager.getUserId(this);
//
//        if (userId == -1) {
//            Toast.makeText(this, "Session expired! Please login again.", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(this, loginactivity.class));
//            finish();
//            return;
//        }
//        int transactionId = getIntent().getIntExtra("transaction_id", -1);
//        if (transactionId != -1) {
//            Transaction existing = dbHelper.getTransactionById(transactionId);
//            // Pre-fill form fields with existing data
//            // Then update on Save
//        }
//
//        saveButton.setOnClickListener(v -> {
//            if (transactionId != -1) {
//                // Update existing transaction
//                dbHelper.updateTransaction(transactionId, updatedData);
//            } else {
//                // Add new
//                dbHelper.insertTransaction(newData);
//            }
//            finish();
//        });
//
//        amountEditText = findViewById(R.id.amountEditText);
//        descriptionEditText = findViewById(R.id.descriptionEditText);
//        categorySpinner = findViewById(R.id.categorySpinner);
//        typeSpinner = findViewById(R.id.typeSpinner);
//        dateEditText = findViewById(R.id.dateEditText);
//        saveButton = findViewById(R.id.saveButton);
//
//        setupSpinners();
//        setupDatePicker();
//
//        saveButton.setOnClickListener(v -> saveTransaction());
//    }
//
//    private void setupSpinners() {
//        // Spinner for Income / Expense
//        String[] types = {"Income", "Expense"};
//        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
//        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        typeSpinner.setAdapter(typeAdapter);
//
//        // Default categories for Expense
//        updateCategorySpinner("Expense");
//
//        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String selectedType = typeSpinner.getSelectedItem().toString();
//                updateCategorySpinner(selectedType);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) { }
//        });
//    }
//
//    private void updateCategorySpinner(String type) {
//        String[] expenseCategories = {"Food", "Travel", "Shopping", "Bills", "Entertainment", "Other"};
//        String[] incomeCategories = {"Salary", "Gift", "Bonus","Investment","Other"};
//
//        ArrayAdapter<String> categoryAdapter;
//        if (type.equals("Income")) {
//            categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, incomeCategories);
//        } else {
//            categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, expenseCategories);
//        }
//        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        categorySpinner.setAdapter(categoryAdapter);
//    }
//
//    private void setupDatePicker() {
//        dateEditText.setOnClickListener(v -> {
//            Calendar calendar = Calendar.getInstance();
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH);
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
//                    (view, selectedYear, selectedMonth, selectedDay) -> {
//                        String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
//                        dateEditText.setText(selectedDate);
//                    }, year, month, day);
//            // Prevent selecting future dates
//        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
//            datePickerDialog.show();
//        });
//    }
//
//
//    private void saveTransaction() {
//        String amountStr = amountEditText.getText().toString().trim();
//        String description = descriptionEditText.getText().toString().trim();
//        String category = categorySpinner.getSelectedItem().toString();
//        String type = typeSpinner.getSelectedItem().toString().toLowerCase();
//        String date = dateEditText.getText().toString().trim();
//
//        if (amountStr.isEmpty() || description.isEmpty() || date.isEmpty()) {
//            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        double amount = Double.parseDouble(amountStr);
//
//        boolean inserted = dbHelper.insertTransaction(userId, amount, description, category, type, date);
//
//        if (inserted) {
//            Toast.makeText(this, "Transaction Added", Toast.LENGTH_SHORT).show();
//            Intent resultIntent = new Intent();
//            setResult(Activity.RESULT_OK, resultIntent);
//            finish();
//        } else {
//            Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//}



//    private void showDatePicker() {
//        final Calendar calendar = Calendar.getInstance();
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//        DatePickerDialog picker = new DatePickerDialog(this,
//                (view, selectedYear, selectedMonth, selectedDay) -> {
//                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                            .format(new GregorianCalendar(selectedYear, selectedMonth, selectedDay).getTime());
//                    tvDate.setText(formattedDate);
//                }, year, month, day);
//        // Prevent selecting future dates
//        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
//        picker.show();
//    }
//}

//
//import android.app.DatePickerDialog;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.*;
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.Calendar;
//
//public class addExpense extends AppCompatActivity {
//
//    EditText editTitle, editAmount, editDate, editDescription;
//    Spinner spinnerType, spinnerCategory;
//    Button btnSave;
//    DatabaseHelper dbHelper;
//    int userId = 1; // Example, you can dynamically get from login
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.addexpense);
//
//        dbHelper = new DatabaseHelper(this);
//
//        editTitle = findViewById(R.id.etTitle);
//        editAmount = findViewById(R.id.etAmount);
//        editDate = findViewById(R.id.etDate);
//        editDescription = findViewById(R.id.etDescription);
//        spinnerType = findViewById(R.id.spinnerType);
//        spinnerCategory = findViewById(R.id.spinnerCategory);
//        btnSave = findViewById(R.id.btnSave);
//
//        // Setup spinners
//        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
//                R.array.expense_types, android.R.layout.simple_spinner_item);
//        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerType.setAdapter(typeAdapter);
//
//        // Set default category adapter
//        updateCategorySpinner("Expense");
//
//        // Date picker
//        editDate.setOnClickListener(v -> {
//            Calendar c = Calendar.getInstance();
//            new DatePickerDialog(this, (view, year, month, day) -> {
//                String dateStr = day + "/" + (month + 1) + "/" + year;
//                editDate.setText(dateStr);
//            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
//        });
//
//        // Change categories dynamically based on type
//        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                updateCategorySpinner(spinnerType.getSelectedItem().toString());
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {}
//        });
//
//        // Save expense
//        btnSave.setOnClickListener(v -> {
//            try {
//                String title = editTitle.getText().toString().trim();
//                String amountStr = editAmount.getText().toString().trim();
//                String date = editDate.getText().toString().trim();
//                String desc = editDescription.getText().toString().trim();
//                String category = spinnerCategory.getSelectedItem().toString();
//                String type = spinnerType.getSelectedItem().toString();
//
//                if (title.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
//                    Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                double amount = Double.parseDouble(amountStr);
//
//                boolean inserted = dbHelper.insertTransaction(userId, title, amount, date, desc, category, type);
//                if (inserted) {
//                    Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show();
//                    finish();
//                } else {
//                    Toast.makeText(this, "Error adding transaction", Toast.LENGTH_SHORT).show();
//                }
//            } catch (NumberFormatException e) {
//                Toast.makeText(this, "Invalid amount entered", Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void updateCategorySpinner(String type) {
//        int categoryArray = type.equals("Income") ? R.array.income_categories : R.array.expense_categories;
//        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
//                categoryArray, android.R.layout.simple_spinner_item);
//        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerCategory.setAdapter(categoryAdapter);
//    }
//}
