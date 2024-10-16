package com.paw.schoolMoney.report;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.paw.schoolMoney.transaction.Transaction;
import com.paw.schoolMoney.transaction.TransactionType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class ReportService {

    public byte[] createPdfReport(List<Transaction> transactions, String reportTitle) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Sortowanie transakcji po dacie malejąco
            transactions.sort(Comparator.comparing(Transaction::getCreatedAt).reversed());

            // Load the font that supports Polish characters (UTF-8)
            InputStream fontStream = new ClassPathResource("fonts/NotoSans.ttf").getInputStream();
            BaseFont baseFont = BaseFont.createFont("NotoSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, BaseFont.NOT_CACHED, fontStream.readAllBytes(), null);

            Font font = new Font(baseFont, 12);  // Use this font for the rest of the document

            // Add title with the font that supports Polish characters
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Paragraph title = new Paragraph(reportTitle, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" ", font));  // Empty space with the same font

            // Create table with headers
            PdfPTable table = new PdfPTable(4); // 4 columns
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Add table headers
            addTableHeader(table, "Data", font);
            addTableHeader(table, "Kwota", font);
            addTableHeader(table, "Opis", font);
            addTableHeader(table, "Rodzic", font);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            // Lista typów transakcji, które mają znak minus
            List<TransactionType> negativeTransactionTypes = Arrays.asList(
                    TransactionType.WITHDRAW, // WYPŁATA
                    TransactionType.TRANSFER, // PRZELEW
                    TransactionType.FUNDRAISE_DEPOSIT, // WPŁATA NA ZBIÓRKĘ
                    TransactionType.FUNDRAISE_WITHDRAW_CORRECTION
            );


            // Add table rows
            for (Transaction transaction : transactions) {
                // Parsowanie daty na LocalDateTime i formatowanie
                String formattedDate = transaction.getCreatedAt().format(formatter);

                // Sprawdzanie, czy typ transakcji znajduje się na liście typów z minusem
                String amountText = transaction.getAmount().toString() + " PLN";
                if (negativeTransactionTypes.contains(transaction.getType())) {
                    amountText = "-" + amountText; // Dodanie znaku minus
                }

                table.addCell(new Phrase(formattedDate, font));
                table.addCell(new Phrase(amountText, font));
                table.addCell(new Phrase(transaction.getDescription(), font));
                table.addCell(new Phrase(transaction.getUser().getFirstname() + " " + transaction.getUser().getLastname(), font));
            }


            document.add(table);
            document.close();

            return baos.toByteArray();  // Return the PDF content as a byte array

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    // Helper method to add a table header with a specific font
    private void addTableHeader(PdfPTable table, String headerTitle, Font font) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
        header.setPhrase(new Phrase(headerTitle, font));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header);
    }
}
