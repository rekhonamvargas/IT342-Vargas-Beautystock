# Android Build Configuration

## Gradle Wrapper

To use the Gradle wrapper, run:
```bash
./gradlew build
./gradlew assembleDebug
./gradlew assembleRelease
```

## Build Variants

### Debug Build
- Minification: Disabled
- Debugging: Enabled
- ProGuard: Not applied

### Release Build
- Minification: Enabled
- Debugging: Disabled
- ProGuard: Applied

## Configuration

Edit `app/build.gradle.kts` to:
- Change minimum API level
- Update Kotlin version
- Modify Compose compiler version
- Adjust ProGuard rules in `proguard-rules.pro`

## Dependencies

All dependencies are managed in `app/build.gradle.kts`:
- Core Android libraries
- Jetpack components
- Networking (Retrofit, OkHttp)
- Image loading (Coil)
- And more...

To update dependencies:
```bash
./gradlew dependencyUpdates
```
