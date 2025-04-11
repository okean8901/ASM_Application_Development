package com.example.asm_app_se06304;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.asm_app_se06304.DataBase.DatabaseContext;

public class AddCategoryActivity extends AppCompatActivity {
    private EditText etCategoryName, etCategoryDescription;
    private RadioGroup rgCategoryType;
    private Button btnSaveCategory;
    private DatabaseContext dbContext;
    private int userId = 1; // Assuming current user has ID = 1

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        dbContext = new DatabaseContext(this);

        etCategoryName = findViewById(R.id.etCategoryName);
        etCategoryDescription = findViewById(R.id.etCategoryDescription);
        rgCategoryType = findViewById(R.id.rgCategoryType);
        btnSaveCategory = findViewById(R.id.btnSaveCategory);

        btnSaveCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCategory();
            }
        });
    }

    private void saveCategory() {
        String name = etCategoryName.getText().toString().trim();
        String description = etCategoryDescription.getText().toString().trim();
        int selectedType = rgCategoryType.getCheckedRadioButtonId();

        if (name.isEmpty()) {
            etCategoryName.setError("Category name is required");
            return;
        }

        String prefix = (selectedType == R.id.rbIncome ? "Income_" : "Expense_");
        String fullCategoryName = prefix + name.replace(" ", "_");

        long result = dbContext.addCategory(userId, fullCategoryName, description);

        if (result != -1) {
            Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show();

            // Notify fragments using Fragment Result API
            Bundle resultBundle = new Bundle();
            resultBundle.putString("category_type", selectedType == R.id.rbIncome ? "Income" : "Expense");
            getSupportFragmentManager().setFragmentResult("category_update", resultBundle);

            finish();
        } else {
            Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show();
        }
    }
}
