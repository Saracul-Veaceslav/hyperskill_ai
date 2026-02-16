# Hyperskill AI - Spring Boot Web Application

A Spring Boot web application built with Kotlin that provides health check and QR code generation functionality.

## ✨ Features

- 🏥 **Health Check Endpoint** - Monitor service status
- 📱 **QR Code Generator** - Create QR codes with customizable size, format, and content
- 🎨 **Multiple Formats** - Support for PNG and JPEG images
- 🔒 **Input Validation** - Proper error handling and validation
- 🛠️ **Built with ZXing** - Industry-standard QR code library

---

## 📋 Table of Contents
- [Quick Start](#-quick-start)
- [Building the Application](#-building-the-application)
- [Running the Application](#-running-the-application)
- [Testing Endpoints](#-testing-endpoints)
- [Process Management](#-process-management-important)
- [Understanding Background Processes](#-understanding-background-processes)
- [Troubleshooting](#-troubleshooting)
- [Project Structure](#-project-structure)

---

## 🚀 Quick Start

**Start the application:**
```bash
./gradlew bootRun
```

**Test it works (in a new terminal):**
```bash
curl http://localhost:12345/health
```

**Stop the application:**
Press `Ctrl+C` in the terminal where it's running, or use:
```bash
lsof -i :12345
kill <PID>
```

---

## 🔨 Building the Application

### Build the project
```bash
./gradlew build
```
This compiles the code, runs tests, and creates a JAR file.

### Build without tests (faster)
```bash
./gradlew build -x test
```

### Clean and rebuild
```bash
./gradlew clean build
```
This removes old build files and rebuilds everything from scratch.

### View available Gradle tasks
```bash
./gradlew tasks
```

---

## ▶️ Running the Application

### Method 1: Using Gradle (Recommended for development)
```bash
./gradlew bootRun
```
- The application starts on **port 12345**
- You'll see logs in the terminal
- Press `Ctrl+C` to stop

### Method 2: Using the JAR file (for production)
```bash
# First, build the JAR
./gradlew build

# Then run it
java -jar build/libs/hyperskill_ai-0.0.1-SNAPSHOT.jar
```

### Run in the background (NOT recommended - see warning below)
```bash
./gradlew bootRun &
```
⚠️ **WARNING:** This runs the app in the background and can consume resources even after you close the terminal. See [Process Management](#-process-management-important) below.

---

## 🧪 Testing Endpoints

Once the application is running, open a **new terminal** and try these commands:

### 1. Health Check Endpoint
Returns plain text confirming the service is running.

```bash
curl http://localhost:12345/health
```
**Expected response:**
```
Service is running
```

### 2. QR Code Generation Endpoint
Generates QR code images with customizable parameters using the ZXing library.

**Parameters:**
- `size` (optional): Image dimensions in pixels (100-1000). Default: 250
- `type` (optional): Image format - "png" or "jpeg". Default: "png"
- `contents` (optional): Text to encode in the QR code. Default: empty

**Basic usage (default 250x250 PNG):**
```bash
curl http://localhost:12345/qr -o qr_image.png
```

**Custom size (500x500):**
```bash
curl "http://localhost:12345/qr?size=500" -o qr_large.png
```

**JPEG format:**
```bash
curl "http://localhost:12345/qr?type=jpeg" -o qr_image.jpeg
```

**With content:**
```bash
curl "http://localhost:12345/qr?contents=Hello%20World" -o qr_hello.png
```

**Complex example (all parameters):**
```bash
curl "http://localhost:12345/qr?size=300&type=jpeg&contents=https://example.com" -o qr_url.jpeg
```

**Open the image (macOS):**
```bash
open qr_image.png
```

**Error handling:**
- Invalid size (not 100-1000): Returns 400 Bad Request
- Invalid type (not png/jpeg): Returns 400 Bad Request
- Empty contents: Returns 500 with "Error generating QR code: Found empty contents"

### 3. QR Code Save to File Endpoint
Generates QR code images and saves them to the server filesystem at `/tmp/qr/`.

**Endpoint:** `GET /qr/save`

**Parameters:** Same as `/qr` endpoint (size, type, contents)

**Response:** Plain text message with the saved file path

**Basic usage:**
```bash
curl "http://localhost:12345/qr/save?contents=Hello"
```

**Response:**
```
QR code saved successfully to: /tmp/qr/qr_20260216_103045.png
```

**With custom parameters:**
```bash
curl "http://localhost:12345/qr/save?size=500&type=jpeg&contents=SavedQR"
```

**Key Features:**
- Automatically creates `/tmp/qr/` directory if it doesn't exist
- Generates unique filenames using timestamp format: `qr_YYYYMMDD_HHMMSS.ext`
- Saves actual image file to server disk
- Returns confirmation message with full file path

**Error handling:**
- Same validation as `/qr` endpoint
- Returns error if file save operation fails

---

## 🛑 Process Management (IMPORTANT!)

This section teaches you how to check what's running and how to stop processes to save computer resources.

### Step 1: Check if your application is running

**Check by port number:**
```bash
lsof -i :12345
```
- If nothing appears, the app is **NOT** running
- If you see output with PID (Process ID), the app **IS** running

**Example output when running:**
```
COMMAND   PID     USER   FD   TYPE   DEVICE SIZE/OFF NODE NAME
java    35789 vaceslav   40u  IPv6   0x...        0t0  TCP *:italk (LISTEN)
```
The important number here is **35789** - that's the PID (Process ID).

**Check for Java/Gradle processes:**
```bash
ps aux | grep -i "gradlew\|spring\|hyperskill" | grep -v grep
```

### Step 2: Kill (stop) the process

**Option 1: Using the PID from lsof**
```bash
# First, find the PID
lsof -i :12345

# Then kill it (replace 35789 with your actual PID)
kill 35789
```

**Option 2: Kill all Java processes (use with caution!)**
```bash
pkill -f "HyperskillAiApplicationKt"
```
⚠️ This only kills your Spring Boot app, not other Java programs.

**Option 3: Force kill (if normal kill doesn't work)**
```bash
kill -9 <PID>
```
⚠️ Use `-9` only as a last resort - it forcefully terminates the process.

### Step 3: Verify the process is stopped
```bash
lsof -i :12345
```
If you see no output, the app has been stopped successfully. ✅

---

## 🔍 Understanding Background Processes

### What are background processes?
Background processes run "behind the scenes" without showing you their output. They continue running even if you close the terminal window.

### How to see ALL your running processes
```bash
ps aux
```
This shows every process running on your computer.

### How to find specific processes
```bash
# Find Java processes
ps aux | grep java

# Find Gradle processes
ps aux | grep gradle

# Find processes using a specific port
lsof -i :12345
```

### Understanding the output
```
USER       PID  %CPU %MEM      VSZ    RSS   TT  STAT STARTED      TIME COMMAND
vaceslav 35789   0.0  0.4 449752400 184368 ??  S     9:42PM   0:02.17 java ...
```

- **USER**: Who started the process (you!)
- **PID**: Process ID (unique number to identify the process)
- **%CPU**: How much CPU it's using (0.0 = very little)
- **%MEM**: How much memory it's using (0.4 = less than 1%)
- **COMMAND**: What program is running

### When should I kill a process?

✅ **Kill it if:**
- You're done using the application
- It's using too much CPU or memory
- You want to restart it with new changes
- You accidentally started multiple instances

❌ **DON'T kill if:**
- You're not sure what it is (Google it first!)
- It's a system process (belongs to 'root' user)
- You're actively using it

### Pro tip: Clean up before logging off
Before closing your laptop or ending work, run:
```bash
lsof -i :12345
```
If anything shows up, kill it to save battery and resources.

---

## 🐛 Troubleshooting

### Problem: Port 12345 is already in use
**Error message:**
```
Port 12345 is already in use
```

**Solution:**
```bash
# Find what's using the port
lsof -i :12345

# Kill it
kill <PID>

# Try starting your app again
./gradlew bootRun
```

### Problem: "Permission denied" when running gradlew
**Error message:**
```
permission denied: ./gradlew
```

**Solution:**
```bash
chmod +x gradlew
```

### Problem: JAVA_HOME not set
**Error message:**
```
ERROR: JAVA_HOME is not set
```

**Solution (macOS):**
```bash
export JAVA_HOME=$(/usr/libexec/java_home)
./gradlew bootRun
```

### Problem: How do I know if the app is running?
**Check 1: Look at port**
```bash
lsof -i :12345
```

**Check 2: Try the health endpoint**
```bash
curl http://localhost:12345/health
```
If it responds with "Service is running", the app is running!

**Check 3: Look for Java processes**
```bash
ps aux | grep HyperskillAiApplicationKt | grep -v grep
```

### Problem: I closed the terminal but the app is still running
This happens when you run the app in the background with `&`.

**Solution:**
```bash
# Find it
lsof -i :12345

# Kill it
kill <PID>
```

---

## 📁 Project Structure

```
hyperskill_ai/
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── com/
│       │       └── example/
│       │           └── hyperskill_ai/
│       │               ├── HyperskillAiApplication.kt    # Main application class
│       │               └── controller/
│       │                   └── QrController.kt           # REST controller
│       └── resources/
│           └── application.properties                    # Configuration (port 12345)
├── build.gradle.kts                                      # Build configuration
├── settings.gradle.kts                                   # Project settings
├── gradlew                                               # Gradle wrapper (Unix)
├── gradlew.bat                                           # Gradle wrapper (Windows)
└── README.md                                             # This file
```

---

## 📚 Additional Commands

### Run tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests QrControllerTest

# Run with detailed output
./gradlew test --info

# Clean and test
./gradlew clean test
```

### View project dependencies
```bash
./gradlew dependencies
```

### Clean build directory
```bash
./gradlew clean
```

### Check code style (if configured)
```bash
./gradlew check
```

---

## 🧪 Testing

The application includes comprehensive test coverage for all QR code functionality.

**Test Suite:** 21 integration tests  
**Test File:** `src/test/kotlin/com/example/hyperskill_ai/controller/QrControllerTest.kt`  
**Documentation:** See [TEST_DOCUMENTATION.md](TEST_DOCUMENTATION.md) for detailed test descriptions

**Test Coverage:**
- ✅ Health endpoint functionality
- ✅ QR code generation with all parameter combinations
- ✅ QR code file saving functionality
- ✅ Parameter validation (size, type, contents)
- ✅ Error handling and edge cases
- ✅ Image format verification (PNG/JPEG)
- ✅ Boundary testing (min/max values)
- ✅ Special characters and complex URLs
- ✅ File system operations (directory creation, file saving)
- ✅ Unique filename generation

---

## 🎯 Summary of Most Important Commands

| Task | Command |
|------|---------|
| **Start the app** | `./gradlew bootRun` |
| **Stop the app** | Press `Ctrl+C` or `kill <PID>` |
| **Test health** | `curl http://localhost:12345/health` |
| **Generate QR code** | `curl "http://localhost:12345/qr?contents=Hello" -o qr.png` |
| **Save QR to server** | `curl "http://localhost:12345/qr/save?contents=Hello"` |
| **Check if running** | `lsof -i :12345` |
| **Find PID** | `lsof -i :12345` |
| **Kill process** | `kill <PID>` |
| **Build project** | `./gradlew build` |

---

## 💡 Best Practices

1. **Always check if the app is running before starting it again**
   ```bash
   lsof -i :12345
   ```

2. **Use `Ctrl+C` to stop instead of closing the terminal**
   - This cleanly shuts down the application

3. **Clean up before logging off**
   - Check for running processes and kill them

4. **Rebuild after code changes**
   ```bash
   # Stop the app first (Ctrl+C)
   ./gradlew build
   ./gradlew bootRun
   ```

5. **Save resources: don't run multiple instances**
   - One instance is enough for testing

---

## 🆘 Quick Help

**"I don't know if my app is running!"**
```bash
lsof -i :12345 && echo "✅ App is running" || echo "❌ App is not running"
```

**"How do I restart the app?"**
```bash
# Stop it
kill $(lsof -t -i :12345)

# Start it
./gradlew bootRun
```

**"I want to check everything is working"**
```bash
# Start the app
./gradlew bootRun

# In a new terminal, test it
curl http://localhost:12345/health

# Stop it when done (Ctrl+C)
```

---

## 📞 Need More Help?

- Check the [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- Check the [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- Run `./gradlew --help` for Gradle help

---

**Made with ❤️ using Spring Boot + Kotlin**
