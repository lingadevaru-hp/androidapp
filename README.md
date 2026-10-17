# Thoshan · Portfolio Assistant — Native Android App

Native Android front end for **Thoshan · Portfolio Assistant** built in Kotlin with Jetpack Compose.
This is a front-end only, offline-capable client that matches the design tokens and interaction model of the web app.

**Version:** 1.3.0
**Last Updated:** October 17, 2026
**Status:** ✅ All features working - Splash screen, smooth transitions, STT/TTS, notifications

---

## 📲 Download the APK (Releases)

The app is distributed as an APK directly from GitHub Releases — no Play Store, no backend required.

### Latest release
👉 **[Download thoshan.apk from Releases →](https://github.com/lingadevaru-hp/androidapp/releases/latest)**

**Version 1.3.0** includes:
- ✅ **Splash Screen** - Smooth fade-in animations with app branding
- ✅ **Smooth Transitions** - Improved navigation and UI transitions
- ✅ **Enhanced STT** - Native Android SpeechRecognizer with better error handling
- ✅ **Improved TTS** - Native Android TextToSpeech with multiple language support
- ✅ **Notification System** - Proper permission handling and user notifications
- ✅ **Bug Fixes** - Fixed compilation errors and runtime issues

### How to install
1. Tap the link above and open the **Releases** page.
2. Under **Assets**, download `app-release.apk`.
3. On your Android phone, open your browser/File Manager and tap the downloaded APK.
4. If prompted, enable **Install unknown apps** for your browser/File Manager, then tap **Install**.
5. Open **Thoshan · Portfolio Assistant** and start chatting.

> Supports Android 8.0 (API 26) through Android 15+ (API 35). No API key, no account, no network needed for voice features.

---

## Features

- **Design Tokens & Palette**: Strict adherence to the dark portfolio theme (`#0a0a0b` background, `#e8703a` orange accent, custom typography and cubic-bezier easing).
- **Custom Lotus Mark**: 5-petal orange emblem drawn dynamically via Compose Canvas with ambient radial glow.
- **Empty State & Suggestion Chips**: Interactive suggestions that trigger instant offline responses.
- **Asymmetric Thread Layout**: Right-aligned user bubbles and un-bubbled assistant markdown text.
- **Thought Chains (`Thoughts ›`)**: Collapsible reasoning lines preceding assistant messages.
- **Action Row & Speaker State Machine**: Speaker state machine (`idle` → `loading` → `speaking` → `paused`) backed by Android TextToSpeech, reaction toggles, and copy-to-clipboard.
- **Mic Voice Recording**: 30-second cap timer, live level meter, and non-auto-sending editable transcript flow.
- **History Drawer**: 288dp slide-in drawer showing chats with relative timestamps, empty-history filtering, and delete confirmations.
- **Search Sheet**: Live search across thread titles and message bodies with highlighted matching substrings.
- **Settings Sheet**: Real-time theme switcher (Dark, Light, System), Indian language reply picker, and DataStore persistence.
- **Call Mode**: Immersive fullscreen `#08080a` voice screen with pulsing lotus orb and transcript display.
- **100% Offline & Front-End Only**: Zero network calls, zero API keys, and secure cleartext traffic disabled.

### Voice (STT + TTS) — no API key, works offline
- **Speech-to-Text**: Native Android `SpeechRecognizer` — built into every Android device. Works offline-ish (uses the device's recognition engine, e.g. Google Speech Recognition).
- **Text-to-Speech**: Native Android `TextToSpeech` — built into every Android device. Works offline-ish (uses the device's TTS voice pack, e.g. Google TTS).
- Both require **zero third-party dependencies** and **zero APK size increase**.

---

## Opening in Android Studio

1. Open Android Studio (Ladybug / Iguana or newer recommended).
2. Select **Open** and choose this project root directory.
3. Allow Gradle to sync dependencies and build the project model.
4. Run on any Android device or emulator running Android 8.0+ (API 26+).

## Building the APK from Command Line

To build the debug APK using Gradle, run:
```bash
./gradlew assembleDebug
```

To build the signed release APK:
```bash
./gradlew assembleRelease
```

## APK Output Location

| Variant | Path |
|---------|------|
| Debug | `app/build/outputs/apk/debug/thoshan.apk` |
| Debug (original) | `app/build/outputs/apk/debug/app-debug.apk` |
| Release | `app/build/outputs/apk/release/app-release.apk` |

> Note: Debug builds are named `thoshan.apk` for easier testing. Release builds use `app-release.apk`.

## Local Testing

To test the APK locally before deployment:
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install app/build/outputs/apk/debug/thoshan.apk
```

The debug APK includes all the latest features and can be installed directly.

## GitHub Actions

Pushing a git tag like `v1.4.0` triggers the **Build & Release APK** workflow (`.github/workflows/release.yml`), which builds the **signed release** APK and attaches it to a GitHub Release as `Thoshan-v1.4.0.apk`.

The full permanent release process (versioning, signing key, backup/restore, CI secrets) is documented in [RELEASING.md](RELEASING.md). To cut a release locally:

```bash
./scripts/release.sh v1.4.0
```

## Requirements

- Android 8.0 (API level 26) or higher
- Microphone permission (granted at first voice use)
- A TTS voice pack installed on your device (Google TTS is pre-installed on most devices)

## Privacy

- No API keys embedded in the APK
- Speech processing happens on-device
- Chat history stays on your device (DataStore)
- No network calls for voice features

---

## Deployment & CI/CD

See **[RELEASING.md](RELEASING.md)** for the permanent release workflow:
versioning rules, the out-of-repo signing key (backup/restore), the local
`scripts/release.sh` path, and the CI secrets setup for tag-triggered releases.

## Troubleshooting

### Gradle Wrapper Issues
If you get errors about `gradlew`:
- Ensure `gradlew` is executable: `chmod +x gradlew`
- Ensure Java 17 is installed
- The wrapper will automatically download Gradle 9.3.1

### Build Errors
If you encounter compilation errors:
- Run `./gradlew clean` then `./gradlew assembleDebug`
- Ensure all Kotlin and Compose dependencies are properly synced

### Release build fails with a keystore error
- The release keystore lives outside the repo at `~/.config/thoshan-android/` — see [RELEASING.md](RELEASING.md) to restore it from backup.

### APK Not Found
After a successful build, check:
- `app/build/outputs/apk/release/app-release.apk` (signed release — installable, updatable)
- `app/build/outputs/apk/debug/app-debug.apk` (debug only)

---

## Repository

- **Source:** `github.com/lingadevaru-hp/androidapp`
- **Releases (APK downloads):** `github.com/lingadevaru-hp/androidapp/releases`
- **Latest Release:** [v1.4.0](https://github.com/lingadevaru-hp/androidapp/releases/tag/v1.4.0)
