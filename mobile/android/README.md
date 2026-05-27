# BeautyStock Android Application

A native Android application built with **Kotlin**, **Jetpack Compose**, and **MVVM** architecture for the BeautyStock beauty inventory management system.

## Overview

BeautyStock Mobile is a native Android app that replicates the core functionality of the web application, providing users with a seamless experience for managing their beauty product inventory on mobile devices.

## Features

### ✅ Implemented

1. **Authentication**
   - Email/Password Registration
   - Email/Password Login
   - Session Management with JWT
   - Token Storage in DataStore (Secure)
   - Role-based Access (Youth/Adult)

2. **Products Management**
   - View all products
   - Search products by name/brand
   - Filter by category
   - Create new products
   - Edit existing products
   - Delete products
   - Product image upload

3. **Favorites**
   - Add/Remove favorite products
   - View favorite products list
   - Check favorite status

4. **Dashboard**
   - Total products count
   - Expiring products count
   - Favorites count
   - Recent products view

5. **User Profile**
   - View user information
   - Edit profile (name, location)
   - Profile image upload
   - Notification settings
   - User role management

6. **Weather-Based Recommendations**
   - Location-based weather fetching
   - Age-specific skincare advice
   - Real-time weather conditions

## Project Structure

```
mobile/android/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/beautystock/
│   │       │   ├── di/                    # Dependency Injection (Future)
│   │       │   ├── model/                 # Data Classes & DTOs
│   │       │   ├── network/               # API Service & HTTP Client
│   │       │   ├── repository/            # Repository Pattern
│   │       │   ├── ui/
│   │       │   │   ├── components/        # Reusable UI Components
│   │       │   │   ├── screens/           # Screen Composables
│   │       │   │   └── theme/             # Material Theme
│   │       │   ├── utils/                 # Utilities & Constants
│   │       │   ├── viewmodel/             # ViewModels & UI State
│   │       │   └── MainActivity.kt        # Entry Point
│   │       ├── res/
│   │       │   ├── drawable/              # Images & Drawables
│   │       │   ├── layout/                # Legacy XML Layouts (if any)
│   │       │   ├── values/                # Colors, Strings, Dimensions
│   │       │   └── xml/                   # Backup & Extraction Rules
│   │       └── AndroidManifest.xml        # App Manifest
│   ├── build.gradle.kts                   # Module-level Build Config
│   └── proguard-rules.pro                 # ProGuard Rules
├── build.gradle.kts                       # Project-level Build Config
├── settings.gradle.kts                    # Gradle Settings
├── gradle.properties                      # Gradle Properties
└── .gitignore                             # Git Ignore Rules
```

## Tech Stack

### Core Android
- **Kotlin** 1.9.21 - Programming Language
- **Android SDK 34** - Latest SDK
- **Jetpack Compose 1.6.2** - Modern UI Framework
- **Material Design 3** - Design System

### Architecture & State Management
- **MVVM** - Model-View-ViewModel
- **ViewModel** - Lifecycle-aware components
- **StateFlow/Flow** - Reactive state management
- **LiveData** - Data binding (alternative)

### Networking
- **Retrofit 2.10.0** - REST API client
- **OkHttp 4.11.0** - HTTP client with interceptors
- **Gson 2.10.1** - JSON serialization
- **Coroutines** - Async operations

### Storage & Preferences
- **DataStore** - Secure key-value storage
- **Room Database** - Local database (optional)
- **Shared Preferences** - App preferences

### Navigation
- **Jetpack Navigation** - Navigation between screens
- **Compose Navigation** - In-app navigation

### Image Loading
- **Coil** - Image loading library

### Permissions
- **Accompanist Permissions** - Permission handling

## Building & Running

### Prerequisites
- Android Studio Iguana (2023.2.1) or newer
- JDK 11 or higher
- Android SDK 24 (minSdk) to 34 (targetSdk)
- Gradle 8.0+

### Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd mobile/android
   ```

2. **Open in Android Studio**
   - File → Open → Select `mobile/android` directory
   - Android Studio will automatically sync Gradle files

3. **Configure Backend URL**
   - Edit `app/build.gradle.kts`
   - Update `BASE_URL` to your backend server
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"http://your-backend-url/\"")
   ```

4. **Run the app**
   - Create an Android Virtual Device (AVD)
   - Click "Run" or press `Shift + F10`
   - Select device/emulator

### Running on Emulator

For API calls, the app is configured to use the public BeautyStock API at `https://api.beautystock.com/api/`.
- Ensure backend is running on port 8080

### Running on Physical Device

1. Enable USB Debugging on device
2. Connect via USB
3. Trust the computer on device
4. Select device in "Running Devices"
5. Click "Run"

## API Integration

All API endpoints are defined in `network/ApiService.kt` using Retrofit.

### Base Configuration
- Base URL: `https://api.beautystock.com/api/`
- API Version: `/v1`
- Authentication: Bearer token in `Authorization` header

### Key Endpoints
- `POST /v1/auth/register` - Register new user
- `POST /v1/auth/login` - User login
- `GET /products` - Get all products
- `POST /products` - Create product
- `GET /recommendations/youth/weather` - Get weather advice
- See `ApiService.kt` for complete list

## State Management

### ViewModel Architecture

Each feature has its own ViewModel:
- `AuthViewModel` - Authentication state
- `ProductViewModel` - Products state
- `DashboardViewModel` - Dashboard data
- `ProfileViewModel` - User profile
- `FavoriteViewModel` - Favorites state
- `WeatherViewModel` - Weather recommendations

### State Models

Each ViewModel manages UI state through sealed classes:
```kotlin
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val auth: AuthResponseDTO) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
```

## Composables & Screens

### Authentication Flow
- `LoginScreen` - Email/password login
- `RegisterScreen` - New user registration with age selection

### Main Application
- `DashboardScreen` - Statistics and overview
- `ProductsScreen` - Product list with search
- `AddProductScreen` - Create new product
- `ProductDetailScreen` - Product details
- `FavoritesScreen` - Favorite products
- `ProfileScreen` - User profile and settings
- `SkincareAdviceScreen` - Weather-based recommendations

### Common Components
- `BeautyTextField` - Styled input fields
- `BeautyButton` - Primary button
- `BeautyOutlinedButton` - Secondary button
- `ErrorDialog` - Error notifications
- `LoadingDialog` - Loading indicator

## Themes & Styling

### Color Palette
- Primary: `#FF7B68EE` (Purple)
- Secondary: `#FF00BCD4` (Cyan)
- Success: `#FF4CAF50` (Green)
- Warning: `#FFFFC107` (Amber)
- Error: `#FFF44336` (Red)

### Spacing System
- XS: 2dp
- SM: 4dp
- MD: 8dp
- LG: 12dp
- XL: 16dp
- XXL: 20dp

### Material Design 3
- Uses Material 3 components
- Supports light/dark theming
- Modern and clean design

## Testing

Unit and integration tests coming soon. Current project is production-ready for manual testing.

## Development Workflow

### Adding a New Feature

1. Create Model/DTO in `model/Models.kt`
2. Add API endpoint in `network/ApiService.kt`
3. Add repository method in `repository/BeautyStockRepository.kt`
4. Create ViewModel in `viewmodel/`
5. Create Screen Composable in `ui/screens/`
6. Add navigation route in `MainApp.kt`

### Best Practices

- Use MVVM architecture consistently
- Leverage Kotlin coroutines for async operations
- Keep UI state in ViewModels, not Composables
- Use StateFlow for reactive state management
- Handle loading and error states gracefully
- Implement proper error handling and user feedback
- Follow Material Design 3 guidelines

## Troubleshooting

### Build Errors
- `Clean Build Folder` → Rebuild
- Check Gradle version compatibility
- Ensure JDK 11+ is installed

### Runtime Errors
- Enable Logcat debugging
- Check backend API connectivity
- Verify token storage and refresh

### Network Issues
- Check firewall rules
- Verify backend URL in BuildConfig
- Ensure INTERNET permission in AndroidManifest

## Future Enhancements

- [ ] Offline mode with Room Database
- [ ] Push notifications
- [ ] Voice search
- [ ] Advanced filtering and sorting
- [ ] Product recommendations ML
- [ ] Barcode scanning
- [ ] Export/import functionality
- [ ] Multi-language support
- [ ] Accessibility improvements
- [ ] Unit and integration tests

## Performance Optimization

- Image lazy loading with Coil
- Efficient list rendering with LazyColumn
- Coroutines for non-blocking operations
- ProGuard obfuscation for release builds
- Data caching where appropriate

## Security

- JWT token storage in secure DataStore
- HTTPS support for API calls
- Token refresh mechanism
- Input validation on client
- Proper error handling to avoid data leaks

## Support & Documentation

For issues, feature requests, or questions:
1. Check existing issues on GitHub
2. Review API documentation
3. Check backend README
4. Contact development team

## License

BeautyStock Mobile is part of the BeautyStock project and follows the same license terms.

## Contributors

- Development Team
- Design Team
- QA Team

---

**Built with ❤️ for BeautyStock Users**
