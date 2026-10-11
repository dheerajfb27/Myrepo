# SkyScape Weather Widget v2

Native Android home-screen weather widget using Kotlin and AppWidgetProvider.

## Current features
- Coffee Glass theme with mocha, cream, and gold accents
- Bengaluru weather from Open-Meteo
- Current temperature, condition, feels-like temperature, wind, humidity, UV index, and pressure
- Seven-day high/low forecast
- Cached weather response for offline fallback
- Tap the widget or refresh icon to update weather

## Build
Requires JDK 17, Android SDK platform 35, Android build tools 35.0.0, and Gradle 8.9.

From this directory run:

```bash
gradle --no-daemon assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

The GitHub Actions workflow builds the debug APK and uploads it as the artifact
`SkyScape-Weather-Widget-v2-debug-apk` for 14 days after a successful run.

## Notes
- The widget uses fixed Bengaluru coordinates for now.
- This repository branch is isolated from the other app projects.
- A successful CI build and installation test must be confirmed before calling the APK release-tested.
