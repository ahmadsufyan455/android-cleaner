# MVP Implementation Plan — Clen

**Project**: Clen (Android Storage Cleaner)
**Package**: `com.zerodev.clen`
**Plan Version**: 0.1
**Status**: Ready for Phase 0
**Last Updated**: 2026-06-04
**Source of Truth**: [SRS.md](SRS.md)

---

## 1. Goal

Build the smallest honest Android storage cleaner that is useful, policy-safe, and technically solid:

- Runs on Android 8.0+ (`minSdk = 26`).
- Targets Android 16 / API 36 (`targetSdk = 36`, `compileSdk = 36`).
- Does not request `MANAGE_EXTERNAL_STORAGE` in v1.
- Uses MediaStore, SAF, and app-private storage only.
- Ships without ads, analytics, trackers, subscriptions, or fake "boost" claims.

The MVP is not "every cleaner feature." The MVP is a stable user journey:

1. User opens Clen.
2. User understands what Clen can and cannot do.
3. User scans allowed storage.
4. User reviews reclaimable files.
5. User deletes only after explicit confirmation.
6. User can restore eligible deleted files from local trash.

---

## 2. Delivery Rules

- Keep the app single-module until the codebase earns modularization.
- Implement domain/data behavior before polishing UI states.
- Every deletion path must be explicit, logged locally, and testable.
- Permission prompts happen only at the moment of need.
- If Android does not allow an action, the app explains and deep-links to system UI instead of pretending.
- No `INTERNET` permission.
- No `AccessibilityService`.
- No `MANAGE_EXTERNAL_STORAGE` for MVP.

---

## 3. MVP Scope

### 3.1 Included

- App foundation: Hilt, Room, DataStore, Compose Navigation, theme shell.
- Onboarding with capability, limitation, and privacy screens.
- Home dashboard with storage totals and last scan/clean state.
- Own-app cache scanner and cleaner.
- MediaStore/SAF scan engine shell.
- MVP junk categories:
  - Own app cache.
  - Residual APKs in Downloads.
  - Large files.
  - Old downloads.
  - Empty folders for SAF-granted trees.
- Scan progress, cancel, and persisted results.
- Results screen with grouped categories and multi-select.
- Clean confirmation, eligible trash, permanent-delete warning.
- Settings for theme and scan thresholds.
- Basic unit tests for domain/data behavior.

### 3.2 Deferred

- Duplicate file detection.
- Duplicate photo visual similarity.
- Per-app storage view.
- Scheduled scans and notifications.
- Bahasa Indonesia localization.
- WhatsApp media helper.
- Storage trend chart.
- Store listing assets.

These are important, but they should not block the first reliable cleaning loop.

---

## 4. Phase Plan

### Phase 0 — Foundation

**Objective**: Prepare the project structure, dependencies, navigation shell, persistence, and first honest feature.

| ID | Status | Task | Depends On | Acceptance |
|---|---|---|---|---|
| MVP-0.1 | Done | Add version catalog entries and Gradle plugins for Hilt, Room, KSP, DataStore, Navigation, Serialization, WorkManager test-ready stack. | None | Project syncs and `assembleDebug` succeeds. |
| MVP-0.2 | Done | Create package structure for `core`, `data`, `domain`, `presentation`, `di`, and `service`. | MVP-0.1 | Source tree matches SRS architecture. |
| MVP-0.3 | Done | Add Hilt application setup and base DI modules for dispatchers. | MVP-0.1 | App launches with `@HiltAndroidApp`; ViewModels can be injected. |
| MVP-0.4 | Done | Add Room database skeleton with `ScanRunEntity`, `FileItemEntity`, and `TrashEntryEntity`. | MVP-0.1 | Room schema compiles; DAO smoke tests pass. |
| MVP-0.5 | Done | Add DataStore settings repository for theme, thresholds, onboarding state, last scan, last clean. | MVP-0.1 | Settings survive process restart. |
| MVP-0.6 | Todo | Add Compose Navigation shell: Home, Scan, Results, Settings, Onboarding. | MVP-0.3 | Back stack behaves predictably; app starts at onboarding until completed. |
| MVP-0.7 | Todo | Build Material 3 theme with dynamic color support and edge-to-edge layout. | MVP-0.6 | Light/dark/system themes render correctly on API 26 and API 36. |
| MVP-0.8 | Todo | Implement own-app cache scanner and cleaner. | MVP-0.4, MVP-0.5 | User can see Clen cache size and clear it without storage permission. |

**Exit Criteria**

- App builds and launches.
- Onboarding can be completed.
- User can clean Clen's own cache.
- No broad storage permission is requested during Phase 0.

---

### Phase 1 — Scan Engine MVP

**Objective**: Build the read-only scan path before introducing shared-storage deletion.

| ID | Status | Task | Depends On | Acceptance |
|---|---|---|---|---|
| MVP-1.1 | Todo | Define domain models: `FileItem`, `JunkCategory`, `ScanRun`, `ScanProgress`, `ScanStatus`, `ScanSource`. | MVP-0.4 | Models are independent from Android framework types where practical. |
| MVP-1.2 | Todo | Define scanner interfaces: `CategoryScanner`, `ScanEngine`, `ScanRepository`. | MVP-1.1 | Scanners can emit progress and results via Flow. |
| MVP-1.3 | Todo | Implement `StorageProbe` using `StorageManager`/`StatFs` for total, used, and free storage. | MVP-0.2 | Dashboard values are within 1% of system values. |
| MVP-1.4 | Todo | Implement media permission coordinator for API 26-36. | MVP-0.6 | Requests correct permission set per API level and handles denial. |
| MVP-1.5 | Todo | Implement SAF tree grant flow with persisted URI permission. | MVP-0.6 | User can grant a folder and see grant state after restart. |
| MVP-1.6 | Todo | Implement residual APK scanner for Downloads. | MVP-1.2, MVP-1.4 | APKs older than 7 days are flagged. |
| MVP-1.7 | Todo | Implement large file scanner for MediaStore and SAF grants. | MVP-1.2, MVP-1.4, MVP-1.5 | Files above threshold appear sorted by size. |
| MVP-1.8 | Todo | Implement old downloads scanner. | MVP-1.2, MVP-1.4 | Downloads older than configured threshold are flagged. |
| MVP-1.9 | Todo | Implement empty folder scanner for SAF tree URIs. | MVP-1.5 | Recursive empty folders are detected only inside granted trees. |
| MVP-1.10 | Todo | Persist scan runs and results in Room. | MVP-1.2, MVP-0.4 | Previous completed scan is visible after app restart. |
| MVP-1.11 | Todo | Add scan cancellation and progress aggregation. | MVP-1.2 | Cancel stops work promptly and leaves a `CANCELLED` scan run. |

**Exit Criteria**

- User can run a scan from Home.
- UI remains responsive during scan.
- User can cancel scan.
- Results persist across process restart.
- Denied permissions degrade into a reduced mode instead of blocking the whole app.

---

### Phase 2 — Review and Clean MVP

**Objective**: Add deletion safely, with user review, clear warnings, and local trash where feasible.

| ID | Status | Task | Depends On | Acceptance |
|---|---|---|---|---|
| MVP-2.1 | Todo | Build scan results screen grouped by category with expandable lists and multi-select. | MVP-1.10 | User can select/deselect files and categories. |
| MVP-2.2 | Todo | Implement whitelist support in DataStore. | MVP-2.1 | Whitelisted files/folders are excluded from later scans. |
| MVP-2.3 | Todo | Define `DeletionExecutor` contract and delete result model. | MVP-2.1 | Delete outcomes distinguish restored-capable, permanent, failed, and skipped. |
| MVP-2.4 | Todo | Implement app-cache deletion path. | MVP-2.3, MVP-0.8 | Own cache clears without platform delete prompts. |
| MVP-2.5 | Todo | Implement local trash copy for eligible files. | MVP-2.3, MVP-0.4 | Restorable files are copied before source deletion and tracked in Room. |
| MVP-2.6 | Todo | Implement permanent-delete warning for large or non-restorable files. | MVP-2.3 | Dialog uses explicit "Permanently delete" action text. |
| MVP-2.7 | Todo | Implement MediaStore delete request path for API 30+. | MVP-2.3 | Shared media deletion uses platform-mediated confirmation when required. |
| MVP-2.8 | Todo | Implement SAF delete path with `DocumentsContract`. | MVP-2.3 | Files in granted trees can be deleted only when URI permission allows it. |
| MVP-2.9 | Todo | Build trash screen with restore eligible files and empty trash now. | MVP-2.5 | Restore works for eligible items; expired trash can be purged. |
| MVP-2.10 | Todo | Add foreground service for user-initiated cleanup exceeding 10 seconds. | MVP-2.3 | API 34+ uses `dataSync` foreground service type. |

**Exit Criteria**

- No file is deleted without explicit confirmation.
- User can distinguish restorable and permanent deletion before cleaning.
- MediaStore and SAF deletion paths use platform-safe APIs.
- Failed deletion attempts are shown without crashing or hiding data.

---

### Phase 3 — MVP UI, Settings, and Polish

**Objective**: Make the MVP feel coherent, accessible, and shippable for internal alpha.

| ID | Status | Task | Depends On | Acceptance |
|---|---|---|---|---|
| MVP-3.1 | Todo | Build Home dashboard with storage totals, scan summary, last cleaned, and Quick Clean CTA. | MVP-1.3, MVP-1.10 | Home reflects scan state accurately. |
| MVP-3.2 | Todo | Build Scan screen with progress, current category, and cancel action. | MVP-1.11 | Progress updates at least every 500ms. |
| MVP-3.3 | Todo | Build Settings screen for theme and thresholds. | MVP-0.5 | Changes apply immediately and persist. |
| MVP-3.4 | Todo | Add permission state rows and deep links to system settings. | MVP-1.4, MVP-1.5 | Permission state is visible and actionable. |
| MVP-3.5 | Todo | Add designed empty and error states for Home, Scan, Results, Settings, Trash. | MVP-2.9 | No raw stack traces or blank states are visible. |
| MVP-3.6 | Todo | Ensure all user-facing text comes from string resources. | MVP-3.5 | Hardcoded user-facing strings are removed from Compose. |
| MVP-3.7 | Todo | Accessibility pass: labels, touch targets, contrast, TalkBack order. | MVP-3.5 | Core flows are usable with TalkBack. |
| MVP-3.8 | Todo | Basic performance pass for scan memory and UI frame stability. | MVP-1.11, MVP-3.2 | Scan does not block UI on a representative device/emulator. |

**Exit Criteria**

- Core MVP flow is usable end to end.
- UI explains reduced functionality clearly.
- Accessibility basics are in place.
- App is ready for internal alpha testing.

---

### Phase 4 — Validation

**Objective**: Prove the MVP is stable enough to continue into beta features.

| ID | Status | Task | Depends On | Acceptance |
|---|---|---|---|---|
| MVP-4.1 | Todo | Add unit tests for scanner filters and threshold behavior. | MVP-1.6, MVP-1.7, MVP-1.8, MVP-1.9 | Tests cover default thresholds and edge cases. |
| MVP-4.2 | Todo | Add unit tests for settings repository. | MVP-0.5 | Theme and thresholds persist correctly. |
| MVP-4.3 | Todo | Add DAO tests for scan results and trash entries. | MVP-0.4, MVP-2.5 | Room insert/query/delete paths pass. |
| MVP-4.4 | Todo | Add deletion executor tests with fake data sources. | MVP-2.3 | Permanent, restorable, skipped, and failed outcomes are covered. |
| MVP-4.5 | Todo | Run API smoke checks on Android 8, Android 13, Android 14, and Android 15/16 target behavior. | MVP-3.8 | Permission and deletion flows match each API level. |
| MVP-4.6 | Todo | Verify manifest policy posture. | MVP-2.10 | No `INTERNET`, no MES, no AccessibilityService; FGS types declared only as needed. |

**Exit Criteria**

- `assembleDebug` succeeds.
- Unit tests pass.
- Manual smoke test passes on representative API levels.
- Known MVP limitations are documented.

---

## 5. Implementation Order

Use this exact order unless a blocker forces a small detour:

1. Phase 0 foundation.
2. Own-app cache feature.
3. Storage dashboard probe.
4. Permission coordinator.
5. SAF grant flow.
6. Scanner interfaces and Room persistence.
7. Residual APK, large file, old downloads, empty folders scanners.
8. Scan UI and results UI.
9. Deletion executor.
10. Trash and restore.
11. Settings and accessibility polish.
12. Tests and manifest validation.

This keeps the first implementation milestone useful even if the broader storage scan takes longer than expected.

---

## 6. Risk Register

| Risk | Impact | MVP Response |
|---|---|---|
| Media permissions behave differently across API 26-36. | User sees missing or confusing results. | Centralize permission coordination and test API-specific branches. |
| SAF tree operations are slower than expected. | Empty-folder scan feels slow. | Keep SAF empty-folder scanning scoped and cancellable. |
| User expects all-app cache clearing. | Trust risk. | Onboarding and per-app copy must clearly state Android does not allow it. |
| Delete flow accidentally hides permanent deletion. | Data-loss risk. | Permanent and non-restorable files require explicit wording. |
| Room entities become coupled to framework types. | Testing and migration friction. | Store URI strings and plain metadata; map framework types at data-source boundaries. |
| MVP expands into v1.5 before it exists. | Delivery delay. | Keep duplicates, schedules, per-app view, and localization deferred until MVP exit criteria pass. |

---

## 7. Definition of Done

A task is done only when:

- It compiles.
- It follows the SRS platform constraints.
- It handles empty, denied, and failure states where relevant.
- It has focused tests if it touches domain, data, scanning, permissions, or deletion behavior.
- It does not introduce network permission, MES, AccessibilityService, ads, analytics, or subscriptions.
- It updates this plan if scope or ordering changes.

---

## 8. First Implementation Sprint

Start here after this plan is accepted:

1. `MVP-0.1` Add dependencies and plugins.
2. `MVP-0.2` Create architecture packages.
3. `MVP-0.3` Add Hilt application setup and dispatcher module.
4. `MVP-0.4` Add Room skeleton.
5. `MVP-0.5` Add DataStore settings repository.
6. `MVP-0.6` Add navigation shell.
7. `MVP-0.8` Implement own-app cache scanner and cleaner.

The first sprint ends with a runnable APK that can complete onboarding and clean Clen's own cache without requesting shared-storage permissions.
