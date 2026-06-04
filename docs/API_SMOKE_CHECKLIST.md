# API Smoke Checklist

Use this checklist before calling the MVP ready for internal alpha. Record device/emulator, Android version, and result for each row.

## Android 8.0-12L

- App launches and onboarding can be completed.
- Scan requests `READ_EXTERNAL_STORAGE` only when the scan starts.
- Denying storage permission still allows own-cache and SAF-granted scans.
- Own-cache clean completes without platform delete prompts.
- No `MANAGE_EXTERNAL_STORAGE`, `INTERNET`, or AccessibilityService behavior appears.

## Android 13

- Scan requests media permissions instead of legacy external storage.
- Denied media permissions produce reduced scan mode, not a blocked app.
- MediaStore scan results appear after granting media access.
- SAF folder grant survives process restart.
- Results selection, ignore-list, and trash UI remain usable.

## Android 14

- Partial media access is reflected as reduced scan mode.
- MediaStore deletion uses Android platform confirmation.
- SAF deletion works only inside granted trees.
- Permanent-delete warning appears before cleanup.
- Foreground service declaration is accepted by the system.

## Android 15/16 Target Behavior

- App installs and launches with target SDK 36.
- Media permissions and partial selection flow match Android system UI.
- Cleanup paths do not request broad storage management.
- Trash restore succeeds for eligible locally cached items.
- Failed or cancelled cleanup attempts are shown without crashing.

## Release Policy Posture

- `./gradlew assembleDebug` passes.
- `./gradlew testDebugUnitTest` passes.
- Manifest policy test passes.
- Known MVP limitations are documented in `docs/MVP_PLAN.md`.
