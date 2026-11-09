# Ashreinu D-pad Player

A starter Android Studio project for the TCL Flip 2. It currently includes the
Ashreinu playlist **תורת הרבי לפרשת השבוע** (playlist ID 3534)
from the JSON file supplied in this conversation.

## What works in this prototype
- Shows the 32 clips from the supplied playlist.
- Uses the direct MP3 `audio_uri` values in the JSON.
- Plays audio in-app using AndroidX Media3 ExoPlayer (no browser/WebView).
- D-pad Up/Down moves through the list; OK/Center selects and plays.
- D-pad Left/Right seeks backward/forward by 10 seconds.
- Media Play/Pause, Next and Previous key events are handled.

## Open and build
1. Install Android Studio.
2. Open this folder (`AshreinuDpadPlayer`) as an existing project.
3. Let Gradle sync and install the Android SDK Platform 35 if prompted.
4. Connect the TCL Flip 2 with USB debugging enabled.
5. Choose Run, or use Build > Build Bundle(s) / APK(s) > Build APK(s).

The app's `targetSdk` is set to 28 for compatibility with older Android devices.
It requires internet access to stream the MP3 files.

## Current limitation
This is the first working prototype for the one playlist in the supplied JSON.
It does not yet browse every Ashreinu playlist or run as a background playback
service after the app is closed. The supplied playlist JSON is in
`app/src/main/assets/playlist.json`; replace it with another playlist response
to load a different playlist.

## Build an APK automatically on GitHub

This project includes a GitHub Actions workflow at
`.github/workflows/android.yml`.

1. Create a new GitHub repository.
2. Upload the **contents** of this project folder (including the hidden
   `.github` folder) to the repository's `main` branch.
3. Open the repository's **Actions** tab. The workflow builds a debug APK
   automatically on each push and pull request, and can also be started with
   **Run workflow**.
4. When a run finishes, open that run and download the artifact named
   `ashreinu-dpad-player-debug-apk` from the **Artifacts** section.

The APK is a debug build for testing, not a Play Store release. GitHub builds
the APK in the cloud; it does not install it on the phone automatically.
