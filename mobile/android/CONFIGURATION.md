# Android App Configuration Guide

## Configuring Backend Connection

### Local Development (Emulator)

For connecting Android to the deployed backend:

1. Edit `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"https://api.beautystock.com/api/\"")
   ```

2. Start your backend on port 8080:
   ```bash
   # In the backend directory
   mvn spring-boot:run
   # or
   gradle bootRun
   ```

3. The app will access it via `https://api.beautystock.com/api/`

### Physical Device

1. Find your computer's local IP address:
   ```bash
   # Windows
   ipconfig  # Look for "IPv4 Address"
   
   # Mac/Linux
   ifconfig  # Look for "inet"
   ```

2. Update `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"http://192.168.x.x:8080/\"")
   ```

3. Ensure device and computer are on same network

### Production Server

```kotlin
buildConfigField("String", "BASE_URL", "\"https://your-api-server.com/\"")
```

## Google OAuth Configuration

### Setup Steps (For Future Implementation)

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project
3. Enable Google Sign-In API
4. Create OAuth 2.0 credentials for Android
5. Get your OAuth client ID
6. Configure in backend application.yml:
   ```yaml
   spring:
     security:
       oauth2:
         client:
           registration:
             google:
               client-id: YOUR_CLIENT_ID
               client-secret: YOUR_CLIENT_SECRET
   ```

7. In Android app, use Web Client credentials (not Android)
8. Add to dependency (when ready):
   ```kotlin
   implementation("com.google.android.gms:play-services-auth:20.7.0")
   ```

## Database Configuration

### Room Database (Optional Local Storage)

To enable Room database for offline support:

1. Add to `app/build.gradle.kts`:
   ```kotlin
   implementation("androidx.room:room-runtime:2.6.1")
   kapt("androidx.room:room-compiler:2.6.1")
   ```

2. Create database and entities in `data/local/`

### DataStore (Current Implementation)

Currently using DataStore for token and user preferences storage:
- Secure key-value pairs
- File-based (faster than SharedPreferences)
- Lifecycle-aware

## Logging and Debugging

### Enable Logging

In `network/RetrofitClient.kt`, HttpLoggingInterceptor is configured:
- Debug builds: BODY level (shows full requests/responses)
- Release builds: NONE level

To view logs in Android Studio:
1. Open Logcat tab at bottom
2. Filter by package name: `com.beautystock`
3. Or search specific tags

### Debug Network Calls

Use Android Studio's Network Inspector:
1. Run → Profiler
2. Network section
3. View all HTTP calls with request/response details

## Permissions Management

Required permissions (already in AndroidManifest.xml):
- `INTERNET` - For API calls
- `CAMERA` - For photo capture (future)
- `READ/WRITE_EXTERNAL_STORAGE` - For file access (future)

### Runtime Permissions

For devices running Android 6.0+, implement runtime permission requests:

```kotlin
// Add Accompanist to dependencies
implementation("com.google.accompanist:accompanist-permissions:0.33.2-alpha")

// Use in Composable
val cameraPermissionState = rememberPermissionState(
    android.Manifest.permission.CAMERA
)
```

## Gradle Configuration

### Building Debug APK

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Building Release APK

```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Installing APK

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Emulator Configuration

### Creating AVD (Android Virtual Device)

1. Android Studio → Tools → AVD Manager
2. Create Virtual Device
3. Select Pixel 6 or similar
4. Android API Level 30+ recommended
5. Configuration:
   - RAM: 2GB+
   - Storage: 2GB+
   - GPU: Enabled

### Emulator Performance

Optimize with:
- Hardware acceleration enabled
- GPU rendering enabled
- Sufficient RAM allocation
- SSD storage for AVD files

## Troubleshooting Configuration

### Issue: Cannot connect to backend

**Solution:**
- Verify backend is running
- Check BASE_URL in BuildConfig
- Ensure network connectivity
- Check firewall rules

### Issue: INTERNET permission denied

**Solution:**
- Verify permission in AndroidManifest.xml
- Grant permission on device (Settings → Apps → Permissions)
- Rebuild and reinstall app

### Issue: Token refresh failing

**Solution:**
- Check DataStore implementation
- Verify token storage/retrieval
- Check backend token endpoint
- Clear app data and re-login

## Environment Variables

### Using .local.properties

Create `local.properties` for machine-specific config (not committed):

```properties
sdk.dir=/path/to/android/sdk
ndk.dir=/path/to/android/ndk
org.gradle.jvmargs=-Xmx2048m
```

## Build Optimization

### Shrink Resources

Edit `app/build.gradle.kts` for release:

```kotlin
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
    }
}
```

### Enable ABI Splits

For optimized APKs by architecture:

```kotlin
splits {
    abi {
        enable true
        reset()
        include "armeabi-v7a", "arm64-v8a"
        universalApk true
    }
}
```

## Next Steps

1. [ ] Configure backend URL for your environment
2. [ ] Test API connectivity from emulator/device
3. [ ] Verify authentication flow
4. [ ] Test all features manually
5. [ ] Configure release signing certificate
6. [ ] Setup CI/CD pipeline
7. [ ] Performance testing
8. [ ] Security audit

---

For more help, refer to Android Developer documentation:
- https://developer.android.com/
- https://developer.android.com/jetpack
- https://developer.android.com/compose
