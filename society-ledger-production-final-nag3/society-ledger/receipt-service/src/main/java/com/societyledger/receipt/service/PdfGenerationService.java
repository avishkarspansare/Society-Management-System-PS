package com.societyledger.receipt.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.societyledger.receipt.entity.Receipt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Generates professional maintenance receipt PDFs using OpenPDF.
 *
 * Layout:
 * ┌────────────────────────────────────────────┐
 * │         SOCIETY LEDGER                      │
 * │         [Society Name]                      │
 * │─────────────────────────────────────────────│
 * │ PAYMENT RECEIPT                             │
 * │ Receipt No: RCP-2024-00001                  │
 * │─────────────────────────────────────────────│
 * │ Flat: B403      Resident: John Doe          │
 * │ Month: January 2024   Amount: ₹2,500.00     │
 * │─────────────────────────────────────────────│
 * │ ✓ Payment Received                          │
 * └────────────────────────────────────────────┘
 */
@Slf4j
@Service
public class PdfGenerationService {

    private static final Color HEADER_BG   = new Color(30, 64, 175);   // deep blue
    private static final Color ACCENT_BG   = new Color(239, 246, 255);  // light blue
    private static final Color SUCCESS_GREEN = new Color(22, 163, 74);
    private static final Color TEXT_DARK   = new Color(17, 24, 39);
    private static final Color TEXT_MUTED  = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(209, 213, 219);

    private static final String RECEIPT_DIR = System.getProperty("user.home")
            + "/society-ledger/receipts/";

    /**
     * Generate a PDF receipt, persist it to disk, and return the file path.
     */
    public String generateReceiptPdf(Receipt receipt) {
        try {
            byte[] pdfBytes = buildPdf(receipt);
            return savePdf(receipt, pdfBytes);
        } catch (Exception e) {
            log.error("PDF generation failed for receipt {}: {}", receipt.getReceiptNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate receipt PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Return PDF bytes for streaming download (does not re-generate from disk).
     */
    public byte[] generateReceiptPdfBytes(Receipt receipt) {
        try {
            return buildPdf(receipt);
        } catch (Exception e) {
            log.error("PDF generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate receipt PDF.", e);
        }
    }

    private byte[] buildPdf(Receipt receipt) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 40, 40);
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document, receipt.getSocietyName());
        addReceiptTitle(document, receipt.getReceiptNumber());
        addDivider(document);
        addReceiptDetails(document, receipt);
        addDivider(document);
        addPaymentConfirmation(document, receipt);
        addFooter(document);

        document.close();
        return baos.toByteArray();
    }

    private void addHeader(Document doc, String societyName) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(20);
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph brandLine = new Paragraph("SOCIETY LEDGER",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Font.BOLD, Color.WHITE));
        brandLine.setAlignment(Element.ALIGN_CENTER);

        Paragraph societyLine = new Paragraph(societyName != null ? societyName : "Housing Society",
                FontFactory.getFont(FontFactory.HELVETICA, 13, Font.NORMAL, new Color(199, 210, 254)));
        societyLine.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(brandLine);
        cell.addElement(societyLine);
        headerTable.addCell(cell);

        doc.add(headerTable);
        doc.add(Chunk.NEWLINE);
    }

    private void addReceiptTitle(Document doc, String receiptNumber) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 1f});

        PdfPCell titleCell = new PdfPCell(new Phrase("PAYMENT RECEIPT",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD, TEXT_DARK)));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPaddingTop(8);
        titleCell.setPaddingBottom(4);

        PdfPCell receiptNumCell = new PdfPCell(new Phrase("Receipt No: " + receiptNumber,
                FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, TEXT_MUTED)));
        receiptNumCell.setBorder(Rectangle.NO_BORDER);
        receiptNumCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        receiptNumCell.setPaddingTop(12);

        table.addCell(titleCell);
        table.addCell(receiptNumCell);
        doc.add(table);
    }

    private void addDivider(Document doc) throws DocumentException {
        PdfPTable divider = new PdfPTable(1);
        divider.setWidthPercentage(100);
        PdfPCell line = new PdfPCell();
        line.setBackgroundColor(BORDER_COLOR);
        line.setFixedHeight(1f);
        line.setBorder(Rectangle.NO_BORDER);
        divider.addCell(line);
        doc.add(Chunk.NEWLINE);
        doc.add(divider);
        doc.add(Chunk.NEWLINE);
    }

    private void addReceiptDetails(Document doc, Receipt receipt) throws DocumentException {
        PdfPTable detailTable = new PdfPTable(2);
        detailTable.setWidthPercentage(100);
        detailTable.setSpacingBefore(10);
        detailTable.setSpacingAfter(10);

        addDetailRow(detailTable, "Flat Number", receipt.getFlatNumber());
        addDetailRow(detailTable, "Resident Name", receipt.getResidentName());
        addDetailRow(detailTable, "Payment For",
                Month.of(receipt.getPaymentMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + receipt.getPaymentYear());
        addAmountRow(detailTable, "Amount Paid", receipt.getAmount());
        addDetailRow(detailTable, "Generated On",
                receipt.getGeneratedAt().toString().substring(0, 10));

        doc.add(detailTable);
    }

    private void addDetailRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, TEXT_MUTED);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, TEXT_DARK);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(ACCENT_BG);
        labelCell.setPadding(8);
        labelCell.setBorderColor(BORDER_COLOR);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "—", valueFont));
        valueCell.setPadding(8);
        valueCell.setBorderColor(BORDER_COLOR);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addAmountRow(PdfPTable table, String label, BigDecimal amount) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, TEXT_MUTED);
        Font amountFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Font.BOLD, HEADER_BG);

        String formattedAmount = "₹ " + NumberFormat.getNumberInstance(new Locale("en", "IN"))
                .format(amount);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(ACCENT_BG);
        labelCell.setPadding(8);
        labelCell.setBorderColor(BORDER_COLOR);

        PdfPCell amountCell = new PdfPCell(new Phrase(formattedAmount, amountFont));
        amountCell.setPadding(8);
        amountCell.setBorderColor(BORDER_COLOR);

        table.addCell(labelCell);
        table.addCell(amountCell);
    }

    private void addPaymentConfirmation(Document doc, Receipt receipt) throws DocumentException {
        PdfPTable confirmTable = new PdfPTable(1);
        confirmTable.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(240, 253, 244));
        cell.setBorderColor(new Color(134, 239, 172));
        cell.setPadding(14);

        Paragraph checkmark = new Paragraph("✓  Payment Received Successfully",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD, SUCCESS_GREEN));
        checkmark.setAlignment(Element.ALIGN_CENTER);

        Paragraph note = new Paragraph(
                "This is a computer-generated receipt and does not require a signature.",
                FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC, TEXT_MUTED));
        note.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(checkmark);
        cell.addElement(note);
        confirmTable.addCell(cell);
        doc.add(confirmTable);
    }

    private void addFooter(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
                "Society Ledger — Financial Transparency Platform | Powered by societyledger.com",
                FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, TEXT_MUTED));
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    private String savePdf(Receipt receipt, byte[] pdfBytes) throws IOException {
        Path dir = Paths.get(RECEIPT_DIR + receipt.getSocietyId() + "/");
        Files.createDirectories(dir);
        String fileName = receipt.getReceiptNumber() + ".pdf";
        Path filePath = dir.resolve(fileName);
        Files.write(filePath, pdfBytes);
        log.info("Receipt PDF saved: {}", filePath);
        return filePath.toString();
    }
}
