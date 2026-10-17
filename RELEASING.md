# Releasing Thoshan Android

Permanent, repeatable release process. **Never change the application ID or the
signing key** — Android updates only install over an existing app when both match.

## Fixed identity (do not change)

| Item | Value |
|------|-------|
| applicationId | `in.lingadevaru.thoshan` |
| Kotlin/namespace package | `lingadevaru.thoshan` |
| App display name | Thoshan |
| Release keystore alias | `thoshan-release` |

The old Google AI Studio package (`com.ai.studio.portfolioassistant.thoshan`) is
abandoned on purpose; it caused the "package conflicts with an existing package"
install error. All releases since v1.4.0 use the new ID above.

## Signing key (outside the repository — never commit it)

Local machine location (gitignored by being outside the repo):

```
~/.config/thoshan-android/thoshan-release.keystore   (the key)
~/.config/thoshan-android/signing.properties          (passwords, chmod 600)
```

`signing.properties` format:

```
THOSHAN_STORE_PASSWORD=<password>
THOSHAN_KEY_ALIAS=thoshan-release
THOSHAN_KEY_PASSWORD=<password>
```

Gradle reads these automatically for local builds. CI overrides them with the
environment variables of the same names, plus `THOSHAN_KEYSTORE_PATH` (see below).

### Backup (do this once, store safely offline)

```bash
# Copy both files to an encrypted backup (USB drive, password manager, etc.)
cp ~/.config/thoshan-android/thoshan-release.keystore /secure/backup/
cp ~/.config/thoshan-android/signing.properties /secure/backup/
```

Losing the keystore means existing installs can never be updated — users would
have to uninstall and reinstall. Keep at least two offline copies.

### Restore on a new machine

```bash
mkdir -p ~/.config/thoshan-android
cp /secure/backup/thoshan-release.keystore ~/.config/thoshan-android/
cp /secure/backup/signing.properties ~/.config/thoshan-android/
chmod 700 ~/.config/thoshan-android && chmod 600 ~/.config/thoshan-android/*
```

## Cutting a release (local, recommended)

```bash
# 1. Bump versionCode (+1) and versionName in app/build.gradle.kts
# 2. Commit the version bump
git add -A && git commit -m "release: v<X.Y.Z>"
# 3. Run the release script (builds, verifies, tags, pushes, publishes)
./scripts/release.sh vX.Y.Z
```

`scripts/release.sh` fails safely: it aborts if the keystore, signing
properties, gh authentication, or the built APK's package/signature are wrong.
It never regenerates the key or changes the application ID.

The APK asset is named `Thoshan-v<X.Y.Z>.apk`.

## Cutting a release (CI, optional)

The workflow `.github/workflows/release.yml` builds a signed release APK when a
`v*` tag is pushed, using these repository **secrets** (Settings → Secrets →
Actions) — set them once with the same values as the local signing files:

- `THOSHAN_KEYSTORE_BASE64` — base64 of the keystore:
  `base64 -w0 ~/.config/thoshan-android/thoshan-release.keystore`
- `THOSHAN_STORE_PASSWORD`
- `THOSHAN_KEY_ALIAS` (`thoshan-release`)
- `THOSHAN_KEY_PASSWORD`

## Versioning rules

- `versionName` = human version (`1.4.0`), `versionCode` = integer, **must increase
  by at least 1 every release** or Android will refuse the update.
- Tag format: `vX.Y.Z`, must match `versionName`. Never reuse or move an existing tag.

## Verifying an APK before publishing

```bash
AAPT=$HOME/Android/Sdk/build-tools/36.0.0/aapt2
$AAPT dump badging Thoshan-vX.Y.Z.apk | head -2   # package + version
$HOME/Android/Sdk/build-tools/36.0.0/apksigner verify --print-certs Thoshan-vX.Y.Z.apk
```

Expected: `package: name='in.lingadevaru.thoshan'`, signer DN `CN=Thoshan, ...`.
