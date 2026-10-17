# Thoshan · Portfolio Assistant — Native Android App

Native Android front end for **Thoshan · Portfolio Assistant** built in Kotlin with Jetpack Compose.
This is a front-end only, offline-capable client that matches the design tokens and interaction model of the web app (`ai.lingadevaru.in`).

---

## 📲 Download the APK (Releases)

The app is distributed as an APK directly from GitHub Releases — no Play Store, no backend required.

### Latest release
👉 **[Download app-release.apk from Releases →](https://github.com/lingadevaru-hp/androidapp/releases/latest)**

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
| Debug | `app/build/outputs/apk/debug/app-debug.apk` |
| Release | `app/build/outputs/apk/release/app-release.apk` |

## GitHub Actions

Pushing a git tag like `v1.1.0` triggers the **Build & Release APK** workflow (`.github/workflows/release.yml`), which:
1. Builds the release APK.
2. Creates a GitHub Release tagged `v1.1.0`.
3. Attaches the APK as a release asset for direct download.

### To cut a release locally
```bash
git tag v1.1.0
git push origin v1.1.0
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

## Repository

- Source: `github.com/lingadevaru-hp/androidapp`
- Releases (APK downloads): `github.com/lingadevaru-hp/androidapp/releases`
