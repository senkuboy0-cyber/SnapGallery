# SnapGallery - Android Gallery App

A modern Android gallery app built with Kotlin and Jetpack Compose to browse, view and manage your photos beautifully.

## Features

- **All Photos View**: Grid display of all device photos
- **Albums**: Browse photos organized by folders/albums
- **Photo Viewer**: Full-screen photo viewing with swipe navigation
- **Material Design 3**: Modern UI with dynamic colors
- **Smooth Performance**: Efficient image loading with Coil
- **CI/CD**: Automated APK build via GitHub Actions

## Tech Stack

| Component | Version |
|-----------|---------|
| **Kotlin** | 2.3.20 |
| **Android Gradle Plugin** | 9.2.0 |
| **Jetpack Compose BOM** | 2026.05.00 |
| **Compose UI** | 1.7.x |
| **Compose Material 3** | 1.3.x |
| **Navigation Compose** | 2.8.0 |
| **Coil** | 2.7.0 |
| **Lifecycle** | 2.8.0 |
| **Min SDK** | 24 |
| **Target SDK** | 36 |
| **Compile SDK** | 36 |

## Building

### Local Build
1. Open in Android Studio (Hedgehog or newer)
2. Sync Gradle
3. Run on device/emulator

### GitHub Actions (Automated)
The project includes CI/CD pipeline that automatically builds debug APK on:
- Every push to `main` branch
- Every pull request to `main` branch

**Built APK Download:** Go to Actions tab → select workflow run → download artifacts

## Project Structure

```
SnapGallery/
├── .github/
│   └── workflows/
│       └── build.yml          # CI/CD pipeline
├── app/
│   └── src/main/
│       ├── java/com/snapgallery/app/
│       │   ├── data/
│       │   │   ├── model/         # Data classes
│       │   │   └── repository/    # Media repository
│       │   ├── ui/
│       │   │   ├── navigation/    # Navigation setup
│       │   │   ├── screens/       # UI screens
│       │   │   ├── theme/         # Material theme
│       │   │   └── viewmodel/     # ViewModels
│       │   ├── MainActivity.kt
│       │   └── SnapGalleryApp.kt
│       ├── res/
│       │   ├── drawable/          # App icons (vector)
│       │   └── values/            # Strings, colors, themes
│       └── AndroidManifest.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## GitHub Actions Workflow

- **Runner**: ubuntu-latest
- **Java**: JDK 17 (Temurin)
- **Android SDK**: Latest via android-actions/setup-android@v3
- **Build**: Gradle assembleDebug
- **Output**: debug-apk artifact (30 days retention)

## CI/CD Pipeline Versions

| Action | Version |
|--------|---------|
| actions/checkout | v4 |
| actions/setup-java | v4 |
| android-actions/setup-android | v3 |
| actions/upload-artifact | v4 |

## Permissions

- `READ_EXTERNAL_STORAGE` (Android < 13)
- `READ_MEDIA_IMAGES` (Android 13+)

## License

MIT License