package com.example.asm_app_se06304;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.asm_app_se06304.DataBase.DatabaseContext;
import java.util.ArrayList;
import java.util.List;

public class AddCategoryActivity extends AppCompatActivity {
    public static final String CATEGORY_UPDATED = "com.example.asm_app_se06304.CATEGORY_UPDATED";
    private EditText etCategoryName, etCategoryDescription;
    private RadioGroup rgCategoryType;
    private Button btnSaveCategory;
    private boolean isEditing = false;
    private int currentEditCategoryId = -1;
    private ListView lvExistingCategories;
    private DatabaseContext dbContext;
    private int userId = 1;
    private ArrayAdapter<String> categoryAdapter;
    private List<String> categoryList;
    private List<Integer> categoryIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        dbContext = new DatabaseContext(this);
        categoryList = new ArrayList<>();
        categoryIds = new ArrayList<>();

        // Initialize views
        etCategoryName = findViewById(R.id.etCategoryName);
        etCategoryDescription = findViewById(R.id.etCategoryDescription);
        rgCategoryType = findViewById(R.id.rgCategoryType);
        btnSaveCategory = findViewById(R.id.btnSaveCategory);
        lvExistingCategories = findViewById(R.id.lvExistingCategories);

        // Setup ListView adapter
        categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                categoryList);
        lvExistingCategories.setAdapter(categoryAdapter);

        // Load existing categories
        loadExistingCategories();

        btnSaveCategory.setOnClickListener(v -> saveCategory());

        // Add long click listener for delete/edit functionality
        lvExistingCategories.setOnItemLongClickListener((parent, view, position, id) -> {
            showOptionsDialog(position);
            return true;
        });
    }

    private void loadExistingCategories() {
        categoryList.clear();
        categoryIds.clear();

        SQLiteDatabase db = dbContext.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContext.CATEGORIES_TABLE,
                new String[]{DatabaseContext.CATEGORY_ID_COL, DatabaseContext.NAME},
                DatabaseContext.USER_ID_COL + " = ? AND " + DatabaseContext.NAME + " NOT LIKE '-- None --'",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int categoryId = cursor.getInt(0);
                String fullCategoryName = cursor.getString(1);

                // Keep the prefix (Income_ or Expense_) when displaying
                String displayName = fullCategoryName.replace("_", " ");

                categoryIds.add(categoryId);
                categoryList.add(displayName);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        categoryAdapter.notifyDataSetChanged();
    }

    private void saveCategory() {
        String name = etCategoryName.getText().toString().trim();
        String description = etCategoryDescription.getText().toString().trim();
        int selectedType = rgCategoryType.getCheckedRadioButtonId();

        if (name.isEmpty()) {
            etCategoryName.setError("Category name is required");
            return;
        }

        // Add prefix and replace spaces with underscores for storage
        String prefix = (selectedType == R.id.rbIncome ? "Income_" : "Expense_");
        String fullCategoryName = prefix + name.replace(" ", "_");

        if (isEditing) {
            long result = dbContext.updateCategory(
                    currentEditCategoryId,
                    fullCategoryName,
                    description
            );

            if (result > 0) {
                Toast.makeText(this, "Category updated successfully", Toast.LENGTH_SHORT).show();
                resetForm();
                loadExistingCategories();
                sendCategoryUpdateResult();
            } else {
                Toast.makeText(this, "Failed to update category", Toast.LENGTH_SHORT).show();
            }
        } else {
            long result = dbContext.addCategory(userId, fullCategoryName, description);

            if (result != -1) {
                Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show();
                resetForm();
                loadExistingCategories();
                sendCategoryUpdateResult();
            } else {
                Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resetForm() {
        etCategoryName.setText("");
        etCategoryDescription.setText("");
        rgCategoryType.check(R.id.rbExpense);
        isEditing = false;
        currentEditCategoryId = -1;
        btnSaveCategory.setText("Save Category");
    }

    private void showOptionsDialog(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Category Options")
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        editCategory(position);
                    } else {
                        deleteCategory(position);
                    }
                })
                .show();
    }

    private void editCategory(int position) {
        currentEditCategoryId = categoryIds.get(position);
        String displayName = categoryList.get(position);

        // Get full category name from database
        SQLiteDatabase db = dbContext.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContext.CATEGORIES_TABLE,
                new String[]{DatabaseContext.NAME, DatabaseContext.DESCRIPTION},
                DatabaseContext.CATEGORY_ID_COL + " = ?",
                new String[]{String.valueOf(currentEditCategoryId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            String fullCategoryName = cursor.getString(0);
            boolean isIncome = fullCategoryName.startsWith("Income_");

            // Set the radio button
            rgCategoryType.check(isIncome ? R.id.rbIncome : R.id.rbExpense);

            // Set the name (remove prefix and replace underscores with spaces)
            String nameWithoutPrefix = fullCategoryName.substring(fullCategoryName.indexOf('_') + 1);
            etCategoryName.setText(nameWithoutPrefix.replace("_", " "));

            // Set description
            etCategoryDescription.setText(cursor.getString(1));
        }
        cursor.close();
        db.close();

        isEditing = true;
        btnSaveCategory.setText("Update Category");
    }

    private void deleteCategory(int position) {
        int categoryId = categoryIds.get(position);
        String displayName = categoryList.get(position);

        // First check if there are any expenses in this category
        SQLiteDatabase db = dbContext.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContext.EXPENSES_TABLE,
                new String[]{DatabaseContext.EXPENSE_ID_COL},
                DatabaseContext.CATEGORY_ID_COL + " = ?",
                new String[]{String.valueOf(categoryId)},
                null, null, null);

        if (cursor.getCount() > 0) {
            cursor.close();
            new AlertDialog.Builder(this)
                    .setTitle("Cannot Delete Category")
                    .setMessage("You have to delete all expenses in '" + displayName + "' first before deleting the category.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        cursor.close();

        // Check if there are budgets associated with this category
        cursor = db.query(
                DatabaseContext.BUDGETS_TABLE,
                new String[]{DatabaseContext.BUDGET_ID_COL},
                DatabaseContext.BUDGET_CATEGORY_ID_COL + " = ?",
                new String[]{String.valueOf(categoryId)},
                null, null, null);

        if (cursor.getCount() > 0) {
            cursor.close();
            new AlertDialog.Builder(this)
                    .setTitle("Cannot Delete Category")
                    .setMessage("You have to delete all budgets in '" + displayName + "' first before deleting the category.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        cursor.close();
        db.close();

        // No expenses or budgets found - proceed with deletion
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + displayName + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    long result = dbContext.deleteCategory(categoryId);
                    if (result > 0) {
                        Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
                        loadExistingCategories();
                        sendCategoryUpdateResult();
                    } else {
                        Toast.makeText(this, "Failed to delete category", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendCategoryUpdateResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("category_updated", true);
        setResult(RESULT_OK, resultIntent);

        if (getSupportFragmentManager() != null) {
            Bundle result = new Bundle();
            result.putBoolean("category_updated", true);
            getSupportFragmentManager().setFragmentResult("category_update", result);
        }
    }
}