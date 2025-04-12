package com.example.asm_app_se06304.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.asm_app_se06304.model.Budget;
import com.example.asm_app_se06304.model.Expense;
import com.example.asm_app_se06304.model.Category;

import java.util.ArrayList;
import java.util.List;

public class DatabaseContext extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FinanceManagement.db";
    private static final int DB_VERSION = 2;

    // Users table
    public static final String USERS_TABLE = "Users";
    public static final String ID_COL = "user_id";
    public static final String USERNAME_COL = "username";
    public static final String PASSWORD_COL = "password_hash";
    public static final String EMAIL_COL = "email";
    public static final String PHONE_COL = "phone_number";
    public static final String CREATED_AT_COL = "created_at";
    public static final String LAST_LOGIN_COL = "last_login";

    // Categories table
    public static final String CATEGORIES_TABLE = "Categories";
    public static final String CATEGORY_ID_COL = "category_id";
    public static final String USER_ID_COL = "user_id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";

    // Expenses table
    public static final String EXPENSES_TABLE = "Expenses";
    public static final String EXPENSE_ID_COL = "expense_id";
    public static final String EXPENSE_DESCRIPTION_COL = "description";
    public static final String EXPENSE_AMOUNT_COL = "amount";
    public static final String EXPENSE_DATE_COL = "expense_date";

    // Table for budgets
    public static final String BUDGETS_TABLE = "Budgets";
    public static final String BUDGET_ID_COL = "budget_id";
    public static final String BUDGET_DESCRIPTION_COL = "description";
    public static final String BUDGET_AMOUNT_COL = "amount";
    public static final String BUDGET_CATEGORY_ID_COL = "category_id";
    public static final String BUDGET_USER_ID_COL = "user_id";
    public static final String BUDGET_DATETIME_COL = "budget_date";

    public DatabaseContext(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String createUsersTable = "CREATE TABLE " + USERS_TABLE + " (" +
                ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USERNAME_COL + " VARCHAR(50) UNIQUE NOT NULL, " +
                PASSWORD_COL + " VARCHAR(255) NOT NULL, " +
                EMAIL_COL + " VARCHAR(100) UNIQUE NOT NULL, " +
                PHONE_COL + " VARCHAR(10), " +
                CREATED_AT_COL + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                LAST_LOGIN_COL + " TIMESTAMP);";
        db.execSQL(createUsersTable);

        // Add sample data for Users
        db.execSQL("INSERT INTO " + USERS_TABLE + " (" + USERNAME_COL + ", " + PASSWORD_COL + ", " + EMAIL_COL + ") " +
                "VALUES ('user1', 'password123', 'user1@example.com');");

        // Create Categories table
        String createCategoriesTable = "CREATE TABLE " + CATEGORIES_TABLE + " (" +
                CATEGORY_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_ID_COL + " INTEGER, " +
                NAME + " VARCHAR(50) NOT NULL, " +
                DESCRIPTION + " TEXT, " +
                "FOREIGN KEY (" + USER_ID_COL + ") REFERENCES " + USERS_TABLE + "(" + ID_COL + "), " +
                "UNIQUE(" + USER_ID_COL + ", " + NAME + "));";
        db.execSQL(createCategoriesTable);

        // Add sample data for Categories (user_id = 1)
        db.execSQL("INSERT INTO " + CATEGORIES_TABLE + " (" + USER_ID_COL + ", " + NAME + ", " + DESCRIPTION + ") VALUES (1, '-- None --', '');");


        // Create Expenses table
        String createExpensesTable = "CREATE TABLE " + EXPENSES_TABLE + " (" +
                EXPENSE_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_ID_COL + " INTEGER NOT NULL, " +
                CATEGORY_ID_COL + " INTEGER NOT NULL, " +
                EXPENSE_DESCRIPTION_COL + " TEXT, " +
                EXPENSE_AMOUNT_COL + " DECIMAL(10, 2) NOT NULL, " +
                EXPENSE_DATE_COL + " DATE NOT NULL, " +
                "FOREIGN KEY (" + USER_ID_COL + ") REFERENCES " + USERS_TABLE + "(" + ID_COL + "), " +
                "FOREIGN KEY (" + CATEGORY_ID_COL + ") REFERENCES " + CATEGORIES_TABLE + "(" + CATEGORY_ID_COL + "));";
        db.execSQL(createExpensesTable);

        // Create Budgets table
        String createBudgetsTable = "CREATE TABLE " + BUDGETS_TABLE + " (" +
                BUDGET_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BUDGET_USER_ID_COL + " INTEGER NOT NULL, " +
                BUDGET_CATEGORY_ID_COL + " INTEGER NOT NULL, " +
                BUDGET_DESCRIPTION_COL + " TEXT, " +
                BUDGET_AMOUNT_COL + " DECIMAL(10, 2) NOT NULL, " +
                BUDGET_DATETIME_COL + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + BUDGET_USER_ID_COL + ") REFERENCES " + USERS_TABLE + "(" + ID_COL + "), " +
                "FOREIGN KEY (" + BUDGET_CATEGORY_ID_COL + ") REFERENCES " + CATEGORIES_TABLE + "(" + CATEGORY_ID_COL + "));";
        db.execSQL(createBudgetsTable);
        Log.d("DatabaseContext", "Database created with sample data");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EXPENSES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CATEGORIES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + BUDGETS_TABLE);

        onCreate(db);
    }

    // Add new expense
    public long addExpense(int userId, int categoryId, String description, double amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor userCursor = db.rawQuery("SELECT " + ID_COL + " FROM " + USERS_TABLE +
                " WHERE " + ID_COL + " = ?", new String[]{String.valueOf(userId)});
        if (!userCursor.moveToFirst()) {
            userCursor.close();
            db.close();
            Log.e("DatabaseContext", "Invalid userId: " + userId);
            return -1;
        }
        userCursor.close();

        Cursor categoryCursor = db.rawQuery("SELECT " + CATEGORY_ID_COL + " FROM " + CATEGORIES_TABLE +
                " WHERE " + CATEGORY_ID_COL + " = ?", new String[]{String.valueOf(categoryId)});
        if (!categoryCursor.moveToFirst()) {
            categoryCursor.close();
            db.close();
            Log.e("DatabaseContext", "Invalid categoryId: " + categoryId);
            return -1;
        }
        categoryCursor.close();

        ContentValues values = new ContentValues();
        values.put(USER_ID_COL, userId);
        values.put(CATEGORY_ID_COL, categoryId);
        values.put(EXPENSE_DESCRIPTION_COL, description);
        values.put(EXPENSE_AMOUNT_COL, amount);
        values.put(EXPENSE_DATE_COL, date);

        long id = db.insert(EXPENSES_TABLE, null, values);
        if (id == -1) {
            Log.e("DatabaseContext", "Failed to insert expense");
        }
        db.close();
        return id;
    }

    // Update expense
    public long updateExpense(long expenseId, int userId, int categoryId, String description, double amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_ID_COL, userId);
        values.put(CATEGORY_ID_COL, categoryId);
        values.put(EXPENSE_DESCRIPTION_COL, description);
        values.put(EXPENSE_AMOUNT_COL, amount);
        values.put(EXPENSE_DATE_COL, date);

        long result = db.update(EXPENSES_TABLE, values, EXPENSE_ID_COL + " = ?", new String[]{String.valueOf(expenseId)});
        db.close();
        return result;
    }

    // Get all expenses for user
    public List<Expense> getAllExpenses(int userId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT e." + EXPENSE_ID_COL + ", e." + EXPENSE_DESCRIPTION_COL + ", e." + EXPENSE_AMOUNT_COL +
                ", e." + EXPENSE_DATE_COL + ", c." + NAME +
                ", e." + CATEGORY_ID_COL +
                " FROM " + EXPENSES_TABLE + " e" +
                " JOIN " + CATEGORIES_TABLE + " c ON e." + CATEGORY_ID_COL + " = c." + CATEGORY_ID_COL +
                " WHERE e." + USER_ID_COL + " = ?" +
                " ORDER BY e." + EXPENSE_DATE_COL + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String description = cursor.getString(1);
                double amount = cursor.getDouble(2);
                String date = cursor.getString(3);
                String categoryName = cursor.getString(4);
                int categoryId = cursor.getInt(5);
                expenses.add(new Expense(id, description, amount, date, categoryName, categoryId));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }


    public List<String> getAllCategories(int userId) {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                CATEGORIES_TABLE,
                new String[]{NAME},
                USER_ID_COL + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return categories;
    }

    public double getTotalBudgetByMonth(int userId, int month, int year) {
        double totalBudget = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to sum budgets for the selected month and year
        String query = "SELECT SUM(" + BUDGET_AMOUNT_COL + ") FROM " + BUDGETS_TABLE +
                " WHERE " + BUDGET_USER_ID_COL + " = ?" +
                " AND strftime('%m', " + BUDGET_DATETIME_COL + ") = ?" +
                " AND strftime('%Y', " + BUDGET_DATETIME_COL + ") = ?";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(userId),
                String.format("%02d", month),
                String.valueOf(year)
        });

        if (cursor.moveToFirst()) {
            totalBudget = cursor.getDouble(0); // Get the sum
        }
        cursor.close();
        db.close();
        return totalBudget;
    }

    public double getTotalBudget(int userId) {
        double totalBudget = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + BUDGET_AMOUNT_COL + ") FROM " + BUDGETS_TABLE + " WHERE " + BUDGET_USER_ID_COL + " = ?",
                new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            totalBudget = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return totalBudget;
    }


    // Lấy danh sách chi phí theo tháng và năm
    public List<Category> getExpensesByMonth(int userId, int month, int year) {
        List<Category> expenses = new ArrayList<>();


        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT c." + NAME + ", SUM(e." + EXPENSE_AMOUNT_COL + ")" +
                " FROM " + EXPENSES_TABLE + " e" +
                " JOIN " + CATEGORIES_TABLE + " c ON e." + CATEGORY_ID_COL + " = c." + CATEGORY_ID_COL +
                " WHERE e." + USER_ID_COL + " = ?" +
                " AND strftime('%m', e." + EXPENSE_DATE_COL + ") = ?" +
                " AND strftime('%Y', e." + EXPENSE_DATE_COL + ") = ?" +
                " GROUP BY c." + CATEGORY_ID_COL + ", c." + NAME;

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(userId),
                String.format("%02d", month),
                String.valueOf(year)
        });

        if (cursor.moveToFirst()) {
            do {
                String categoryName = cursor.getString(0);
                double totalAmount = cursor.getDouble(1);
                int color = getColorForCategory(categoryName);
                expenses.add(new Category(categoryName, totalAmount, color));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    public long addBudget(int userId, int categoryId, String description, double amount, String datetime) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Validate userId
        Cursor userCursor = db.rawQuery("SELECT " + ID_COL + " FROM " + USERS_TABLE +
                " WHERE " + ID_COL + " = ?", new String[]{String.valueOf(userId)});
        if (!userCursor.moveToFirst()) {
            userCursor.close();
            db.close();
            Log.e("DatabaseContext", "Invalid userId: " + userId);
            return -1;
        }
        userCursor.close();

        // Validate categoryId
        Cursor categoryCursor = db.rawQuery("SELECT " + CATEGORY_ID_COL + " FROM " + CATEGORIES_TABLE +
                " WHERE " + CATEGORY_ID_COL + " = ?", new String[]{String.valueOf(categoryId)});
        if (!categoryCursor.moveToFirst()) {
            categoryCursor.close();
            db.close();
            Log.e("DatabaseContext", "Invalid categoryId: " + categoryId);
            return -1;
        }
        categoryCursor.close();

        // Insert budget into Budgets table
        ContentValues values = new ContentValues();
        values.put(BUDGET_USER_ID_COL, userId);
        values.put(BUDGET_CATEGORY_ID_COL, categoryId);
        values.put(BUDGET_DESCRIPTION_COL, description);
        values.put(BUDGET_AMOUNT_COL, amount);
        values.put(BUDGET_DATETIME_COL, datetime);

        long id = db.insert(BUDGETS_TABLE, null, values);
        if (id == -1) {
            Log.e("DatabaseContext", "Failed to insert budget");
        }
        db.close();
        return id;
    }

    public long updateBudget(long budgetId, int userId, int categoryId, String description, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Validate userId
        Cursor userCursor = db.rawQuery("SELECT " + ID_COL + " FROM " + USERS_TABLE +
                " WHERE " + ID_COL + " = ?", new String[]{String.valueOf(userId)});
        if (!userCursor.moveToFirst()) {
            userCursor.close();
            db.close();
            Log.e("DatabaseContext", "Invalid userId: " + userId);
            return -1;
        }
        userCursor.close();

        // Validate categoryId
        Cursor categoryCursor = db.rawQuery("SELECT " + CATEGORY_ID_COL + " FROM " + CATEGORIES_TABLE +
                " WHERE " + CATEGORY_ID_COL + " = ?", new String[]{String.valueOf(categoryId)});
        if (!categoryCursor.moveToFirst()) {
            categoryCursor.close();
            db.close();
            Log.e("DatabaseContext", "Invalid categoryId: " + categoryId);
            return -1;
        }
        categoryCursor.close();

        // Prepare values for update
        ContentValues values = new ContentValues();
        values.put(BUDGET_USER_ID_COL, userId);
        values.put(BUDGET_CATEGORY_ID_COL, categoryId);
        values.put(BUDGET_DESCRIPTION_COL, description);
        values.put(BUDGET_AMOUNT_COL, amount);

        // Update the budget
        long result = db.update(BUDGETS_TABLE, values, BUDGET_ID_COL + " = ?", new String[]{String.valueOf(budgetId)});
        db.close();
        return result;
    }



    public List<Budget> getAllBudgets(int userId) {
        List<Budget> budgets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT b." + BUDGET_ID_COL + ", b." + BUDGET_DESCRIPTION_COL + ", " +
                "b." + BUDGET_AMOUNT_COL + ", b." + BUDGET_DATETIME_COL + ", " +
                "b." + BUDGET_CATEGORY_ID_COL + ", c." + NAME +
                " FROM " + BUDGETS_TABLE + " b" +
                " JOIN " + CATEGORIES_TABLE + " c ON b." + BUDGET_CATEGORY_ID_COL + " = c." + CATEGORY_ID_COL +
                " WHERE b." + BUDGET_USER_ID_COL + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String description = cursor.getString(1);
                double amount = cursor.getDouble(2);
                String date = cursor.getString(3);
                int categoryId = cursor.getInt(4);
                String fullCategoryName = cursor.getString(5);

                // Remove prefix from category name
                String displayCategoryName = fullCategoryName;
                if (fullCategoryName.startsWith("Income_") || fullCategoryName.startsWith("Expense_")) {
                    displayCategoryName = fullCategoryName.substring(fullCategoryName.indexOf('_') + 1);
                }

                String budgetId = String.valueOf(id);
                budgets.add(new Budget(description, amount, date, budgetId, categoryId, displayCategoryName));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return budgets;
    }

    // Update a category
    public long updateCategory(int categoryId, String newName, String newDescription) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME, newName);
        values.put(DESCRIPTION, newDescription);

        long result = db.update(
                CATEGORIES_TABLE,
                values,
                CATEGORY_ID_COL + " = ?",
                new String[]{String.valueOf(categoryId)}
        );
        db.close();
        return result; // Returns number of rows affected
    }

    public String getCategoryDescription(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                CATEGORIES_TABLE,
                new String[]{DESCRIPTION},
                CATEGORY_ID_COL + " = ?",
                new String[]{String.valueOf(categoryId)},
                null, null, null
        );

        String description = null;
        if (cursor.moveToFirst()) {
            description = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return description;
    }

    // Delete a category
    public long deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(
                CATEGORIES_TABLE,
                CATEGORY_ID_COL + " = ?",
                new String[]{String.valueOf(categoryId)}
        );
        db.close();
        return result; // Returns number of rows deleted
    }

    public long deleteExpense(long expenseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(EXPENSES_TABLE, EXPENSE_ID_COL + " = ?", new String[]{String.valueOf(expenseId)});
        db.close();
        return result;
    }

    public long deleteBudget(long budgetId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(BUDGETS_TABLE, BUDGET_ID_COL + " = ?", new String[]{String.valueOf(budgetId)});
        db.close();
        return result;
    }

    private int getColorForCategory(String categoryName) {
        switch (categoryName) {
            case "Food": return android.graphics.Color.BLUE;
            case "Transportation": return android.graphics.Color.RED;
            case "Entertainment": return android.graphics.Color.YELLOW;
            case "Shopping": return android.graphics.Color.GREEN;
            case "Housing": return android.graphics.Color.MAGENTA;
            case "Tuition": return android.graphics.Color.CYAN;
            default: return android.graphics.Color.GRAY;
        }
    }


    public long addCategory(int userId, String name, String description) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_ID_COL, userId);
        values.put(NAME, name);
        values.put(DESCRIPTION, description);

        long id = db.insert(CATEGORIES_TABLE, null, values);
        db.close();

        return id;
    }
    public List<String> getCategoriesByType(int userId, boolean isIncome) {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String prefix = isIncome ? "Income_" : "Expense_";
        String query = "SELECT " + NAME + " FROM " + CATEGORIES_TABLE +
                " WHERE " + USER_ID_COL + " = ? AND " + NAME + " LIKE ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), prefix + "%"});

        if (cursor.moveToFirst()) {
            do {
                // Bỏ tiền tố khi hiển thị
                String fullName = cursor.getString(0);
                String displayName = fullName.substring(fullName.indexOf('_') + 1);
                categories.add(displayName.replace("_", " ")); // Thay dấu _ bằng khoảng trắng
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return categories;
    }
    public boolean isIncomeCategory(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + NAME + " FROM " + CATEGORIES_TABLE +
                        " WHERE " + CATEGORY_ID_COL + " = ?",
                new String[]{String.valueOf(categoryId)});

        boolean isIncome = false;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            isIncome = name.startsWith("Income_");
        }

        cursor.close();
        db.close();
        return isIncome;
    }

    public List<String> getDetailedBudgetChanges(String startDate, String endDate) {
        List<String> changes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Query all budget changes
        String budgetQuery = "SELECT b." + BUDGET_DESCRIPTION_COL + ", b." + BUDGET_AMOUNT_COL +
                ", b." + BUDGET_DATETIME_COL + ", c." + NAME +
                " FROM " + BUDGETS_TABLE + " b" +
                " JOIN " + CATEGORIES_TABLE + " c ON b." + BUDGET_CATEGORY_ID_COL + " = c." + CATEGORY_ID_COL +
                " WHERE date(b." + BUDGET_DATETIME_COL + ") BETWEEN date(?) AND date(?)" +
                " ORDER BY b." + BUDGET_DATETIME_COL;

        // Query all expenses affecting budget
        String expenseQuery = "SELECT e." + EXPENSE_DESCRIPTION_COL + ", e." + EXPENSE_AMOUNT_COL +
                ", e." + EXPENSE_DATE_COL + ", c." + NAME +
                " FROM " + EXPENSES_TABLE + " e" +
                " JOIN " + CATEGORIES_TABLE + " c ON e." + CATEGORY_ID_COL + " = c." + CATEGORY_ID_COL +
                " WHERE date(e." + EXPENSE_DATE_COL + ") BETWEEN date(?) AND date(?)" +
                " ORDER BY e." + EXPENSE_DATE_COL;

        // Process budget changes
        Cursor budgetCursor = db.rawQuery(budgetQuery, new String[]{startDate, endDate});
        while (budgetCursor.moveToNext()) {
            String date = budgetCursor.getString(2).substring(0, 10);
            String category = budgetCursor.getString(3);
            String description = budgetCursor.getString(0);
            double amount = budgetCursor.getDouble(1);
            changes.add(date + " | BUDGET SET | " + category + " | " +
                    description + " | " + String.format("%,.0f", amount) + " VND");
        }
        budgetCursor.close();

        // Process expense changes
        Cursor expenseCursor = db.rawQuery(expenseQuery, new String[]{startDate, endDate});
        while (expenseCursor.moveToNext()) {
            String date = expenseCursor.getString(2).substring(0, 10);
            String category = expenseCursor.getString(3);
            String description = expenseCursor.getString(0);
            double amount = expenseCursor.getDouble(1);
            changes.add(date + " | EXPENSE   | " + category + " | " +
                    description + " | -" + String.format("%,.0f", amount) + " VND");
        }
        expenseCursor.close();

        db.close();
        return changes;
    }

    public List<Expense> getExpensesByCategory(int userId, int categoryId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT e." + EXPENSE_ID_COL + ", e." + EXPENSE_DESCRIPTION_COL +
                ", e." + EXPENSE_AMOUNT_COL + ", e." + EXPENSE_DATE_COL +
                ", c." + NAME + ", e." + CATEGORY_ID_COL +
                " FROM " + EXPENSES_TABLE + " e" +
                " JOIN " + CATEGORIES_TABLE + " c ON e." + CATEGORY_ID_COL + " = c." + CATEGORY_ID_COL +
                " WHERE e." + USER_ID_COL + " = ? AND e." + CATEGORY_ID_COL + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(categoryId)});

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String description = cursor.getString(1);
                double amount = cursor.getDouble(2);
                String date = cursor.getString(3);
                String fullCategoryName = cursor.getString(4);
                int catId = cursor.getInt(5);

                // Remove prefix from category name
                String displayCategoryName = fullCategoryName;
                if (fullCategoryName.startsWith("Income_") || fullCategoryName.startsWith("Expense_")) {
                    displayCategoryName = fullCategoryName.substring(fullCategoryName.indexOf('_') + 1);
                }

                expenses.add(new Expense(id, description, amount, date, displayCategoryName, catId));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    public int deleteExpensesByCategory(int userId, int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.delete(
                EXPENSES_TABLE,
                USER_ID_COL + " = ? AND " + CATEGORY_ID_COL + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(categoryId)}
        );
        db.close();
        return count;
    }

    public int getBudgetCategoryId(long budgetId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int categoryId = -1;

        Cursor cursor = db.query(
                BUDGETS_TABLE,
                new String[]{BUDGET_CATEGORY_ID_COL},
                BUDGET_ID_COL + " = ?",
                new String[]{String.valueOf(budgetId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            categoryId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return categoryId;
    }


    // Extract the suffix after the first underscore
    private String getCategorySuffix(String fullName) {
        if (fullName == null) return "";
        int underscoreIndex = fullName.indexOf('_');
        return underscoreIndex >= 0 ? fullName.substring(underscoreIndex + 1) : fullName;
    }

    // Check if any expenses share the same suffix as this budget's category
    public boolean hasExpensesWithSameSuffix(int userId, long budgetId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // First get the budget's category suffix
        String budgetSuffixQuery = "SELECT c." + NAME + " FROM " + BUDGETS_TABLE + " b " +
                "JOIN " + CATEGORIES_TABLE + " c ON b." + BUDGET_CATEGORY_ID_COL + " = c." + CATEGORY_ID_COL + " " +
                "WHERE b." + BUDGET_ID_COL + " = ?";

        Cursor budgetCursor = db.rawQuery(budgetSuffixQuery, new String[]{String.valueOf(budgetId)});
        String budgetSuffix = "";

        if (budgetCursor.moveToFirst()) {
            budgetSuffix = getCategorySuffix(budgetCursor.getString(0));
        }
        budgetCursor.close();

        if (budgetSuffix.isEmpty()) return false;

        // Now check for expenses with matching suffixes
        String expenseQuery = "SELECT COUNT(*) FROM " + EXPENSES_TABLE + " e " +
                "JOIN " + CATEGORIES_TABLE + " c ON e." + CATEGORY_ID_COL + " = c." + CATEGORY_ID_COL + " " +
                "WHERE e." + USER_ID_COL + " = ? AND " +
                "(c." + NAME + " LIKE '%_" + budgetSuffix + "' OR " +
                "c." + NAME + " LIKE '" + budgetSuffix + "')";

        Cursor expenseCursor = db.rawQuery(expenseQuery, new String[]{String.valueOf(userId)});
        boolean hasMatches = false;

        if (expenseCursor.moveToFirst()) {
            hasMatches = expenseCursor.getInt(0) > 0;
        }
        expenseCursor.close();
        db.close();

        return hasMatches;
    }


    public String getCategoryDisplayName(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String displayName = "";

        Cursor cursor = db.query(
                CATEGORIES_TABLE,
                new String[]{NAME},
                CATEGORY_ID_COL + " = ?",
                new String[]{String.valueOf(categoryId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            String fullName = cursor.getString(0);
            // Remove everything before and including the first underscore
            int underscoreIndex = fullName.indexOf('_');
            if (underscoreIndex >= 0 && underscoreIndex < fullName.length() - 1) {
                displayName = fullName.substring(underscoreIndex + 1);
            } else {
                displayName = fullName; // No underscore or nothing after it
            }
        }
        cursor.close();
        db.close();
        return displayName;
    }

}