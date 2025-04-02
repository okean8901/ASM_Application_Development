package com.example.asm_app_se06304.model;

public class Budget {
    private String description;
    private double amount;
    private String date;
    private String category;

    public Budget(String description, double amount, String date, String category) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }
}