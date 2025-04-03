package com.example.asm_app_se06304.utils;

import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
public class PdfGenerator {
    public static boolean createPdf(File file, List<String> data) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        int yPosition = 50;
        for (String line : data) {
            page.getCanvas().drawText(line, 50, yPosition, new android.graphics.Paint());
            yPosition += 30;
        }

        document.finishPage(page);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e("PdfGenerator", "Lỗi khi lưu PDF: " + e.getMessage());
            return false;
        }
    }
}
