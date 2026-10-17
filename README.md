# MyMoney — Android offline finance prototype

Complete, standalone Android Studio / GitHub Actions project. No files from an older repository are required.

## What is included
- Offline WebView Android app displaying the existing MyMoney mobile prototype
- Emerald-and-white dashboard, hidden scrollbars, floating + button
- Accounts, cards with shared limit, transactions, EMI simulation, budgets, goals, reports, settings
- Local browser storage for prototype data (stored on the device)
- GitHub Actions workflow that builds a debug APK

**Architecture:** Kotlin Android activity + bundled HTML/CSS/JavaScript, not a native Jetpack Compose/Room rewrite. No real bank integration, background EMI processing, or OS reminders. The prototype's financial calculations and statement generation are illustrative and not production-grade.

## Create a fresh GitHub repository
1. Extract this ZIP to a folder on your computer.
2. On GitHub, create a new repository called `MyMoney` (do not initialize it with a README if you plan to upload everything at once).
3. Select **uploading an existing file** and upload the **contents** of this extracted folder, preserving folder paths including `.github/workflows/android.yml`.
4. Commit to the `main` branch.
5. Go to **Actions → Build MyMoney APK → Run workflow** (a push to main should also trigger it).
6. Open the successful run and download the **MyMoney-debug-apk** artifact.
7. Unzip that artifact and install `app-debug.apk` on your Android device.

## Build locally
Open the extracted folder in Android Studio with JDK 17 and Android SDK 35. Sync Gradle and use **Build → Build APK(s)**. For a command-line build, install Gradle 8.7 and run `gradle assembleDebug` from the repository root. This archive does not include a Gradle wrapper; GitHub Actions installs Gradle 8.7 automatically.

## Project files
- `app/src/main/assets/mymoney.html`: offline UI and interactive prototype
- `app/src/main/java/com/mymoney/app/MainActivity.kt`: Android WebView container
- `app/src/main/AndroidManifest.xml`: app configuration
- `.github/workflows/android.yml`: APK build pipeline

## Important migration note
Deleting the previous repository removes its history unless you keep a backup. Installing this APK over a previous app with a different signing key may require uninstalling the old app first; uninstalling can delete its local data. Back up anything important before replacing the app.
