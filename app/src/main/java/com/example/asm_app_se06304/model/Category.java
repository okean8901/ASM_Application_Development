package com.example.asm_app_se06304.model;

public class Category {
    private String categoryName;
    private double totalAmount;
    private int color;

    public Category(String categoryName, double totalAmount, int color) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
        this.color = color;
    }


    public String getCategoryName() {
        return categoryName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public int getColor() {
        return color;
    }
}