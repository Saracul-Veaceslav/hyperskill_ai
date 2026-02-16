package com.example.hyperskill_ai.controller

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import javax.imageio.ImageIO

/**
 * Controller for QR-related endpoints.
 * 
 * Provides health check and QR code generation functionality.
 */
@RestController
class QrController {

    companion object {
        private const val MIN_SIZE = 100
        private const val MAX_SIZE = 1000
        private const val DEFAULT_SIZE = 250
        private const val DEFAULT_TYPE = "png"
        private val ALLOWED_TYPES = setOf("png", "jpeg")
        private const val SAVE_DIRECTORY = "/tmp/qr/"
    }

    /**
     * Health check endpoint.
     * 
     * @return Plain text response indicating the service is running
     */
    @GetMapping("/health", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun health(): ResponseEntity<String> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body("Service is running")
    }

    /**
     * QR code generation endpoint.
     * 
     * Generates a QR code image with customizable parameters.
     * 
     * @param size Optional size parameter (100-1000). Defines width and height in pixels
     * @param type Optional image format ("png" or "jpeg")
     * @param contents Optional string to encode in the QR code
     * @return ResponseEntity containing the QR code image as byte array
     */
    @GetMapping("/qr")
    fun generateQrImage(
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) contents: String?
    ): ResponseEntity<*> {
        
        // Validate size parameter
        val imageSize = size ?: DEFAULT_SIZE
        if (imageSize < MIN_SIZE || imageSize > MAX_SIZE) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Size must be between $MIN_SIZE and $MAX_SIZE pixels")
        }

        // Validate type parameter
        val imageType = (type ?: DEFAULT_TYPE).lowercase()
        if (imageType !in ALLOWED_TYPES) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Type must be 'png' or 'jpeg'")
        }

        // Get contents parameter (empty string if not provided, ZXing will handle validation)
        val qrContents = contents ?: ""

        try {
            // Generate QR code using ZXing
            val qrCodeImage = generateQrCodeImage(qrContents, imageSize)

            // Convert BufferedImage to byte array
            val outputStream = ByteArrayOutputStream()
            ImageIO.write(qrCodeImage, imageType, outputStream)
            val imageBytes = outputStream.toByteArray()

            // Set appropriate content type
            val headers = HttpHeaders()
            headers.contentType = when (imageType) {
                "jpeg" -> MediaType.IMAGE_JPEG
                else -> MediaType.IMAGE_PNG
            }

            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(imageBytes)
                
        } catch (e: Exception) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Error generating QR code: ${e.message}")
        }
    }

    /**
     * Generates a QR code BufferedImage using ZXing library.
     * 
     * @param contents The string to encode in the QR code
     * @param size The width and height of the QR code in pixels
     * @return BufferedImage containing the QR code
     */
    private fun generateQrCodeImage(contents: String, size: Int): BufferedImage {
        // Configure QR code generation hints
        val hints = hashMapOf<EncodeHintType, Any>(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
            EncodeHintType.MARGIN to 1
        )

        // Generate QR code matrix
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(
            contents,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )

        // Create BufferedImage from bit matrix
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        
        for (x in 0 until size) {
            for (y in 0 until size) {
                val color = if (bitMatrix.get(x, y)) Color.BLACK.rgb else Color.WHITE.rgb
                image.setRGB(x, y, color)
            }
        }

        return image
    }

    /**
     * QR code generation and save endpoint.
     * 
     * Generates a QR code image and saves it to the server filesystem.
     * 
     * @param size Optional size parameter (100-1000). Defines width and height in pixels
     * @param type Optional image format ("png" or "jpeg")
     * @param contents Optional string to encode in the QR code
     * @return ResponseEntity containing success message with file path
     */
    @GetMapping("/qr/save")
    fun saveQrImage(
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) contents: String?
    ): ResponseEntity<String> {
        
        // Validate size parameter
        val imageSize = size ?: DEFAULT_SIZE
        if (imageSize < MIN_SIZE || imageSize > MAX_SIZE) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Size must be between $MIN_SIZE and $MAX_SIZE pixels")
        }

        // Validate type parameter
        val imageType = (type ?: DEFAULT_TYPE).lowercase()
        if (imageType !in ALLOWED_TYPES) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Type must be 'png' or 'jpeg'")
        }

        // Get contents parameter (empty string if not provided, ZXing will handle validation)
        val qrContents = contents ?: ""

        try {
            // Generate QR code using ZXing
            val qrCodeImage = generateQrCodeImage(qrContents, imageSize)

            // Create save directory if it doesn't exist
            val saveDir = File(SAVE_DIRECTORY)
            if (!saveDir.exists()) {
                saveDir.mkdirs()
            }

            // Generate filename based on timestamp
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
            val timestamp = dateFormat.format(Date())
            val filename = "qr_${timestamp}.${imageType}"
            val filePath = File(SAVE_DIRECTORY, filename)

            // Save the image to disk
            ImageIO.write(qrCodeImage, imageType, filePath)

            // Return success message with file path
            return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.TEXT_PLAIN)
                .body("QR code saved successfully to: ${filePath.absolutePath}")
                
        } catch (e: Exception) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Error saving QR code: ${e.message}")
        }
    }
}
