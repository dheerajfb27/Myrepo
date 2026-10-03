# DocuCraft AI Android

Native Android shell for the live DocuCraft AI application.

Backend: https://docucraft-ai.hatchable.site

## GitHub Actions
Pushes affecting docucraft-android automatically build a release APK. You can also run the workflow manually from GitHub Actions.

The APK is uploaded as the workflow artifact:
docucraft-ai-release-apk

## Local build
Open this folder in Android Studio or run:
gradlew.bat assembleRelease

The APK appears at app/build/outputs/apk/release/
