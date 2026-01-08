package com.smtech.SM_Caterer.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.smtech.SM_Caterer.service.QrCodeGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service implementation for UPI QR code generation using ZXing library.
 *
 * UPI URI Format:
 * upi://pay?pa=<VPA>&pn=<NAME>&am=<AMOUNT>&cu=INR&tn=<NOTE>
 */
@Slf4j
@Service
public class QrCodeGeneratorServiceImpl implements QrCodeGeneratorService {

    private static final int QR_CODE_SIZE = 300;
    private static final String CURRENCY = "INR";

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public String generateQrCode(String upiId, String payeeName, BigDecimal amount,
                                  String note, Long orderId, Long tenantId) {
        try {
            String upiUri = generateUpiDeepLink(upiId, payeeName, amount, note);

            // Create directory structure
            Path qrCodeDir = Paths.get(uploadPath, "qrcodes", "tenant-" + tenantId);
            Files.createDirectories(qrCodeDir);

            // Generate filename
            String filename = String.format("qr-order-%d-%d.png", orderId, System.currentTimeMillis());
            Path filePath = qrCodeDir.resolve(filename);

            // Generate QR code
            BitMatrix bitMatrix = createBitMatrix(upiUri);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

            log.info("Generated QR code for order {} at: {}", orderId, filePath);
            return filePath.toString();

        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    @Override
    public String generateUpiDeepLink(String upiId, String payeeName, BigDecimal amount, String note) {
        StringBuilder upiUri = new StringBuilder("upi://pay?");

        try {
            upiUri.append("pa=").append(URLEncoder.encode(upiId, StandardCharsets.UTF_8.toString()));
            upiUri.append("&pn=").append(URLEncoder.encode(payeeName, StandardCharsets.UTF_8.toString()));

            if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                upiUri.append("&am=").append(amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
            }

            upiUri.append("&cu=").append(CURRENCY);

            if (note != null && !note.isBlank()) {
                upiUri.append("&tn=").append(URLEncoder.encode(note, StandardCharsets.UTF_8.toString()));
            }

        } catch (UnsupportedEncodingException e) {
            log.error("Failed to encode UPI URI: {}", e.getMessage());
            throw new RuntimeException("Failed to encode UPI URI", e);
        }

        return upiUri.toString();
    }

    @Override
    public String generateQrCodeBase64(String upiId, String payeeName, BigDecimal amount, String note) {
        try {
            String upiUri = generateUpiDeepLink(upiId, payeeName, amount, note);
            BitMatrix bitMatrix = createBitMatrix(upiUri);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code Base64: {}", e.getMessage());
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    private BitMatrix createBitMatrix(String content) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);
    }
}
