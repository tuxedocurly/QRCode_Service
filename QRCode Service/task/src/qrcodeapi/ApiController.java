package qrcodeapi;

import com.google.zxing.WriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * This class is the REST API controller for handling QR code generation requests.
 * It provides endpoints for checking the health of the API and generating QR codes
 * with specified parameters.
 */
@RestController
public class ApiController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    private static final String ERROR_IMAGE_TYPE = "Only png, jpeg and gif image types are supported";
    private static final String ERROR_IMAGE_SIZE = "Image size must be between 150 and 350 pixels";
    private static final String ERROR_IMAGE_CONTENTS = "Contents cannot be null or blank";
    private static final String ERROR_IMAGE_CORRECTION = "Permitted error correction levels are L, M, Q, H";

    private final QrCodeGenerator qrCodeGenerator = new QrCodeGenerator();

    /**
     * Endpoint to check if the API is up and running.
     *
     * @return a {@link ResponseEntity} with {@link HttpStatus#OK} to indicate that the API is healthy.
     */
    @GetMapping("/api/health")
    public ResponseEntity<HttpStatus> getHealth() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Endpoint to generate a QR code image based on the provided parameters.
     *
     * @param contents   the string data to encode in the QR code. If not provided, an error response is returned.
     * @param size       the size (in pixels) of the generated QR code. Defaults to 250 if not provided.
     *                   The value must be between 150 and 350 pixels.
     * @param type       the image format of the QR code (e.g., png, jpeg, gif). Defaults to "png" if not provided.
     *                   Only png, jpeg, and gif types are supported.
     * @param correction the error correction level of the QR code ('L', 'M', 'Q', or 'H'). Defaults to 'L' if not provided.
     * @return a {@link ResponseEntity} containing the generated QR code image in the specified format,
     * or an error message if validation fails or an exception occurs during generation.
     */
    @GetMapping("/api/qrcode")
    public ResponseEntity<?> getImage(
            @RequestParam(required = false) String contents,
            @RequestParam(defaultValue = "250") int size,
            @RequestParam(defaultValue = "png") String type,
            @RequestParam(defaultValue = "L") char correction) {

        // begin: validate image contents, size, correction, and type
        if (!qrCodeGenerator.isValidContents(contents)) {
            return createErrorResponse(ERROR_IMAGE_CONTENTS);
        }

        if (!qrCodeGenerator.isValidSize(size)) {
            return createErrorResponse(ERROR_IMAGE_SIZE);
        }
        if (!qrCodeGenerator.isValidCorrection(correction)) {
            return createErrorResponse(ERROR_IMAGE_CORRECTION);
        }

        if (!qrCodeGenerator.isValidType(type)) {
            return createErrorResponse(ERROR_IMAGE_TYPE);
        }

        // generate the QR code
        try {
            BufferedImage qrImage = qrCodeGenerator.generateQrCode(contents, size, correction);

            // convert BufferedImage into byte array
            byte[] imageBytes;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(qrImage, type, baos);
                imageBytes = baos.toByteArray();
            }

            // Return the image with appropriate content
            return switch (type.toLowerCase()) {
                case "jpeg" -> ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
                case "png" -> ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes);
                case "gif" -> ResponseEntity.ok().contentType(MediaType.IMAGE_GIF).body(imageBytes);
                default -> createErrorResponse(ERROR_IMAGE_TYPE);
            };

        } catch (WriterException | IOException e) {
            logger.error("An error occurred while generating the QR code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while generating the QR code");
        }
    }

    /**
     * Creates a {@link ResponseEntity} containing an error message.
     *
     * @param errorMessage the error message to be included in the response.
     * @return a {@link ResponseEntity} with a status of {@link HttpStatus#BAD_REQUEST} and the error message.
     */
    private ResponseEntity<Map<String, String>> createErrorResponse(String errorMessage) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}


