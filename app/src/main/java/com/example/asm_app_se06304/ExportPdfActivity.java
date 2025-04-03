package com.example.asm_app_se06304;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.pdf.PdfDocument;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
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
                Toast.makeText(this, "Please select a time period.", Toast.LENGTH_SHORT).show();
                return;
            }
            requestStoragePermission();
        });
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // Format as yyyy-MM-dd for database
                    String dbDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    // Format as dd/MM/yyyy for display
                    String displayDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);

                    if (isStartDate) {
                        startDate = dbDate;
                        tvStartDate.setText(displayDate);
                    } else {
                        endDate = dbDate;
                        tvEndDate.setText(displayDate);
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
                Toast.makeText(this, "You need to grant permission to save the PDF!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generatePdf() {
        try {
            // Lấy dữ liệu chi tiết
            List<String> detailedChanges = databaseContext.getDetailedBudgetChanges(startDate, endDate);

            if (detailedChanges.isEmpty()) {
                Toast.makeText(this, "No budget changes found in selected period", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileName = "Budget_Report_" + System.currentTimeMillis() + ".pdf";
            OutputStream outputStream = createOutputStream(fileName);

            if (outputStream != null) {
                Document document = new Document();
                PdfWriter.getInstance(document, outputStream);
                document.open();

                // Setup font
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
                Font titleFont = new Font(bf, 18, Font.BOLD);
                Font headerFont = new Font(bf, 12, Font.BOLD);
                Font normalFont = new Font(bf, 10);

                // Add title
                document.add(new Paragraph("DETAILED BUDGET REPORT", titleFont));
                document.add(new Paragraph(" "));

                // Add period info
                document.add(new Paragraph("Period: From " + startDate + " to " + endDate, headerFont));
                document.add(new Paragraph("Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()), normalFont));
                document.add(new Paragraph(" "));

                // Create table
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);

                // Add table headers
                table.addCell(new PdfPCell(new Phrase("Date", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Category", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Description", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Amount", headerFont)));

                // Add data rows
                for (String change : detailedChanges) {
                    String[] parts = change.split("\\|");
                    for (String part : parts) {
                        table.addCell(new PdfPCell(new Phrase(part.trim(), normalFont)));
                    }
                }

                document.add(table);

                // Add summary
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Total records: " + detailedChanges.size(), headerFont));

                document.close();
                outputStream.close();

                Toast.makeText(this, "PDF exported successfully!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PDF Export", "Error", e);
        }
    }

    private OutputStream createOutputStream(String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            ContentResolver resolver = getContentResolver();
            Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);
            return uri != null ? resolver.openOutputStream(uri) : null;
        } else {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            return new FileOutputStream(file);
        }
    }
}
