# Thoshan · Portfolio Assistant — Native Android App

Native Android front end for **Thoshan · Portfolio Assistant** built in Kotlin with Jetpack Compose.
This is a front-end only, offline-capable client that matches the design tokens and interaction model of the web app (`ai.lingadevaru.in`).

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
(Or `gradle assembleDebug` in local terminal environments).

## APK Output Location
The compiled debug APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```
