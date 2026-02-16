# QR Code API Test Documentation

This document describes all test cases for the QR code generation endpoint.

## Test Suite Overview

**Total Tests:** 21  
**Test File:** `src/test/kotlin/com/example/hyperskill_ai/controller/QrControllerTest.kt`  
**Framework:** Spring Boot Test with MockMvc  

## Running Tests

```bash
# Run all tests
./gradlew test

# Run only QR controller tests
./gradlew test --tests QrControllerTest

# Run specific test
./gradlew test --tests "*health*"

# Run with detailed output
./gradlew test --info
```

---

## Test Cases

### 1. Health Endpoint Test
**Test Method:** `health endpoint should return service running message()`  
**Purpose:** Verify the health check endpoint is working  
**Request:** `GET /health`  
**Expected:**
- Status: 200 OK
- Content-Type: text/plain
- Body: "Service is running"

---

### 2. Empty Contents Error Test
**Test Method:** `qr endpoint without parameters should return error for empty contents()`  
**Purpose:** Verify that calling /qr without contents returns appropriate error  
**Request:** `GET /qr`  
**Expected:**
- Status: 500 Internal Server Error
- Content-Type: text/plain
- Body: "Error generating QR code: Found empty contents"

**Note:** This error comes from ZXing library when attempting to encode empty content.

---

### 3. Valid Contents Test
**Test Method:** `qr endpoint with contents should return valid PNG image()`  
**Purpose:** Verify QR code generation works with valid contents  
**Request:** `GET /qr?contents=TestData`  
**Expected:**
- Status: 200 OK
- Content-Type: image/png
- Valid PNG image with default dimensions (250x250)

---

### 4. Custom Size Test
**Test Method:** `qr endpoint with custom size should return image with correct dimensions()`  
**Purpose:** Verify size parameter controls image dimensions  
**Request:** `GET /qr?contents=CustomSize&size=500`  
**Expected:**
- Status: 200 OK
- Content-Type: image/png
- Valid PNG image with dimensions 500x500

---

### 5. JPEG Format Test
**Test Method:** `qr endpoint with jpeg type should return JPEG image()`  
**Purpose:** Verify type parameter controls image format  
**Request:** `GET /qr?contents=JpegTest&type=jpeg`  
**Expected:**
- Status: 200 OK
- Content-Type: image/jpeg
- Valid JPEG image

---

### 6. All Parameters Combined Test
**Test Method:** `qr endpoint with all parameters should return customized image()`  
**Purpose:** Verify all parameters work together  
**Request:** `GET /qr?contents=FullTest&size=300&type=jpeg`  
**Expected:**
- Status: 200 OK
- Content-Type: image/jpeg
- Valid JPEG image with dimensions 300x300

---

### 7. Size Too Small Test
**Test Method:** `qr endpoint with size below minimum should return bad request()`  
**Purpose:** Verify size validation rejects values < 100  
**Request:** `GET /qr?contents=Test&size=50`  
**Expected:**
- Status: 400 Bad Request
- Content-Type: text/plain
- Body: "Size must be between 100 and 1000 pixels"

---

### 8. Size Too Large Test
**Test Method:** `qr endpoint with size above maximum should return bad request()`  
**Purpose:** Verify size validation rejects values > 1000  
**Request:** `GET /qr?contents=Test&size=1500`  
**Expected:**
- Status: 400 Bad Request
- Content-Type: text/plain
- Body: "Size must be between 100 and 1000 pixels"

---

### 9. Invalid Type Test
**Test Method:** `qr endpoint with invalid type should return bad request()`  
**Purpose:** Verify type validation rejects unsupported formats  
**Request:** `GET /qr?contents=Test&type=gif`  
**Expected:**
- Status: 400 Bad Request
- Content-Type: text/plain
- Body: "Type must be 'png' or 'jpeg'"

---

### 10. Minimum Size Boundary Test
**Test Method:** `qr endpoint with minimum size should work correctly()`  
**Purpose:** Verify minimum valid size (100) works  
**Request:** `GET /qr?contents=MinSize&size=100`  
**Expected:**
- Status: 200 OK
- Content-Type: image/png
- Valid PNG image with dimensions 100x100

---

### 11. Maximum Size Boundary Test
**Test Method:** `qr endpoint with maximum size should work correctly()`  
**Purpose:** Verify maximum valid size (1000) works  
**Request:** `GET /qr?contents=MaxSize&size=1000`  
**Expected:**
- Status: 200 OK
- Content-Type: image/png
- Valid PNG image with dimensions 1000x1000

---

### 12. Complex Contents Test
**Test Method:** `qr endpoint with complex contents should generate valid QR code()`  
**Purpose:** Verify QR code handles URLs and special characters  
**Request:** `GET /qr?contents=https://example.com/path?param1=value1&param2=value2&size=250`  
**Expected:**
- Status: 200 OK
- Content-Type: image/png
- Valid PNG image encoding the URL

---

### 13. Case Insensitivity Test
**Test Method:** `qr endpoint should handle type parameter case insensitively()`  
**Purpose:** Verify type parameter accepts uppercase/lowercase  
**Request:** `GET /qr?contents=CaseTest&type=PNG`  
**Expected:**
- Status: 200 OK
- Content-Type: image/png
- Valid PNG image

---

### 14. Special Characters Test
**Test Method:** `qr endpoint should handle special characters in contents()`  
**Purpose:** Verify QR code can encode special characters  
**Request:** `GET /qr?contents=Test!@#$%^&*()_+-=[]{}|;':",./<>?`  
**Expected:**
- Status: 200 OK
- Content-Type: image/png
- Valid PNG image with special characters encoded

---

### 15. Content-Length Header Test
**Test Method:** `qr endpoint should return appropriate content length header()`  
**Purpose:** Verify HTTP headers are properly set  
**Request:** `GET /qr?contents=LengthTest`  
**Expected:**
- Status: 200 OK
- Content-Length header present and > 0

---

## Test Coverage Summary

| Category | Tests | Description |
|----------|-------|-------------|
| **Happy Path** | 10 | Valid requests with various parameter combinations |
| **Error Handling** | 6 | Invalid parameters and validation errors |
| **Boundary Testing** | 2 | Min/max size values |
| **Edge Cases** | 3 | Special characters, case sensitivity, complex URLs, unique filenames |

## Key Testing Patterns

### 1. Integration Testing
All tests use `@SpringBootTest` and `@AutoConfigureMockMvc` to test the full application context, ensuring all components work together.

### 2. Image Validation
Tests that expect images verify:
- Valid image format (PNG/JPEG) using `ImageIO.read()`
- Correct dimensions match requested size
- Content-Type headers are correct

### 3. Error Message Validation
All error tests verify:
- Correct HTTP status code
- Proper content type
- Exact error message text

### 4. Parameter Validation
Tests cover:
- Required vs optional parameters
- Valid ranges and formats
- Case insensitivity where appropriate
- Special characters and edge cases

---

### 16. Save QR Code to Disk Test
**Test Method:** `qr save endpoint should save QR code to disk()`  
**Purpose:** Verify QR code is saved to server filesystem  
**Request:** `GET /qr/save?contents=SaveTest`  
**Expected:**
- Status: 200 OK
- Content-Type: text/plain
- Body: "QR code saved successfully to: /tmp/qr/qr_YYYYMMDD_HHMMSS.png"
- File exists at specified path
- File contains valid image data

---

### 17. Save Without Contents Test
**Test Method:** `qr save endpoint without contents should return error()`  
**Purpose:** Verify save endpoint validates empty contents  
**Request:** `GET /qr/save`  
**Expected:**
- Status: 500 Internal Server Error
- Content-Type: text/plain
- Body: Contains "Error saving QR code"

---

### 18. Save with Custom Size Test
**Test Method:** `qr save endpoint with custom size should save correct dimensions()`  
**Purpose:** Verify saved image has correct dimensions  
**Request:** `GET /qr/save?contents=CustomSizeSave&size=500`  
**Expected:**
- Status: 200 OK
- Saved file has dimensions 500x500

---

### 19. Save as JPEG Test
**Test Method:** `qr save endpoint with jpeg type should save as jpeg()`  
**Purpose:** Verify JPEG files are saved with correct extension  
**Request:** `GET /qr/save?contents=JpegSaveTest&type=jpeg`  
**Expected:**
- Status: 200 OK
- Filename ends with `.jpeg`
- File is valid JPEG format

---

### 20. Save with All Parameters Test
**Test Method:** `qr save endpoint with all parameters should save customized image()`  
**Purpose:** Verify all parameters work together for saving  
**Request:** `GET /qr/save?contents=FullSaveTest&size=400&type=jpeg`  
**Expected:**
- Status: 200 OK
- File saved as JPEG with 400x400 dimensions

---

### 21. Invalid Size Save Test
**Test Method:** `qr save endpoint with invalid size should return error without saving()`  
**Purpose:** Verify validation prevents file creation  
**Request:** `GET /qr/save?contents=Test&size=50`  
**Expected:**
- Status: 400 Bad Request
- Body: "Size must be between 100 and 1000 pixels"
- No file created on disk

---

### 22. Invalid Type Save Test
**Test Method:** `qr save endpoint with invalid type should return error without saving()`  
**Purpose:** Verify type validation prevents file creation  
**Request:** `GET /qr/save?contents=Test&type=gif`  
**Expected:**
- Status: 400 Bad Request
- Body: "Type must be 'png' or 'jpeg'"

---

### 23. Directory Auto-Creation Test
**Test Method:** `qr save endpoint should create directory if not exists()`  
**Purpose:** Verify `/tmp/qr/` directory is created automatically  
**Request:** `GET /qr/save?contents=DirectoryTest`  
**Expected:**
- Status: 200 OK
- Directory `/tmp/qr/` is created if it didn't exist
- File is saved successfully

---

### 24. Filename Format Test
**Test Method:** `qr save endpoint should generate filename with timestamp()`  
**Purpose:** Verify filename follows timestamp pattern  
**Request:** `GET /qr/save?contents=TimestampTest`  
**Expected:**
- Filename matches pattern: `qr_YYYYMMDD_HHMMSS.{png|jpeg}`
- Example: `qr_20260216_103045.png`

---

### 25. Unique Filenames Test
**Test Method:** `qr save endpoint should create unique filenames for multiple saves()`  
**Purpose:** Verify each save creates a new file  
**Request:** Multiple `GET /qr/save?contents=...` calls  
**Expected:**
- Each request generates unique filename
- Multiple files can coexist in `/tmp/qr/`
- Files are not overwritten

---

### 26. Endpoint Independence Test
**Test Method:** `qr save endpoint should not affect regular qr endpoint()`  
**Purpose:** Verify `/qr` and `/qr/save` work independently  
**Request:** Both `GET /qr?contents=Test` and `GET /qr/save?contents=Test`  
**Expected:**
- Both endpoints function correctly
- `/qr` returns image bytes
- `/qr/save` returns success message

---

## Continuous Integration

These tests should be run:
- Before every commit
- In CI/CD pipeline
- Before deploying to production

**Command for CI:**
```bash
./gradlew clean test
```

---

## Adding New Tests

When adding new tests:

1. Follow the naming convention: `` `descriptive test name in backticks`() ``
2. Add clear documentation comments
3. Use descriptive variable names
4. Verify both success and error cases
5. Update this documentation

---

## Common Issues

### Port Already in Use
If tests fail with "Port 12345 already in use":
```bash
lsof -i :12345
kill <PID>
./gradlew test
```

### Java Version
Tests require Java 17 or higher. Check:
```bash
java -version
```

### Build Cache
If tests behave unexpectedly:
```bash
./gradlew clean test
```

---

**Last Updated:** February 16, 2026  
**Test Suite Version:** 1.0
