package com.example.asm_app_se06304.model;

public class Budget {
    private String description;
    private double amount;
    private String date;


    public Budget(String description, double amount, String date, String category) {
        this.description = description;
        this.amount = amount;
        this.date = date;

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


}