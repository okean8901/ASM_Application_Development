package com.example.asm_app_se06304;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.asm_app_se06304.DataBase.DatabaseContext;
import com.example.asm_app_se06304.adapter.ExpenseCategoryAdapter;
import com.example.asm_app_se06304.model.ExpenseCategory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private ListView listExpenseCategories;
    private Spinner spinnerMonth, spinnerYear;
    private TextView tvTotalBudget, tvTotalSpent, tvRemainingBudget, tvBudgetPercentage;
    private DatabaseContext db;
    private int userId = 1; // Giả định userId
    private ExpenseCategoryAdapter adapter;
    private List<ExpenseCategory> expenseCategories;
    private final double TOTAL_BUDGET = 0; // Giả định ngân sách cố định

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseContext(requireActivity());
        expenseCategories = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo views
        listExpenseCategories = view.findViewById(R.id.list_expense_categories);
        spinnerMonth = view.findViewById(R.id.spinner_month);
        spinnerYear = view.findViewById(R.id.spinner_year);
        tvTotalBudget = view.findViewById(R.id.tv_total_budget);
        tvTotalSpent = view.findViewById(R.id.tv_total_spent);
        tvRemainingBudget = view.findViewById(R.id.tv_remaining_budget);
        tvBudgetPercentage = view.findViewById(R.id.tv_budget_percentage);

        // Thiết lập adapter cho ListView
        adapter = new ExpenseCategoryAdapter(requireActivity(), expenseCategories);
        listExpenseCategories.setAdapter(adapter);

        // Thiết lập Spinner tháng
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, getMonths());
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Thiết lập Spinner năm
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, getYears());
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Thiết lập tháng và năm hiện tại
        Calendar calendar = Calendar.getInstance();
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH));
        spinnerYear.setSelection(getYears().indexOf(String.valueOf(calendar.get(Calendar.YEAR))));

        // Lắng nghe sự kiện chọn tháng/năm
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExpenses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExpenses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Lắng nghe kết quả từ ExpensesFragment
        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if (result.getBoolean("expense_added", false)) {
                    loadExpenses();
                }
            }
        });



        // Lắng nghe kết quả từ BudgetFragment
        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if (result.getBoolean("budget_added", false)) {
                    loadExpenses();
                }
            }
        });

        // Tải dữ liệu ban đầu
        loadExpenses();
        return view;
    }

    private List<String> getMonths() {
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(String.format("%02d", i));
        }
        return months;
    }

    private List<String> getYears() {
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            years.add(String.valueOf(i));
        }
        return years;
    }

    private void loadExpenses() {
        int month = Integer.parseInt(spinnerMonth.getSelectedItem().toString());
        int year = Integer.parseInt(spinnerYear.getSelectedItem().toString());

        // Load expenses
        expenseCategories = db.getExpensesByMonth(userId, month, year);
        adapter.updateCategories(expenseCategories);

        // Calculate total spent
        double totalSpent = 0;
        for (ExpenseCategory category : expenseCategories) {
            totalSpent += category.getTotalAmount();
        }

        // Get total budget from the database
        double totalBudget = db.getTotalBudget(userId); // Get total budget from the database
        double remainingBudget = totalBudget - totalSpent;
        double budgetPercentage = (totalSpent / totalBudget) * 100;


        // Update TextViews
        DecimalFormat formatter = new DecimalFormat("#,### VNĐ");
        tvTotalBudget.setText("Tổng ngân sách: " + formatter.format(totalBudget));
        tvTotalSpent.setText("Tổng chi tiêu: " + formatter.format(totalSpent));
        tvRemainingBudget.setText("Ngân sách còn lại: " + formatter.format(remainingBudget));
        tvBudgetPercentage.setText(String.format("%.2f%% ngân sách đã sử dụng", budgetPercentage));
    }
}