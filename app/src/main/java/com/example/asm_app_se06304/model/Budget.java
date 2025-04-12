package com.example.asm_app_se06304.model;

public class Budget {
    private String description;
    private double amount;
    private String date; // budget_date
    private String budgetId;
    private int categoryId;
    private String categoryName;

    public Budget(String description, double amount, String date, String budgetId,
                  int categoryId, String categoryName) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public int getCategoryId() {
        return categoryId;
    }
    public String getCategoryName() {
        return categoryName;
    }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getBudgetId() { return budgetId; }
}