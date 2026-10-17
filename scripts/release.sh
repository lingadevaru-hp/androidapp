#!/usr/bin/env bash
# scripts/release.sh — build, verify and publish a signed Thoshan Android release.
#
# Usage: ./scripts/release.sh vX.Y.Z
#
# Fails safely: never regenerates the signing key, never changes the application
# ID, and aborts if any prerequisite (keystore, auth, APK identity) is missing.
set -euo pipefail

VERSION="${1:-}"
[[ "$VERSION" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo "Usage: ./scripts/release.sh vX.Y.Z" >&2; exit 1; }
PLAIN="${VERSION#v}"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

SIGN_DIR="${THOSHAN_SIGNING_DIR:-$HOME/.config/thoshan-android}"
KEYSTORE="${THOSHAN_KEYSTORE_PATH:-$SIGN_DIR/thoshan-release.keystore}"
PROPS="$SIGN_DIR/signing.properties"
APK="app/build/outputs/apk/release/app-release.apk"
ASSET="Thoshan-${VERSION}.apk"
EXPECTED_ID="in.lingadevaru.thoshan"
BUILD_TOOLS="${THOSHAN_BUILD_TOOLS:-$HOME/Android/Sdk/build-tools/36.0.0}"

fail() { echo "ERROR: $*" >&2; exit 1; }

# --- Prerequisites -----------------------------------------------------------
[[ -f "$KEYSTORE" ]] || fail "release keystore not found at $KEYSTORE (see RELEASING.md — never regenerate)"
[[ -f "$PROPS" ]] || fail "signing.properties not found at $PROPS (see RELEASING.md)"
command -v gh >/dev/null || fail "GitHub CLI (gh) not installed"
gh auth status >/dev/null 2>&1 || fail "gh is not authenticated (run: gh auth login)"
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail "not a git repository"
[[ -x ./gradlew ]] || fail "gradlew missing or not executable"

# Version consistency: gradle versionName must equal the tag, versionCode integer.
GRADLE_NAME=$(sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' app/build.gradle.kts | head -1)
GRADLE_CODE=$(sed -n 's/.*versionCode = \([0-9]*\).*/\1/p' app/build.gradle.kts | head -1)
[[ "$GRADLE_NAME" == "$PLAIN" ]] || fail "versionName in app/build.gradle.kts is '$GRADLE_NAME', expected '$PLAIN'"
[[ "$GRADLE_CODE" =~ ^[0-9]+$ ]] || fail "versionCode not found in app/build.gradle.kts"
git rev-parse "v$PLAIN" >/dev/null 2>&1 && fail "tag v$PLAIN already exists locally — never reuse tags"
git ls-remote --exit-code --tags origin "refs/tags/v$PLAIN" >/dev/null 2>&1 && fail "tag v$PLAIN already exists on origin — never reuse tags"

[[ -z "$(git status --porcelain)" ]] || fail "working tree is not clean — commit first"

# --- Build -------------------------------------------------------------------
echo "==> Building release APK (versionName=$GRADLE_NAME versionCode=$GRADLE_CODE)..."
./gradlew assembleRelease --no-daemon
[[ -f "$APK" ]] || fail "APK not produced at $APK"

# --- Verify APK identity (never trust the build config alone) ----------------
AAPT="$BUILD_TOOLS/aapt2"
APKSIGNER="$BUILD_TOOLS/apksigner"
[[ -x "$AAPT" ]] || fail "aapt2 not found at $AAPT"
[[ -x "$APKSIGNER" ]] || fail "apksigner not found at $APKSIGNER"

BADGING=$("$AAPT" dump badging "$APK")
echo "$BADGING" | head -1 | grep -q "name='$EXPECTED_ID'" || fail "APK package is not $EXPECTED_ID"
echo "$BADGING" | grep -q "versionCode='$GRADLE_CODE'" || fail "APK versionCode mismatch"
echo "$BADGING" | grep -q "versionName='$PLAIN'" || fail "APK versionName mismatch"
echo "$BADGING" | grep -q "label='Thoshan'" || fail "APK label is not Thoshan"
"$APKSIGNER" verify "$APK" || fail "APK signature verification failed"
"$APKSIGNER" verify --print-certs "$APK" | grep -q "CN=Thoshan" || fail "APK is not signed with the Thoshan release key"
echo "==> APK verified: $EXPECTED_ID v$PLAIN (code $GRADLE_CODE), signed with release key"

# --- Tag, push, release ------------------------------------------------------
cp "$APK" "$ASSET"
git tag -a "$VERSION" -m "Thoshan Android $VERSION"
git push origin main
git push origin "$VERSION"

echo "==> Creating GitHub release with $ASSET ..."
gh release create "$VERSION" \
  --title "Thoshan Android $VERSION" \
  --notes "Thoshan Android APK $VERSION (application ID: $EXPECTED_ID, versionCode $GRADLE_CODE).

Download \`$ASSET\` and open it on your Android phone to install.
Android 8.0+ required. If upgrading from the old \`com.ai.studio...\` build,
uninstall that app first — this is a new application identity." \
  "$ASSET"

rm -f "$ASSET"
echo "==> Release published: $(gh release view "$VERSION" --json url -q .url)"
