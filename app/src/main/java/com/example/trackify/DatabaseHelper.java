package com.example.trackify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Trackify.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_TRANSACTIONS = "transactions";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, " +
                "password TEXT, " +
                "date_of_joining TEXT, " +
                "last_login TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "amount REAL, " +
                "category TEXT, " +
                "date TEXT, " +
                "type TEXT, " +
                "description TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        values.put("username", username);
        values.put("password", password);
        values.put("date_of_joining", currentDate);
        values.put("last_login", currentDate);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public int loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_USERS + " WHERE username=? AND password=?", new String[]{username, password});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
            ContentValues values = new ContentValues();
            values.put("last_login", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            db.update(TABLE_USERS, values, "id=?", new String[]{String.valueOf(userId)});
        }
        cursor.close();
        return userId;
    }

    public boolean insertTransaction(int userId, double amount, String category, String date, String type, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("amount", amount);
        values.put("category", category);
        values.put("date", date);
        values.put("type", type);
        values.put("description", description);
        long result = db.insert(TABLE_TRANSACTIONS, null, values);
        return result != -1;
    }

    public boolean updateTransaction(int id, double amount, String category, String date, String type, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("category", category);
        values.put("date", date);
        values.put("type", type);
        values.put("description", description);
        int rows = db.update(TABLE_TRANSACTIONS, values, "id=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_TRANSACTIONS, "id=?", new String[]{String.valueOf(transactionId)});
        return rows > 0;
    }

    public Transaction getTransactionById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM transactions WHERE id=?", new String[]{String.valueOf(id)});
        Transaction transaction = null;
        if (cursor.moveToFirst()) {
            transaction = new Transaction();
            transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            transaction.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
            transaction.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
            transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
            transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
            transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        }
        cursor.close();
        return transaction;
    }



    public List<Transaction> getRecentTransactions(int userId, int limit) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM transactions WHERE user_id=? ORDER BY date DESC LIMIT ?", new String[]{String.valueOf(userId), String.valueOf(limit)});
        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                transaction.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
                transaction.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                transactions.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactions;
    }

    public double getTotalByType(int userId, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE user_id=? AND type=?", new String[]{String.valueOf(userId), type});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public double getExpenseForPeriod(int userId, String period) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        String query;
        String[] args;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        switch (period.toLowerCase()) {
            case "day":
                query = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'expense' AND date = today";
                args = new String[]{String.valueOf(userId), today};
                break;
            case "week":
                query = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'expense' AND date >= DATE(?, '-6 days')";
                args = new String[]{String.valueOf(userId), today};
                break;
            case "month":
                query = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'expense' AND strftime('%Y-%m', date) = strftime('%Y-%m', ?)";
                args = new String[]{String.valueOf(userId), today};
                break;
            default:
                query = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'expense'";
                args = new String[]{String.valueOf(userId)};
                break;
        }

        Cursor cursor = db.rawQuery(query, args);
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public Map<String, Double> getTodayExpenseByCategory(int userId) {
        Map<String, Double> categoryTotals = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String query = "SELECT category, SUM(amount) FROM transactions WHERE user_id = ? AND type = 'expense' AND date = ? GROUP BY category";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), today});
        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                double total = cursor.getDouble(1);
                categoryTotals.put(category, total);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categoryTotals;
    }

    public List<CategoryData> getCategoryData(int userId, String type, String period) {
        List<CategoryData> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String dateCondition;
        switch (period.toLowerCase()) {
            case "daily":
                dateCondition = "date = date('now', 'localtime')";
                break;
            case "weekly":
                dateCondition = "date >= date('now', '-6 days', 'localtime')";
                break;
            case "monthly":
                dateCondition = "strftime('%Y-%m', date) = strftime('%Y-%m', 'now', 'localtime')";
                break;
            case "yearly":
                dateCondition = "strftime('%Y', date) = strftime('%Y', 'now', 'localtime')";
                break;
            default:
                dateCondition = "1 = 1"; // No filter
        }

        String query = "SELECT category, SUM(amount) as total FROM transactions " +
                "WHERE user_id = ? AND type = ? AND " + dateCondition + " GROUP BY category ORDER BY total DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), type});
        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                list.add(new CategoryData(category, total));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public String getDateOfJoining(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT date_of_joining FROM users WHERE id=?", new String[]{String.valueOf(userId)});
        String date = null;
        if (cursor.moveToFirst()) {
            date = cursor.getString(0);
        }
        cursor.close();
        return date;
    }

    public String getLastLogin(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT last_login FROM users WHERE id=?", new String[]{String.valueOf(userId)});
        String lastLogin = null;
        if (cursor.moveToFirst()) {
            lastLogin = cursor.getString(0);
        }
        cursor.close();
        return lastLogin;
    }
}



