# BeautyStock Android - Device Configuration Guide

## Build Flavors

The app now supports 3 different build flavors to work on any device:

### 1. **Emulator Flavor** (For Android Emulator)
**Best for:** Development on Android Studio emulator on your PC
- **API URL:** `https://api.beautystock.com/api/`
- **Command:** `./gradlew installEmulatoreDebug`
- **Usage:** This is the default when running from Android Studio

### 2. **Device Flavor** (For Physical Android Devices)
**Best for:** Running on a real phone/tablet
- **Default API URL:** `https://api.beautystock.com/api/`
- **Command:** `./gradlew installDeviceDebug`

**To configure for your network:**
1. Find your backend machine's IP address:
   - **Windows:** Open Command Prompt and type `ipconfig`
   - **Mac/Linux:** Open Terminal and type `ifconfig`
   - Look for IPv4 address (usually starts with 192.168.x.x or 10.x.x.x)

2. Edit `mobile/android/app/build.gradle.kts`
3. Change the "device" flavor URL:
   ```kotlin
   create("device") {
       dimension = "environment"
       buildConfigField("String", "BASE_URL", "\"http://YOUR_BACKEND_IP:8080/\"")
   }
   ```

### 3. **Production Flavor** (For App Store/Release)
**Best for:** Production deployment
- **API URL:** `https://api.beautystock.com/api/`
- **Command:** `./gradlew installProductionRelease`
- **Note:** Update URL to your actual production server

## Running on Different Devices

### Android Emulator (Default)
```bash
cd mobile/android
./gradlew installEmulatoreDebug
```
Then run the app from Android Studio or:
```bash
adb shell am start -n com.beautystock/.MainActivity
```

### Physical Device (USB Connected)
1. Enable USB Debugging on your Android device:
   - Go to Settings > About Phone
   - Tap Build Number 7 times
   - Go back and enter Developer Options
   - Enable USB Debugging

2. Connect device via USB cable

3. Install app:
   ```bash
   cd mobile/android
   ./gradlew installDeviceDebug
   ```

4. Verify your backend is accessible:
   - On your backend machine, backend should be listening on `0.0.0.0:8080`
   - Both devices (phone and backend machine) must be on the same WiFi network
   - Test with: `curl http://YOUR_BACKEND_IP:8080/v1/auth/register`

### Wireless ADB (No USB Cable)
```bash
# Enable ADB over TCP on device (requires USB once to setup)
adb connect DEVICE_IP:5555

# Then disconnect USB and run:
./gradlew installDeviceDebug
```

## Troubleshooting

### "Connection refused" Error
- **Problem:** Backend not accessible from device
- **Solution:** 
  - Check backend is running: `netstat -an | grep 8080` (Windows) or `lsof -i :8080` (Mac/Linux)
  - Verify correct IP address in build.gradle.kts
  - Check firewall is not blocking port 8080
  - Ensure both devices on same network

### Wrong BASE_URL Being Used
- **Problem:** App still using old URL
- **Solution:**
  - Clean build: `./gradlew clean`
  - Rebuild: `./gradlew installDeviceDebug` (or appropriate flavor)
  - Check `BuildConfig.BASE_URL` in your code

### App Crashes on Startup
- **Problem:** Import errors or compilation issues
- **Solution:**
  - Check Android Logcat for errors
  - Verify all imports are correct
  - Run `./gradlew build` to see compilation errors

## Build Commands Reference

```bash
# Debug builds
./gradlew installEmulatoreDebug    # Emulator
./gradlew installDeviceDebug       # Physical device
./gradlew installProductionDebug   # Production

# Release builds
./gradlew installEmulatorRelease   # Emulator
./gradlew installDeviceRelease     # Physical device
./gradlew installProductionRelease # Production

# Clean and rebuild
./gradlew clean && ./gradlew install[Flavor]Debug
```

## Network Requirements

| Device Type | Network Requirement | Example URL |
|-------------|-------------------|------------|
| Emulator | Public backend | `https://api.beautystock.com/api/` |
| Physical Device | Same WiFi network | `http://192.168.1.5:8080` |
| Production | Internet | `https://api.beautystock.com` |

## API Endpoint Configuration

All endpoints now use the BuildConfig.BASE_URL + `/v1/...` pattern:
- Register: `POST {BASE_URL}/v1/auth/register`
- Login: `POST {BASE_URL}/v1/auth/login`
- Products: `GET {BASE_URL}/v1/products`
- etc.
