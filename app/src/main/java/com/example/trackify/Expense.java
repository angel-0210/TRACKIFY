package com.example.trackify;


    public class Expense {
        private int id;
        private String title;
        private String category;
        private double amount;
        private String type; // "Income" or "Expense"
        private String date;

        public Expense(int id, String title, String category, double amount, String type, String date) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.amount = amount;
            this.type = type;
            this.date = date;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getCategory() {
            return category;
        }

        public double getAmount() {
            return amount;
        }

        public String getType() {
            return type;
        }

        public String getDate() {
            return date;
        }
    }