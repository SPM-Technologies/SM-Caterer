package com.smtech.SM_Caterer.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smtech.SM_Caterer.domain.entity.Order;
import com.smtech.SM_Caterer.domain.entity.Payment;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.service.PdfReceiptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Service implementation for PDF receipt generation using OpenPDF.
 */
@Slf4j
@Service
public class PdfReceiptServiceImpl implements PdfReceiptService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, Color.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @Value("${app.upload.logo-dir:uploads/logos}")
    private String logoUploadDir;

    @Override
    public String generateReceipt(Payment payment) {
        try {
            Path receiptDir = Paths.get(uploadPath, "receipts", "tenant-" + payment.getTenant().getId());
            Files.createDirectories(receiptDir);

            String filename = String.format("receipt-%s.pdf", payment.getPaymentNumber());
            Path filePath = receiptDir.resolve(filename);

            byte[] pdfBytes = generateReceiptBytes(payment);

            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(pdfBytes);
            }

            log.info("Generated PDF receipt for payment {} at: {}", payment.getPaymentNumber(), filePath);
            return filePath.toString();

        } catch (IOException e) {
            log.error("Failed to generate PDF receipt: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF receipt", e);
        }
    }

    @Override
    public byte[] generateReceiptBytes(Payment payment) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Tenant tenant = payment.getTenant();
            Order order = payment.getOrder();

            // Add receipt header
            addReceiptHeader(document, tenant);

            // Add receipt title
            Paragraph title = new Paragraph("PAYMENT RECEIPT", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add payment details table
            addPaymentDetails(document, payment);

            // Add order details table
            if (order != null) {
                addOrderDetails(document, order);
            }

            // Add footer
            addFooter(document, tenant);

        } catch (DocumentException e) {
            log.error("Failed to create PDF document: {}", e.getMessage());
            throw new RuntimeException("Failed to create PDF document", e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private void addReceiptHeader(Document document, Tenant tenant) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        // Add logo if available
        if (tenant.hasLogo()) {
            try {
                String logoPath = tenant.getLogoPath();
                // Handle both full path and relative path
                Path fullLogoPath;
                if (logoPath.contains("logos/")) {
                    String filename = logoPath.substring(logoPath.lastIndexOf("logos/") + 6);
                    fullLogoPath = Paths.get(logoUploadDir, filename);
                } else {
                    fullLogoPath = Paths.get(logoPath);
                }

                if (Files.exists(fullLogoPath)) {
                    Image logo = Image.getInstance(fullLogoPath.toString());
                    logo.scaleToFit(100, 60);
                    logo.setAlignment(Element.ALIGN_CENTER);

                    PdfPCell logoCell = new PdfPCell(logo);
                    logoCell.setBorder(Rectangle.NO_BORDER);
                    logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    logoCell.setPaddingBottom(10);
                    headerTable.addCell(logoCell);
                }
            } catch (Exception e) {
                log.warn("Failed to add logo to receipt: {}", e.getMessage());
                // Continue without logo
            }
        }

        // Business/Display name - use effective display name
        String displayName = tenant.getEffectiveDisplayName();
        PdfPCell businessCell = new PdfPCell(new Phrase(displayName, HEADER_FONT));
        businessCell.setBorder(Rectangle.NO_BORDER);
        businessCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTable.addCell(businessCell);

        // Tagline if available
        if (tenant.getTagline() != null && !tenant.getTagline().isBlank()) {
            Font taglineFont = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);
            PdfPCell taglineCell = new PdfPCell(new Phrase(tenant.getTagline(), taglineFont));
            taglineCell.setBorder(Rectangle.NO_BORDER);
            taglineCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(taglineCell);
        }

        // Address
        if (tenant.getAddress() != null) {
            String address = buildAddress(tenant);
            PdfPCell addressCell = new PdfPCell(new Phrase(address, SMALL_FONT));
            addressCell.setBorder(Rectangle.NO_BORDER);
            addressCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(addressCell);
        }

        // Contact info
        StringBuilder contact = new StringBuilder();
        if (tenant.getPhone() != null) {
            contact.append("Phone: ").append(tenant.getPhone());
        }
        if (tenant.getEmail() != null) {
            if (contact.length() > 0) contact.append(" | ");
            contact.append("Email: ").append(tenant.getEmail());
        }
        if (contact.length() > 0) {
            PdfPCell contactCell = new PdfPCell(new Phrase(contact.toString(), SMALL_FONT));
            contactCell.setBorder(Rectangle.NO_BORDER);
            contactCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(contactCell);
        }

        // GSTIN if available
        if (tenant.getGstin() != null) {
            PdfPCell gstinCell = new PdfPCell(new Phrase("GSTIN: " + tenant.getGstin(), SMALL_FONT));
            gstinCell.setBorder(Rectangle.NO_BORDER);
            gstinCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(gstinCell);
        }

        headerTable.setSpacingAfter(15);
        document.add(headerTable);

        // Divider
        document.add(new Paragraph("━".repeat(80), SMALL_FONT));
    }

    private void addPaymentDetails(Document document, Payment payment) throws DocumentException {
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2});

        addTableRow(table, "Receipt No:", payment.getPaymentNumber(), true);
        addTableRow(table, "Payment Date:", payment.getPaymentDate().format(DATE_FORMATTER), false);
        addTableRow(table, "Amount:", CURRENCY_FORMAT.format(payment.getAmount()), true);
        addTableRow(table, "Payment Method:", payment.getPaymentMethod().name(), false);

        if (payment.getTransactionReference() != null && !payment.getTransactionReference().isBlank()) {
            addTableRow(table, "Transaction Ref:", payment.getTransactionReference(), false);
        }

        if (payment.getUpiId() != null && !payment.getUpiId().isBlank()) {
            addTableRow(table, "UPI ID:", payment.getUpiId(), false);
        }

        addTableRow(table, "Status:", payment.getStatus().name(), false);

        table.setSpacingAfter(15);
        document.add(table);
    }

    private void addOrderDetails(Document document, Order order) throws DocumentException {
        Paragraph orderHeader = new Paragraph("Order Details", HEADER_FONT);
        orderHeader.setSpacingBefore(10);
        orderHeader.setSpacingAfter(5);
        document.add(orderHeader);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2});

        addTableRow(table, "Order No:", order.getOrderNumber(), false);

        if (order.getCustomer() != null) {
            addTableRow(table, "Customer:", order.getCustomer().getName(), false);
        }

        if (order.getEventDate() != null) {
            addTableRow(table, "Event Date:", order.getEventDate().format(DATE_FORMATTER), false);
        }

        if (order.getTotalAmount() != null) {
            addTableRow(table, "Order Total:", CURRENCY_FORMAT.format(order.getTotalAmount()), false);
        }

        table.setSpacingAfter(15);
        document.add(table);
    }

    private void addFooter(Document document, Tenant tenant) throws DocumentException {
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("━".repeat(80), SMALL_FONT));

        Paragraph thanks = new Paragraph("Thank you for your payment!", BOLD_FONT);
        thanks.setAlignment(Element.ALIGN_CENTER);
        thanks.setSpacingBefore(10);
        document.add(thanks);

        // Add company name and tagline in footer
        String footerText = tenant.getEffectiveDisplayName();
        if (tenant.getTagline() != null && !tenant.getTagline().isBlank()) {
            footerText += " | " + tenant.getTagline();
        }
        Paragraph companyFooter = new Paragraph(footerText, SMALL_FONT);
        companyFooter.setAlignment(Element.ALIGN_CENTER);
        companyFooter.setSpacingBefore(5);
        document.add(companyFooter);

        Paragraph note = new Paragraph("This is a computer generated receipt.", SMALL_FONT);
        note.setAlignment(Element.ALIGN_CENTER);
        note.setSpacingBefore(5);
        document.add(note);
    }

    private void addTableRow(PdfPTable table, String label, String value, boolean highlight) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5);
        table.addCell(labelCell);

        Font valueFont = highlight ? BOLD_FONT : NORMAL_FONT;
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);
        table.addCell(valueCell);
    }

    private String buildAddress(Tenant tenant) {
        StringBuilder sb = new StringBuilder();
        if (tenant.getAddress() != null) {
            sb.append(tenant.getAddress());
        }
        if (tenant.getCity() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(tenant.getCity());
        }
        if (tenant.getState() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(tenant.getState());
        }
        if (tenant.getPincode() != null) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(tenant.getPincode());
        }
        return sb.toString();
    }
}
