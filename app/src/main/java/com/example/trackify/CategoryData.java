package com.example.trackify;


    public class CategoryData {
        private String category;
        private double totalAmount;

        public CategoryData(String category, double totalAmount) {
            this.category = category;
            this.totalAmount = totalAmount;
        }

        public String getCategory() {
            return category;
        }

        public double getTotalAmount() {
            return totalAmount;
        }
    }


