# SkyScape Weather Widget

Native Android weather widget for Pixel devices, with a slim black-glass design, tree/bird/cloud scene, live weather, forecast, and resizable home-screen layouts.

## Project status

Initial isolated project scaffold. Weather provider and API integration are the next implementation step. No API key is committed.

## Build

Open `SkyScapeWeatherWidget` in Android Studio with Android SDK installed, then build the `app` debug variant.

## Animation note

Android home-screen widgets are system-rendered and updates are throttled. The widget uses lightweight scene updates rather than a continuous high-frequency animation loop to protect battery life.
