package com.example.asm_app_se06304.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.asm_app_se06304.model.Expense;
import com.example.asm_app_se06304.model.ExpenseCategory;

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

        // Thêm dữ liệu mẫu cho Users
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

        // Thêm dữ liệu mẫu cho Categories (user_id = 1)
        db.execSQL("INSERT INTO " + CATEGORIES_TABLE + " (" + USER_ID_COL + ", " + NAME + ", " + DESCRIPTION + ") VALUES (1, 'Ăn uống', 'Chi phí ăn uống');");
        db.execSQL("INSERT INTO " + CATEGORIES_TABLE + " (" + USER_ID_COL + ", " + NAME + ", " + DESCRIPTION + ") VALUES (1, 'Di chuyển', 'Chi phí đi lại');");
        db.execSQL("INSERT INTO " + CATEGORIES_TABLE + " (" + USER_ID_COL + ", " + NAME + ", " + DESCRIPTION + ") VALUES (1, 'Giải trí', 'Chi phí giải trí');");
        db.execSQL("INSERT INTO " + CATEGORIES_TABLE + " (" + USER_ID_COL + ", " + NAME + ", " + DESCRIPTION + ") VALUES (1, 'Mua sắm', 'Chi phí mua sắm');");
        db.execSQL("INSERT INTO " + CATEGORIES_TABLE + " (" + USER_ID_COL + ", " + NAME + ", " + DESCRIPTION + ") VALUES (1, 'Nhà ở', 'Chi phí nhà ở');");
        db.execSQL("INSERT INTO " + CATEGORIES_TABLE + " (" + USER_ID_COL + ", " + NAME + ", " + DESCRIPTION + ") VALUES (1, 'Học phí', 'Chi phí học phí');");

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

        Log.d("DatabaseContext", "Database created with sample data");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EXPENSES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CATEGORIES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
        onCreate(db);
    }

    // Thêm chi phí mới
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

    // Cập nhật chi phí
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

    // Lấy danh sách chi phí của người dùng
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

    // Lấy danh sách chi phí theo tháng và năm
    public List<ExpenseCategory> getExpensesByMonth(int userId, int month, int year) {
        List<ExpenseCategory> expenses = new ArrayList<>();
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
                expenses.add(new ExpenseCategory(categoryName, totalAmount, color));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    private int getColorForCategory(String categoryName) {
        switch (categoryName) {
            case "Ăn uống": return android.graphics.Color.BLUE;
            case "Di chuyển": return android.graphics.Color.RED;
            case "Giải trí": return android.graphics.Color.YELLOW;
            case "Mua sắm": return android.graphics.Color.GREEN;
            case "Nhà ở": return android.graphics.Color.MAGENTA;
            case "Học phí": return android.graphics.Color.CYAN;
            default: return android.graphics.Color.GRAY;
        }
    }
}