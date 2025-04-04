package com.example.asm_app_se06304;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.asm_app_se06304.DataBase.DatabaseContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpensesFragment extends Fragment {

    private EditText etDescription, etAmount, etDate;
    private Spinner spinnerCategory;
    private Button btnSave;
    private DatabaseContext db;
    private int userId = 1;
    private Map<String, Integer> categoryMap;
    private long expenseId = -1;

    public ExpensesFragment() {
        // Required empty public constructor
    }

    public static ExpensesFragment newInstance() {
        return new ExpensesFragment();
    }

    public static ExpensesFragment newInstance(long expenseId, String description, double amount, String date, int categoryId) {
        ExpensesFragment fragment = new ExpensesFragment();
        Bundle args = new Bundle();
        args.putLong("expenseId", expenseId);
        args.putString("description", description);
        args.putDouble("amount", amount);
        args.putString("date", date);
        args.putInt("categoryId", categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        etDescription = view.findViewById(R.id.et_description);
        etAmount = view.findViewById(R.id.et_amount);
        etDate = view.findViewById(R.id.et_date);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        btnSave = view.findViewById(R.id.btn_save);
        db = new DatabaseContext(requireActivity());
        categoryMap = new HashMap<>();

        Bundle args = getArguments();
        if (args != null && args.containsKey("expenseId")) {
            expenseId = args.getLong("expenseId", -1);
            etDescription.setText(args.getString("description", ""));
            etAmount.setText(String.valueOf(args.getDouble("amount", 0.0)));
            etDate.setText(args.getString("date", ""));
            int categoryId = args.getInt("categoryId", -1);
            setupCategorySpinner(categoryId);
        } else {
            setupCategorySpinner(-1);
        }

        etDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveExpense());

        return view;
    }

    private void setupCategorySpinner(int selectedCategoryId) {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase dbReadable = db.getReadableDatabase();

        Cursor cursor = dbReadable.rawQuery(
                "SELECT " + DatabaseContext.CATEGORY_ID_COL + ", " + DatabaseContext.NAME +
                        " FROM " + DatabaseContext.CATEGORIES_TABLE +
                        " WHERE " + DatabaseContext.USER_ID_COL + " = ? AND " +
                        DatabaseContext.NAME + " LIKE 'Expense_%'",
                new String[]{String.valueOf(userId)}
        );

        int selectedPosition = 0;
        if (cursor.moveToFirst()) {
            int index = 0;
            do {
                int categoryId = cursor.getInt(0);
                String fullName = cursor.getString(1);
                String displayName = fullName.substring("Expense_".length());
                categories.add(displayName);
                categoryMap.put(displayName, categoryId);
                if (categoryId == selectedCategoryId) {
                    selectedPosition = index;
                }
                index++;
                Log.d("ExpensesFragment", "Category: " + displayName + " (ID: " + categoryId + ")");
            } while (cursor.moveToNext());
        } else {
            // Default expense categories
            String[] defaultCategories = {"Food", "Transport", "Entertainment", "Shopping", "Housing", "Education"};
            for (int i = 0; i < defaultCategories.length; i++) {
                categories.add(defaultCategories[i]);
                categoryMap.put(defaultCategories[i], i + 1);
            }
            Toast.makeText(requireActivity(), "No expense categories found, using defaults", Toast.LENGTH_LONG).show();
        }
        cursor.close();
        dbReadable.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        if (selectedCategoryId != -1) {
            spinnerCategory.setSelection(selectedPosition);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                requireActivity(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    etDate.setText(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void saveExpense() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String categoryDisplayName = spinnerCategory.getSelectedItem().toString();

        if (description.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(requireActivity(), "Invalid date format (YYYY-MM-DD)", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(requireActivity(), "Amount must be positive", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireActivity(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer categoryId = categoryMap.get(categoryDisplayName);
        if (categoryId == null) {
            Toast.makeText(requireActivity(), "Invalid category", Toast.LENGTH_SHORT).show();
            return;
        }

        long result;
        if (expenseId == -1) {
            result = db.addExpense(userId, categoryId, description, amount, date);
        } else {
            result = db.updateExpense(expenseId, userId, categoryId, description, amount, date);
        }

        if (result != -1) {
            Toast.makeText(requireActivity(),
                    expenseId == -1 ? "Expense added" : "Expense updated",
                    Toast.LENGTH_SHORT).show();
            clearInputs();
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(requireActivity(), "Error saving expense", Toast.LENGTH_SHORT).show();
            Log.e("ExpensesFragment", "Save failed for user: " + userId);
        }
    }

    private void clearInputs() {
        etDescription.setText("");
        etAmount.setText("");
        etDate.setText("");
        spinnerCategory.setSelection(0);
    }
}