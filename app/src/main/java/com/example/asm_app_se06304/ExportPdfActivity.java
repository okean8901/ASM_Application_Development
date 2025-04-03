package com.example.asm_app_se06304;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.asm_app_se06304.DataBase.DatabaseContext;
import com.example.asm_app_se06304.model.Expense;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class ExportPdfActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 100;

    private TextView tvStartDate, tvEndDate;
    private Button btnSelectStartDate, btnSelectEndDate, btnGeneratePDF;
    private String startDate = "", endDate = "";
    private DatabaseContext databaseContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_export);

        databaseContext = new DatabaseContext(this);

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        btnGeneratePDF = findViewById(R.id.btnGeneratePDF);

        btnSelectStartDate.setOnClickListener(v -> showDatePicker(true));
        btnSelectEndDate.setOnClickListener(v -> showDatePicker(false));
        btnGeneratePDF.setOnClickListener(v -> {
            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn khoảng thời gian", Toast.LENGTH_SHORT).show();
                return;
            }
            requestStoragePermission();
        });
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    if (isStartDate) {
                        startDate = date;
                        tvStartDate.setText(startDate);
                    } else {
                        endDate = date;
                        tvEndDate.setText(endDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            } else {
                generatePdf();
            }
        } else {
            generatePdf();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePdf();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để lưu PDF!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generatePdf() {
        try {
            // Lấy danh sách thay đổi ngân sách từ database
            List<String> budgetChanges = databaseContext.getBudgetChanges(startDate, endDate);

            if (budgetChanges.isEmpty()) {
                Toast.makeText(this, "Không có thay đổi ngân sách trong khoảng thời gian này", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileName = "Budget_Changes_Report_" + System.currentTimeMillis() + ".pdf";
            OutputStream outputStream = null;

            // Tạo file PDF (giữ nguyên phần tạo file như trước)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                ContentResolver contentResolver = getContentResolver();
                Uri uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), values);

                if (uri != null) {
                    outputStream = contentResolver.openOutputStream(uri);
                }
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDir, fileName);
                outputStream = new FileOutputStream(file);
            }

            if (outputStream != null) {
                Document document = new Document();
                PdfWriter.getInstance(document, outputStream);
                document.open();

                // Thêm tiêu đề và thông tin cơ bản
                document.add(new Paragraph("BÁO CÁO THAY ĐỔI NGÂN SÁCH"));
                document.add(new Paragraph(" ")); // Dòng trống
                document.add(new Paragraph("Từ ngày: " + startDate + "  Đến ngày: " + endDate));
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Tổng số thay đổi: " + budgetChanges.size()));
                document.add(new Paragraph(" "));

                // Thêm danh sách các thay đổi
                document.add(new Paragraph("Chi tiết các thay đổi:"));
                document.add(new Paragraph(" "));

                for (String change : budgetChanges) {
                    document.add(new Paragraph(change));
                }

                document.close();
                outputStream.close();

                Toast.makeText(this, "Đã xuất PDF báo cáo thay đổi ngân sách thành công!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Không thể tạo file PDF!", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi xuất PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
