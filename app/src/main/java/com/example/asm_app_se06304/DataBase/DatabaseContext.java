package com.example.asm_app_se06304.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

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

    // Budgets table
    public static final String BUDGETS_TABLE = "Budgets";
    public static final String BUDGET_ID_COL = "budget_id";
    public static final String AMOUNT_COL = "amount";
    public static final String MONTH_COL = "month";
    public static final String YEAR_COL = "year";

    // Expenses table
    public static final String EXPENSES_TABLE = "Expenses";
    public static final String EXPENSE_ID_COL = "expense_id";
    public static final String EXPENSE_DESCRIPTION_COL = "description";
    public static final String EXPENSE_AMOUNT_COL = "amount";
    public static final String EXPENSE_DATE_COL = "expense_date";

    // RecurringExpenses table
    public static final String RECURRING_EXPENSES_TABLE = "RecurringExpenses";
    public static final String RECURRING_ID_COL = "recurring_id";
    public static final String FREQUENCY_COL = "frequency";
    public static final String START_DATE_COL = "start_date";
    public static final String END_DATE_COL = "end_date";
    public static final String LAST_GENERATED_COL = "last_generated";

    // Notifications table
    public static final String NOTIFICATIONS_TABLE = "Notifications";
    public static final String NOTIFICATION_ID_COL = "notification_id";
    public static final String MESSAGE_COL = "message";
    public static final String IS_READ_COL = "is_read";

    // Reports table
    public static final String REPORTS_TABLE = "Reports";
    public static final String REPORT_ID_COL = "report_id";
    public static final String REPORT_NAME_COL = "report_name";
    public static final String REPORT_DATA_COL = "report_data";

    public DatabaseContext(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String createUsersTable = "CREATE TABLE " + USERS_TABLE + " (" +
                ID_COL + " INTEGER PRIMARY KEY, " +
                USERNAME_COL + " VARCHAR(50) UNIQUE NOT NULL, " +
                PASSWORD_COL + " VARCHAR(255) NOT NULL, " +
                EMAIL_COL + " VARCHAR(100) UNIQUE NOT NULL, " +
                PHONE_COL + " VARCHAR(10), " +
                CREATED_AT_COL + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                LAST_LOGIN_COL + " TIMESTAMP);";

        db.execSQL(createUsersTable);

        // Create Categories table
        String createCategoriesTable = "CREATE TABLE " + CATEGORIES_TABLE + " (" +
                CATEGORY_ID_COL + " INTEGER PRIMARY KEY, " +
                USER_ID_COL + " INTEGER, " +
                NAME + " VARCHAR(50) NOT NULL, " +
                DESCRIPTION + " TEXT, " +
                "FOREIGN KEY (" + USER_ID_COL + ") REFERENCES " + USERS_TABLE + "(" + ID_COL + "), " +
                "UNIQUE(" + USER_ID_COL + ", " + NAME + "));";

        db.execSQL(createCategoriesTable);

        // Create Budgets table
        String createBudgetsTable = "CREATE TABLE " + BUDGETS_TABLE + " (" +
                BUDGET_ID_COL + " INTEGER PRIMARY KEY, " +
                USER_ID_COL + " INTEGER NOT NULL, " +
                CATEGORY_ID_COL + " INTEGER NOT NULL, " +
                AMOUNT_COL + " DECIMAL(10, 2) NOT NULL, " +
                MONTH_COL + " INTEGER NOT NULL, " +
                YEAR_COL + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + USER_ID_COL + ") REFERENCES " + USERS_TABLE + "(" + ID_COL + "), " +
                "FOREIGN KEY (" + CATEGORY_ID_COL + ") REFERENCES " + CATEGORIES_TABLE + "(" + CATEGORY_ID_COL + "), " +
                "UNIQUE(" + USER_ID_COL + ", " + CATEGORY_ID_COL + ", " + MONTH_COL + ", " + YEAR_COL + "));";

        db.execSQL(createBudgetsTable);

        // Create Expenses table
        String createExpensesTable = "CREATE TABLE " + EXPENSES_TABLE + " (" +
                EXPENSE_ID_COL + " INTEGER PRIMARY KEY, " +
                USER_ID_COL + " INTEGER NOT NULL, " +
                CATEGORY_ID_COL + " INTEGER NOT NULL, " +
                EXPENSE_DESCRIPTION_COL + " TEXT, " +
                EXPENSE_AMOUNT_COL + " DECIMAL(10, 2) NOT NULL, " +
                EXPENSE_DATE_COL + " DATE NOT NULL, " +
                "FOREIGN KEY (" + USER_ID_COL + ") REFERENCES " + USERS_TABLE + "(" + ID_COL + "), " +
                "FOREIGN KEY (" + CATEGORY_ID_COL + ") REFERENCES " + CATEGORIES_TABLE + "(" + CATEGORY_ID_COL + "));";

        db.execSQL(createExpensesTable);

        // Create RecurringExpenses table
        String createRecurringExpensesTable = "CREATE TABLE " + RECURRING_EXPENSES_TABLE + " (" +
                RECURRING_ID_COL + " INTEGER PRIMARY KEY, " +
                USER_ID_COL + " INTEGER NOT NULL, " +
                CATEGORY_ID_COL + " INTEGER NOT NULL, " +
                EXPENSE_DESCRIPTION_COL + " TEXT, " +
                EXPENSE_AMOUNT_COL + " DECIMAL(10, 2) NOT NULL, " +
                FREQUENCY_COL + " VARCHAR(20) NOT NULL, " +
                START_DATE_COL + " DATE NOT NULL, " +
                END_DATE_COL + " DATE, " +
                LAST_GENERATED_COL + " DATE, " +
                "FOREIGN KEY (" + USER_ID_COL + ") REFERENCES " + USERS_TABLE + "(" + ID_COL + "), " +
                "FOREIGN KEY (" + CATEGORY_ID_COL + ") REFERENCES " + CATEGORIES_TABLE + "(" + CATEGORY_ID_COL + "));";

        db.execSQL(createRecurringExpensesTable);

        // Create Notifications table
        String createNotificationsTable = "CREATE TABLE " + NOTIFICATIONS_TABLE + " (" +
                NOTIFICATION_ID_COL + " INTEGER PRIMARY KEY, " +
                USER_ID_COL + " INTEGER NOT NULL, " +
                CATEGORY_ID_COL + " INTEGER, " +
                MESSAGE_COL + " TEXT NOT NULL, " +
                IS_READ_COL + " BOOLEAN DEFAULT FALSE, " +
                "FOREIGN KEY (" + USER_ID_COL + ") REFERENCES " + USERS_TABLE + "(" + ID_COL + "), " +
                "FOREIGN KEY (" + CATEGORY_ID_COL + ") REFERENCES " + CATEGORIES_TABLE + "(" + CATEGORY_ID_COL + "));";

        db.execSQL(createNotificationsTable);

        // Create Reports table
        String createReportsTable = "CREATE TABLE " + REPORTS_TABLE + " (" +
                REPORT_ID_COL + " INTEGER PRIMARY KEY, " +
                USER_ID_COL + " INTEGER NOT NULL, " +
                REPORT_NAME_COL + " VARCHAR(100) NOT NULL, " +
                START_DATE_COL + " DATE NOT NULL, " +
                END_DATE_COL + " DATE NOT NULL, " +
                REPORT_DATA_COL + " TEXT, " +
                "FOREIGN KEY (" + USER_ID_COL + ") REFERENCES " + USERS_TABLE + "(" + ID_COL + "));";

        db.execSQL(createReportsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + REPORTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATIONS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + RECURRING_EXPENSES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EXPENSES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + BUDGETS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CATEGORIES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);

        // Create tables again
        onCreate(db);
    }
}