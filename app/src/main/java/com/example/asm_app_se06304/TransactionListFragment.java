package com.example.asm_app_se06304;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.asm_app_se06304.DataBase.DatabaseContext;
import com.example.asm_app_se06304.adapter.TransactionAdapter;
import com.example.asm_app_se06304.model.Budget;
import com.example.asm_app_se06304.model.Expense;
import com.example.asm_app_se06304.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionListFragment extends Fragment implements TransactionAdapter.OnTransactionActionListener {

    private ListView listTransactions;
    private DatabaseContext db;
    private int userId = 1;
    private List<Transaction> transactions;
    private TransactionAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseContext(requireActivity());
        transactions = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_list, container, false);

        listTransactions = view.findViewById(R.id.list_transactions);
        adapter = new TransactionAdapter(requireActivity(), transactions, this);
        listTransactions.setAdapter(adapter);

        loadTransactions();

        return view;
    }

    private void loadTransactions() {
        transactions.clear();

        // Load Budgets
        List<Budget> budgets = db.getAllBudgets(userId);
        for (Budget budget : budgets) {
            String budgetId = budget.getBudgetId();
            if (budgetId == null || budgetId.isEmpty()) {
                Toast.makeText(requireActivity(), "Invalid budget ID found", Toast.LENGTH_SHORT).show();
                continue;
            }

            // Get the category name for this budget
            int categoryId = budget.getCategoryId();
            String categoryName = getCategoryDisplayName(categoryId);

            transactions.add(new Transaction(
                    Long.parseLong(budgetId),
                    "Budget",
                    budget.getDescription() + " (" + categoryName + ")",
                    budget.getAmount(),
                    budget.getDate(),
                    categoryId
            ));
        }

        // Load Expenses
        List<Expense> expenses = db.getAllExpenses(userId);
        for (Expense expense : expenses) {
            // Get the category name for this expense
            int categoryId = expense.getCategoryId();
            String categoryName = getCategoryDisplayName(categoryId);

            transactions.add(new Transaction(
                    expense.getId(),
                    "Expense",
                    expense.getDescription() + " (" + categoryName + ")",
                    expense.getAmount(),
                    expense.getDate(),
                    categoryId
            ));
        }

        adapter.notifyDataSetChanged();
    }

    private String getCategoryDisplayName(int categoryId) {
        if (categoryId <= 0) {
            return "Uncategorized";
        }

        // Get the full category name from the database
        String fullName = db.getCategoryName(categoryId);

        // Extract the display name by removing the prefix
        if (fullName != null) {
            if (fullName.startsWith("Income_")) {
                return fullName.substring("Income_".length());
            } else if (fullName.startsWith("Expense_")) {
                return fullName.substring("Expense_".length());
            }
            return fullName;
        }

        return "Unknown";
    }

    @Override
    public void onEdit(Transaction transaction) {
        if (transaction.getType().equals("Budget")) {
            BudgetFragment fragment = BudgetFragment.newInstance(
                    transaction.getId(),
                    transaction.getDescription().replaceAll(" \\(.*\\)$", ""), // Remove category name from description
                    transaction.getAmount(),
                    transaction.getDate(),
                    transaction.getCategoryId()
            );

            // Set up a result listener for when the BudgetFragment finishes editing
            getParentFragmentManager().setFragmentResultListener("budget_edit_result", this, (requestKey, bundle) -> {
                if (bundle.getBoolean("budget_updated", false)) {
                    // Reload transactions after update
                    loadTransactions();

                    // Notify HomeFragment that a transaction was edited
                    Bundle result = new Bundle();
                    result.putBoolean("transaction_updated", true);
                    getParentFragmentManager().setFragmentResult("transaction_update", result);
                }
            });

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } else if (transaction.getType().equals("Expense")) {
            ExpensesFragment fragment = ExpensesFragment.newInstance(
                    transaction.getId(),
                    transaction.getDescription().replaceAll(" \\(.*\\)$", ""), // Remove category name from description
                    transaction.getAmount(),
                    transaction.getDate(),
                    transaction.getCategoryId()
            );

            // Set up a result listener for when the ExpensesFragment finishes editing
            getParentFragmentManager().setFragmentResultListener("expense_edit_result", this, (requestKey, bundle) -> {
                if (bundle.getBoolean("expense_updated", false)) {
                    // Reload transactions after update
                    loadTransactions();

                    // Notify HomeFragment that a transaction was edited
                    Bundle result = new Bundle();
                    result.putBoolean("transaction_updated", true);
                    getParentFragmentManager().setFragmentResult("transaction_update", result);
                }
            });

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onDelete(Transaction transaction) {
        if (transaction.getType().equals("Budget")) {
            // Check if there are expenses with matching suffix
            boolean hasMatchingExpenses = db.hasExpensesWithSameSuffix(userId, transaction.getId());

            if (hasMatchingExpenses) {
                // Get display name for the alert
                String displayName = getCategoryDisplayName(transaction.getCategoryId());

                new AlertDialog.Builder(requireActivity())
                        .setTitle("Cannot Delete Budget")
                        .setMessage("The '" + displayName + "' category has expenses. " +
                                "Please delete all expenses first before deleting this budget.")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                // No matching expenses - safe to delete
                deleteBudget(transaction);
            }
        } else if (transaction.getType().equals("Expense")) {
            // Existing expense deletion logic
            long result = db.deleteExpense(transaction.getId());
            handleDeletionResult(result);
        }
    }

    private void deleteBudget(Transaction transaction) {
        long result = db.deleteBudget(transaction.getId());
        handleDeletionResult(result);
    }

    private void handleDeletionResult(long result) {
        if (result > 0) {
            Toast.makeText(requireActivity(), "Transaction deleted", Toast.LENGTH_SHORT).show();
            loadTransactions();

            // Notify HomeFragment that a transaction was deleted
            Bundle resultBundle = new Bundle();
            resultBundle.putBoolean("transaction_deleted", true);
            getParentFragmentManager().setFragmentResult("transaction_update", resultBundle);
        } else {
            Toast.makeText(requireActivity(), "Error deleting transaction", Toast.LENGTH_SHORT).show();
        }
    }
}