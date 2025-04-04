package com.example.asm_app_se06304;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.asm_app_se06304.DataBase.DatabaseContext;
import com.example.asm_app_se06304.adapter.ExpenseCategoryAdapter;
import com.example.asm_app_se06304.model.Category;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.Manifest;

public class HomeFragment extends Fragment {

    private ListView listExpenseCategories;
    private Spinner spinnerMonth, spinnerYear;
    private TextView tvTotalBudget, tvTotalSpent, tvRemainingBudget, tvBudgetPercentage, tvSeeAll;
    private DatabaseContext db;
    private int userId = 1;
    private ExpenseCategoryAdapter adapter;
    private List<Category> expenseCategories;

    // Notification variables
    private static final String NOTIFICATION_PREFS = "NotificationPrefs";
    private static final String LAST_NOTIFICATION_TIME = "lastNotificationTime";
    private static final String LAST_BUDGET_STATE = "lastBudgetState";
    private static final long NOTIFICATION_COOLDOWN = 10 * 60 * 1000; // 10 minutes
    private boolean notificationsEnabled = true;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseContext(requireActivity());
        expenseCategories = new ArrayList<>();

        // Create notification channel
        createNotificationChannel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        listExpenseCategories = view.findViewById(R.id.list_expense_categories);
        spinnerMonth = view.findViewById(R.id.spinner_month);
        spinnerYear = view.findViewById(R.id.spinner_year);
        tvTotalBudget = view.findViewById(R.id.tv_total_budget);
        tvTotalSpent = view.findViewById(R.id.tv_total_spent);
        tvRemainingBudget = view.findViewById(R.id.tv_remaining_budget);
        tvBudgetPercentage = view.findViewById(R.id.tv_budget_percentage);
        tvSeeAll = view.findViewById(R.id.see_all_button);

        // Thêm phần này ngay sau khi khởi tạo view
        checkAndRequestNotificationPermissionFirstTime();

        adapter = new ExpenseCategoryAdapter(requireActivity(), expenseCategories);
        listExpenseCategories.setAdapter(adapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, getMonths());
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, getYears());
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        Calendar calendar = Calendar.getInstance();
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH));
        spinnerYear.setSelection(getYears().indexOf(String.valueOf(calendar.get(Calendar.YEAR))));

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExpenses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExpenses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        getParentFragmentManager().setFragmentResultListener("requestKey",
                this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if (result.getBoolean("expense_added", false) || result.getBoolean(
                        "budget_added", false)) {
                    loadExpenses();
                }
            }
        });

        // Xử lý sự kiện nhấn "See All"
        tvSeeAll.setOnClickListener(v -> {
            TransactionListFragment fragment = new TransactionListFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Set up notification toggle
        ImageView notificationIcon = view.findViewById(R.id.notificationIcon);
        SharedPreferences prefs = requireActivity().getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE);
        notificationsEnabled = prefs.getBoolean("notificationsEnabled", true);
        updateNotificationIcon(notificationIcon);

        notificationIcon.setOnClickListener(v -> {
            notificationsEnabled = !notificationsEnabled;
            saveNotificationState(notificationsEnabled);
            updateNotificationIcon(notificationIcon);
            showNotificationToggleMessage();
        });

        loadExpenses();
        return view;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "budget_notification_channel",
                    "Budget Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications about budget warnings");
            NotificationManager manager = requireActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
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

        expenseCategories = db.getExpensesByMonth(userId, month, year);
        adapter.updateCategories(expenseCategories);

        double totalSpent = 0;
        for (Category category : expenseCategories) {
            totalSpent += category.getTotalAmount();
        }

        double totalBudget = db.getTotalBudget(userId);
        double remainingBudget = totalBudget - totalSpent;
        double budgetPercentage = totalBudget == 0 ? 0 : (totalSpent / totalBudget) * 100;

        DecimalFormat formatter = new DecimalFormat("#,### VNĐ");
        tvTotalBudget.setText("Total budget: " + formatter.format(totalBudget));
        tvTotalSpent.setText("Total expense: " + formatter.format(totalSpent));
        tvRemainingBudget.setText("Remaining budget: " + formatter.format(remainingBudget));
        tvBudgetPercentage.setText(String.format("%.2f%% Used budget", budgetPercentage));
// Check and send notifications
        checkBudgetAndSendNotification(totalBudget, totalSpent, remainingBudget);
    }

    private void checkBudgetAndSendNotification(double totalBudget, double totalSpent, double remainingBudget) {
        if (!notificationsEnabled) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE);
        long lastNotificationTime = prefs.getLong(LAST_NOTIFICATION_TIME, 0);
        double lastNotifiedState = Double.longBitsToDouble(prefs.getLong(LAST_BUDGET_STATE, Double.doubleToLongBits(0)));
        long currentTime = System.currentTimeMillis();

        // Check cooldown and budget state
        if (currentTime - lastNotificationTime < NOTIFICATION_COOLDOWN &&
                remainingBudget == lastNotifiedState) {
            return;
        }

        // Check notification conditions
        if (remainingBudget < 0) {
            sendNotification("Budget Alert", "You have exceeded your budget!");
            saveNotificationState(currentTime, remainingBudget);
        } else if (remainingBudget < totalBudget * 0.2) {
            sendNotification("Budget Warning", "You have used over 80% of your budget");
            saveNotificationState(currentTime, remainingBudget);
        }
    }

    private void sendNotification(String title, String message) {
// Kiểm tra quyền trước khi gửi
        if (!canSendNotification()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Hiển thị hướng dẫn nếu đã từ chối trước đó
                if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    showPermissionDeniedDialog();
                    return;
                }
            }
            requestNotificationPermission();
            return;
        }

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "budget_notification_channel")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Show notification
        NotificationManagerCompat.from(requireContext()).notify(2, builder.build());
    }

    private boolean canSendNotification() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    100
            );
        }
    }

    private void saveNotificationState(long time, double budgetState) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(LAST_NOTIFICATION_TIME, time);
        editor.putLong(LAST_BUDGET_STATE, Double.doubleToLongBits(budgetState));
        editor.apply();
    }

    private void saveNotificationState(boolean enabled) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("notificationsEnabled", enabled).apply();
    }

    private void updateNotificationIcon(ImageView icon) {
        if (notificationsEnabled) {
            icon.setImageResource(R.drawable.bell);
        } else {
            icon.setImageResource(R.drawable.bell_off);
        }
    }

    private void showNotificationToggleMessage() {
        String message = notificationsEnabled ? "Notifications enabled" : "Notifications disabled";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100 || requestCode == 200) { // Cập nhật để xử lý cả 2 trường hợp
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, có thể gửi thông báo ngay nếu cần
                loadExpenses();
            } else if (requestCode == 200) { // Chỉ hiển thị với yêu cầu lần đầu
                Toast.makeText(requireContext(),
                        "You can enable permissions in Settings > Apps > " + getString(R.string.app_name),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkAndRequestNotificationPermissionFirstTime() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("is_first_time", true);

        if (isFirstTime && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Đánh dấu đã mở ứng dụng lần đầu
            prefs.edit().putBoolean("is_first_time", false).apply();

            // Kiểm tra và yêu cầu quyền
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                // Hiển thị giải thích trước khi yêu cầu
                new AlertDialog.Builder(requireContext())
                        .setTitle("Important Notice")
                        .setMessage("To receive budget alerts, please grant notification permissions.")
                        .setPositiveButton("Agree", (dialog, which) -> {
                            requestPermissions(
                                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                    200 // Sử dụng request code khác với các yêu cầu sau này
                            );
                        })
                        .setNegativeButton("Later", null)
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Quyền thông báo bị từ chối")
                .setMessage("Vui lòng bật quyền trong Cài đặt để nhận cảnh báo quan trọng")
                .setPositiveButton("Mở Cài đặt", (d, w) -> openAppSettings())
                .setNegativeButton("Để sau", null)
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", requireContext().getPackageName(), null));
        startActivity(intent);
    }
}