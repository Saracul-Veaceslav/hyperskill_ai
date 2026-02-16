package com.example.hyperskill_ai.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Integration tests for QrController.
 * 
 * Tests all QR code generation endpoints with various parameter combinations
 * and validates proper error handling.
 */
@SpringBootTest
@AutoConfigureMockMvc
class QrControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    /**
     * Test Case 1: Health endpoint returns correct status and message
     */
    @Test
    fun `health endpoint should return service running message`() {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(content().string("Service is running"))
    }

    /**
     * Test Case 2: QR endpoint without parameters
     * Expected: Returns 500 with error message about empty contents
     */
    @Test
    fun `qr endpoint without parameters should return error for empty contents`() {
        mockMvc.perform(get("/qr"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string("Error generating QR code: Found empty contents"))
    }

    /**
     * Test Case 3: QR endpoint with valid contents parameter
     * Expected: Returns 200 with valid PNG image
     */
    @Test
    fun `qr endpoint with contents should return valid PNG image`() {
        val result = mockMvc.perform(get("/qr").param("contents", "TestData"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
            .andReturn()

        // Verify it's a valid PNG image by attempting to read it
        val imageBytes = result.response.contentAsByteArray
        val bufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
        assert(bufferedImage != null) { "Image should be readable" }
        assert(bufferedImage.width == 250) { "Default width should be 250" }
        assert(bufferedImage.height == 250) { "Default height should be 250" }
    }

    /**
     * Test Case 4: QR endpoint with custom size parameter
     * Expected: Returns 200 with image of specified size
     */
    @Test
    fun `qr endpoint with custom size should return image with correct dimensions`() {
        val result = mockMvc.perform(
            get("/qr")
                .param("contents", "CustomSize")
                .param("size", "500")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
            .andReturn()

        val imageBytes = result.response.contentAsByteArray
        val bufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
        assert(bufferedImage.width == 500) { "Width should be 500" }
        assert(bufferedImage.height == 500) { "Height should be 500" }
    }

    /**
     * Test Case 5: QR endpoint with JPEG type parameter
     * Expected: Returns 200 with valid JPEG image
     */
    @Test
    fun `qr endpoint with jpeg type should return JPEG image`() {
        val result = mockMvc.perform(
            get("/qr")
                .param("contents", "JpegTest")
                .param("type", "jpeg")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
            .andReturn()

        // Verify it's a valid JPEG image
        val imageBytes = result.response.contentAsByteArray
        val bufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
        assert(bufferedImage != null) { "JPEG image should be readable" }
    }

    /**
     * Test Case 6: QR endpoint with all parameters
     * Expected: Returns 200 with customized JPEG image
     */
    @Test
    fun `qr endpoint with all parameters should return customized image`() {
        val result = mockMvc.perform(
            get("/qr")
                .param("contents", "FullTest")
                .param("size", "300")
                .param("type", "jpeg")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
            .andReturn()

        val imageBytes = result.response.contentAsByteArray
        val bufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
        assert(bufferedImage.width == 300) { "Width should be 300" }
        assert(bufferedImage.height == 300) { "Height should be 300" }
    }

    /**
     * Test Case 7: QR endpoint with size below minimum (< 100)
     * Expected: Returns 400 Bad Request with error message
     */
    @Test
    fun `qr endpoint with size below minimum should return bad request`() {
        mockMvc.perform(
            get("/qr")
                .param("contents", "Test")
                .param("size", "50")
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string("Size must be between 100 and 1000 pixels"))
    }

    /**
     * Test Case 8: QR endpoint with size above maximum (> 1000)
     * Expected: Returns 400 Bad Request with error message
     */
    @Test
    fun `qr endpoint with size above maximum should return bad request`() {
        mockMvc.perform(
            get("/qr")
                .param("contents", "Test")
                .param("size", "1500")
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string("Size must be between 100 and 1000 pixels"))
    }

    /**
     * Test Case 9: QR endpoint with invalid type parameter
     * Expected: Returns 400 Bad Request with error message
     */
    @Test
    fun `qr endpoint with invalid type should return bad request`() {
        mockMvc.perform(
            get("/qr")
                .param("contents", "Test")
                .param("type", "gif")
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string("Type must be 'png' or 'jpeg'"))
    }

    /**
     * Test Case 10: QR endpoint with minimum valid size (100)
     * Expected: Returns 200 with 100x100 image
     */
    @Test
    fun `qr endpoint with minimum size should work correctly`() {
        val result = mockMvc.perform(
            get("/qr")
                .param("contents", "MinSize")
                .param("size", "100")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
            .andReturn()

        val imageBytes = result.response.contentAsByteArray
        val bufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
        assert(bufferedImage.width == 100) { "Width should be 100" }
        assert(bufferedImage.height == 100) { "Height should be 100" }
    }

    /**
     * Test Case 11: QR endpoint with maximum valid size (1000)
     * Expected: Returns 200 with 1000x1000 image
     */
    @Test
    fun `qr endpoint with maximum size should work correctly`() {
        val result = mockMvc.perform(
            get("/qr")
                .param("contents", "MaxSize")
                .param("size", "1000")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
            .andReturn()

        val imageBytes = result.response.contentAsByteArray
        val bufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
        assert(bufferedImage.width == 1000) { "Width should be 1000" }
        assert(bufferedImage.height == 1000) { "Height should be 1000" }
    }

    /**
     * Test Case 12: QR endpoint with complex contents
     * Expected: Returns 200 with QR code containing the complex string
     */
    @Test
    fun `qr endpoint with complex contents should generate valid QR code`() {
        val complexContents = "https://example.com/path?param1=value1&param2=value2"
        val result = mockMvc.perform(
            get("/qr")
                .param("contents", complexContents)
                .param("size", "250")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
            .andReturn()

        val imageBytes = result.response.contentAsByteArray
        val bufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
        assert(bufferedImage != null) { "Image should be readable" }
    }

    /**
     * Test Case 13: QR endpoint with type parameter case insensitivity
     * Expected: Returns 200 with JPEG image (PNG uppercase should work)
     */
    @Test
    fun `qr endpoint should handle type parameter case insensitively`() {
        mockMvc.perform(
            get("/qr")
                .param("contents", "CaseTest")
                .param("type", "PNG")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
    }

    /**
     * Test Case 14: QR endpoint with special characters in contents
     * Expected: Returns 200 with valid QR code
     */
    @Test
    fun `qr endpoint should handle special characters in contents`() {
        val specialContents = "Test!@#\$%^&*()_+-=[]{}|;':\",./<>?"
        val result = mockMvc.perform(
            get("/qr")
                .param("contents", specialContents)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
            .andReturn()

        val imageBytes = result.response.contentAsByteArray
        assert(imageBytes.isNotEmpty()) { "Image should not be empty" }
    }

    /**
     * Test Case 15: QR endpoint response should have appropriate content length
     * Expected: Content-Length header should be present and positive
     */
    @Test
    fun `qr endpoint should return appropriate content length header`() {
        mockMvc.perform(
            get("/qr")
                .param("contents", "LengthTest")
        )
            .andExpect(status().isOk)
            .andExpect(header().exists("Content-Length"))
            .andExpect { result ->
                val contentLength = result.response.getHeader("Content-Length")?.toLongOrNull()
                assert(contentLength != null && contentLength > 0) {
                    "Content-Length should be positive"
                }
            }
    }

    // ==================== /qr/save Endpoint Tests ====================

    /**
     * Test Case 16: Save QR code with valid contents
     * Expected: Returns 200 with success message containing file path
     */
    @Test
    fun `qr save endpoint should save QR code to disk`() {
        val result = mockMvc.perform(
            get("/qr/save")
                .param("contents", "SaveTest")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andReturn()

        val responseBody = result.response.contentAsString
        assert(responseBody.contains("QR code saved successfully to:")) {
            "Response should confirm successful save"
        }
        assert(responseBody.contains("/tmp/qr/")) {
            "Response should contain save directory path"
        }
        assert(responseBody.contains("qr_")) {
            "Response should contain filename prefix"
        }
        assert(responseBody.contains(".png")) {
            "Default format should be PNG"
        }

        // Extract file path and verify file exists
        val filePath = responseBody.substringAfter("to: ").trim()
        val file = File(filePath)
        assert(file.exists()) { "File should exist on disk: $filePath" }
        assert(file.length() > 0) { "File should not be empty" }

        // Cleanup
        file.delete()
    }

    /**
     * Test Case 17: Save QR code without contents parameter
     * Expected: Returns 500 with error about empty contents
     */
    @Test
    fun `qr save endpoint without contents should return error`() {
        mockMvc.perform(get("/qr/save"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error saving QR code")))
    }

    /**
     * Test Case 18: Save QR code with custom size
     * Expected: Saves image with specified dimensions
     */
    @Test
    fun `qr save endpoint with custom size should save correct dimensions`() {
        val result = mockMvc.perform(
            get("/qr/save")
                .param("contents", "CustomSizeSave")
                .param("size", "500")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andReturn()

        // Extract file path
        val responseBody = result.response.contentAsString
        val filePath = responseBody.substringAfter("to: ").trim()
        val file = File(filePath)

        // Verify file exists and read image
        assert(file.exists()) { "File should exist" }
        val savedImage = ImageIO.read(file)
        assert(savedImage.width == 500) { "Saved image width should be 500" }
        assert(savedImage.height == 500) { "Saved image height should be 500" }

        // Cleanup
        file.delete()
    }

    /**
     * Test Case 19: Save QR code as JPEG format
     * Expected: Saves JPEG file with correct extension
     */
    @Test
    fun `qr save endpoint with jpeg type should save as jpeg`() {
        val result = mockMvc.perform(
            get("/qr/save")
                .param("contents", "JpegSaveTest")
                .param("type", "jpeg")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andReturn()

        val responseBody = result.response.contentAsString
        assert(responseBody.contains(".jpeg")) {
            "Response should indicate JPEG file"
        }

        // Extract and verify file
        val filePath = responseBody.substringAfter("to: ").trim()
        val file = File(filePath)
        assert(file.exists()) { "JPEG file should exist" }
        assert(file.name.endsWith(".jpeg")) { "File should have .jpeg extension" }

        // Cleanup
        file.delete()
    }

    /**
     * Test Case 20: Save QR code with all parameters
     * Expected: Saves customized image with all specifications
     */
    @Test
    fun `qr save endpoint with all parameters should save customized image`() {
        val result = mockMvc.perform(
            get("/qr/save")
                .param("contents", "FullSaveTest")
                .param("size", "400")
                .param("type", "jpeg")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andReturn()

        val responseBody = result.response.contentAsString
        val filePath = responseBody.substringAfter("to: ").trim()
        val file = File(filePath)

        // Verify file properties
        assert(file.exists()) { "File should exist" }
        assert(file.name.endsWith(".jpeg")) { "Should be JPEG" }

        val savedImage = ImageIO.read(file)
        assert(savedImage.width == 400) { "Width should be 400" }
        assert(savedImage.height == 400) { "Height should be 400" }

        // Cleanup
        file.delete()
    }

    /**
     * Test Case 21: Save endpoint with invalid size
     * Expected: Returns 400 Bad Request without creating file
     */
    @Test
    fun `qr save endpoint with invalid size should return error without saving`() {
        val filesBefore = File("/tmp/qr/").listFiles()?.size ?: 0

        mockMvc.perform(
            get("/qr/save")
                .param("contents", "Test")
                .param("size", "50")
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string("Size must be between 100 and 1000 pixels"))

        // Verify no new files created
        val filesAfter = File("/tmp/qr/").listFiles()?.size ?: 0
        assert(filesAfter == filesBefore) {
            "No files should be created for invalid request"
        }
    }

    /**
     * Test Case 22: Save endpoint with invalid type
     * Expected: Returns 400 Bad Request without creating file
     */
    @Test
    fun `qr save endpoint with invalid type should return error without saving`() {
        mockMvc.perform(
            get("/qr/save")
                .param("contents", "Test")
                .param("type", "gif")
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(content().string("Type must be 'png' or 'jpeg'"))
    }

    /**
     * Test Case 23: Verify save directory is created automatically
     * Expected: Directory /tmp/qr/ should be created if it doesn't exist
     */
    @Test
    fun `qr save endpoint should create directory if not exists`() {
        // Delete directory if exists (for clean test)
        val saveDir = File("/tmp/qr/")
        if (saveDir.exists()) {
            saveDir.listFiles()?.forEach { it.delete() }
            saveDir.delete()
        }

        // Make request
        val result = mockMvc.perform(
            get("/qr/save")
                .param("contents", "DirectoryTest")
        )
            .andExpect(status().isOk)
            .andReturn()

        // Verify directory was created
        assert(saveDir.exists()) { "Save directory should be created" }
        assert(saveDir.isDirectory) { "Save path should be a directory" }

        // Verify file was saved
        val responseBody = result.response.contentAsString
        val filePath = responseBody.substringAfter("to: ").trim()
        val file = File(filePath)
        assert(file.exists()) { "File should be saved" }

        // Cleanup
        file.delete()
    }

    /**
     * Test Case 24: Verify filename format with timestamp
     * Expected: Filename should match pattern qr_YYYYMMDD_HHMMSS.ext
     */
    @Test
    fun `qr save endpoint should generate filename with timestamp`() {
        val result = mockMvc.perform(
            get("/qr/save")
                .param("contents", "TimestampTest")
        )
            .andExpect(status().isOk)
            .andReturn()

        val responseBody = result.response.contentAsString
        val filePath = responseBody.substringAfter("to: ").trim()
        val file = File(filePath)

        // Verify filename format: qr_YYYYMMDD_HHMMSS.png
        val filename = file.name
        assert(filename.matches(Regex("qr_\\d{8}_\\d{6}\\.png"))) {
            "Filename should match pattern qr_YYYYMMDD_HHMMSS.png, got: $filename"
        }

        // Cleanup
        file.delete()
    }

    /**
     * Test Case 25: Verify multiple saves create unique filenames
     * Expected: Each save should create a new file with different timestamp
     */
    @Test
    fun `qr save endpoint should create unique filenames for multiple saves`() {
        // First save
        val result1 = mockMvc.perform(
            get("/qr/save")
                .param("contents", "First")
        )
            .andExpect(status().isOk)
            .andReturn()

        Thread.sleep(1000) // Wait 1 second to ensure different timestamp

        // Second save
        val result2 = mockMvc.perform(
            get("/qr/save")
                .param("contents", "Second")
        )
            .andExpect(status().isOk)
            .andReturn()

        val filePath1 = result1.response.contentAsString.substringAfter("to: ").trim()
        val filePath2 = result2.response.contentAsString.substringAfter("to: ").trim()

        assert(filePath1 != filePath2) {
            "Files should have different paths/names"
        }

        val file1 = File(filePath1)
        val file2 = File(filePath2)
        assert(file1.exists()) { "First file should exist" }
        assert(file2.exists()) { "Second file should exist" }

        // Cleanup
        file1.delete()
        file2.delete()
    }

    /**
     * Test Case 26: Save endpoint preserves original /qr endpoint functionality
     * Expected: Both endpoints should work independently
     */
    @Test
    fun `qr save endpoint should not affect regular qr endpoint`() {
        // Test regular endpoint still works
        val regularResult = mockMvc.perform(
            get("/qr")
                .param("contents", "RegularTest")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
            .andReturn()

        val imageBytes = regularResult.response.contentAsByteArray
        assert(imageBytes.isNotEmpty()) { "Regular endpoint should return image" }

        // Test save endpoint works
        val saveResult = mockMvc.perform(
            get("/qr/save")
                .param("contents", "SaveTest")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andReturn()

        val responseBody = saveResult.response.contentAsString
        assert(responseBody.contains("saved successfully")) {
            "Save endpoint should return success message"
        }

        // Cleanup
        val filePath = responseBody.substringAfter("to: ").trim()
        File(filePath).delete()
    }
}
