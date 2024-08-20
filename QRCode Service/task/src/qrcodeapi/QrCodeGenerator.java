package qrcodeapi;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * The {@code QrCodeGenerator} class provides functionality for generating QR code images
 * and validating input parameters related to QR code generation.
 */
class QrCodeGenerator {

    /**
     * Generates a QR code image from the given content and returns it as a {@link BufferedImage}.
     *
     * @param contents the string data to encode in the QR code. This can be any text or data that you want to represent
     *                 in the QR code, such as URLs, plain text, etc.
     * @param size the size (width and height in pixels) of the resulting QR code image. The image will be a square
     *             with both dimensions equal to this size.
     * @param correction the error correction level of the QR code. The valid characters are 'L', 'M', 'Q', and 'H',
     *                   representing different levels of error correction.
     * @return a {@link BufferedImage} representing the generated QR code.
     * @throws WriterException if an error occurs during the encoding process of the QR code.
     * @throws IllegalArgumentException if the provided correction level is invalid.
     */
    public BufferedImage generateQrCode(String contents, int size, char correction) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        // Map the correction char to the corresponding ErrorCorrectionLevel
        ErrorCorrectionLevel errorCorrectionLevel = switch (Character.toUpperCase(correction)) {
            case 'L' -> ErrorCorrectionLevel.L;
            case 'M' -> ErrorCorrectionLevel.M;
            case 'Q' -> ErrorCorrectionLevel.Q;
            case 'H' -> ErrorCorrectionLevel.H;
            default -> throw new IllegalArgumentException("Invalid correction level: " + correction);
        };
        Map<EncodeHintType, ?> hints = Map.of(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        BitMatrix bitMatrix = qrCodeWriter.encode(contents, BarcodeFormat.QR_CODE, size, size, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Validates the image type provided to the API.
     *
     * @param type the image format of the QR code (e.g., png, jpeg, gif).
     * @return {@code true} if the image type is valid; {@code false} otherwise.
     */
    public boolean isValidType (String type) {
        return type.equalsIgnoreCase("png") ||
                type.equalsIgnoreCase("jpeg") ||
                type.equalsIgnoreCase("gif");
    }

    /**
     * Validates the contents provided to the API for QR code generation.
     *
     * @param contents the string data to encode in the QR code.
     * @return {@code true} if the contents are neither {@code null} nor blank; {@code false} otherwise.
     */
    public boolean isValidContents (String contents) {
        return contents != null && !contents.isEmpty() && !contents.isBlank();
    }

    /**
     * Validates the size provided to the API for QR code generation.
     *
     * @param size the size (in pixels) of the resulting QR code image.
     * @return {@code true} if the size is between 150 and 350 pixels; {@code false} otherwise.
     */
    public boolean isValidSize(int size) {
        return !(size < 150 || size > 350);
    }

    /**
     * Validates the error correction level provided to the API.
     *
     * @param correction the character representing the error correction level ('L', 'M', 'Q', 'H').
     * @return {@code true} if the correction level is one of the valid characters; {@code false} otherwise.
     */
    public boolean isValidCorrection(char correction) {
        return Character.toUpperCase(correction) == 'L' ||
                Character.toUpperCase(correction) == 'M' ||
                Character.toUpperCase(correction) == 'Q' ||
                Character.toUpperCase(correction) == 'H';
    }
}
