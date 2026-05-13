# SnapGallery - Android Gallery App

A modern Android gallery app built with Kotlin and Jetpack Compose to browse, view and manage your photos beautifully.

## Features

- **All Photos View**: Grid display of all device photos
- **Albums**: Browse photos organized by folders/albums
- **Photo Viewer**: Full-screen photo viewing with swipe navigation
- **Material Design 3**: Modern UI with dynamic colors
- **Smooth Performance**: Efficient image loading with Coil

## Tech Stack

| Component | Version |
|-----------|---------|
| Kotlin | 2.3.20 |
| Android Gradle Plugin | 9.2.0 |
| Jetpack Compose BOM | 2026.05.00 |
| Compose UI | 1.7.x |
| Compose Material 3 | 1.3.x |
| Navigation Compose | 2.8.0 |
| Coil | 2.7.0 |
| Lifecycle | 2.8.0 |
| Min SDK | 24 |
| Target SDK | 36 |
| Compile SDK | 36 |

## Project Structure

```
app/
├── src/main/
│   ├── java/com/snapgallery/app/
│   │   ├── data/
│   │   │   ├── model/         # Data classes
│   │   │   └── repository/    # Media repository
│   │   ├── ui/
│   │   │   ├── navigation/    # Navigation setup
│   │   │   ├── screens/      # UI screens
│   │   │   ├── theme/         # Material theme
│   │   │   └── viewmodel/     # ViewModels
│   │   ├── MainActivity.kt
│   │   └── SnapGalleryApp.kt
│   ├── res/
│   │   └── values/            # Strings, colors, themes
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Building

1. Open in Android Studio (Hedgehog or newer)
2. Sync Gradle
3. Run on device/emulator

## Permissions

- `READ_EXTERNAL_STORAGE` (Android < 13)
- `READ_MEDIA_IMAGES` (Android 13+)

## License

MIT License
