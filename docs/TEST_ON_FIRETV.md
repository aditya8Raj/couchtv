# Test CouchTV on Amazon Fire TV Stick

## Option A: Build APK from GitHub Actions (recommended)

1. Push latest code to GitHub.
2. Open repository Actions tab.
3. Run workflow: Build Android Debug APK.
4. Wait for success.
5. Download artifact: couchtv-debug-apk.
6. Extract the artifact zip and keep app-debug.apk.

## Option B: Build APK locally

Prerequisites:

- Android Studio installed
- Android SDK 35 installed

Steps:

1. Open project folder apps/firetv-android in Android Studio.
2. Sync Gradle.
3. Build the app using Build > Build APK(s).
4. Find APK at:
   apps/firetv-android/app/build/outputs/apk/debug/app-debug.apk

## Install APK on Fire TV via ADB

1. On Fire TV:
   - Settings > My Fire TV > About > click Fire TV Stick 7 times to enable Developer Options
   - Settings > My Fire TV > Developer Options > turn ON ADB Debugging
   - Turn ON Install unknown apps for your installer app
2. Ensure Fire TV and your computer are on same Wi-Fi.
3. Find Fire TV IP from Settings > Network.
4. Install ADB on computer.
5. Run commands:
   adb connect FIRE_TV_IP:5555
   adb install -r app-debug.apk
6. Open CouchTV from your Fire TV apps list.

## Current test expectation

- Home screen should load curated channels from remote feed.
- If remote fetch fails, app uses local fallback channels.
- Selecting a channel should open playback screen and start stream.
