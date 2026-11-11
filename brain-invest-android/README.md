# Brain-invest Android starter

A native Android starter app for a personal Indian-market research workspace.

## Build an APK
1. Open the repository's **Actions** tab.
2. Select **Brain-invest Android APK**.
3. Select **Run workflow** on the `master` branch.
4. When the run succeeds, open it and download the `brain-invest-debug-apk` artifact.
5. Extract the ZIP to get `app-debug.apk`, then install it on your Android phone.

The workflow also runs automatically when files under `brain-invest-android/` change.

## Current scope
This is a UI starter: Overview, Stocks, Mutual Funds, Portfolio and Options sections. It uses explanatory placeholder content. It does **not** yet connect to live NSE/BSE prices, AI services, broker accounts or execute trades. No login or registration is included.

Before real investing use, add a licensed market-data source, secure backend/API-key handling, validated research logic, and clear risk disclosures. Never embed private API keys in the Android app.
