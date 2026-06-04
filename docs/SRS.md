# Software Requirements Specification — Clen

**Project**: Clen (Android Storage Cleaner)
**Package**: `com.zerodev.clen`
**Document Version**: 0.2 (Draft)
**Status**: Planning
**Owner**: zerodev
**Last Updated**: 2026-06-04

---

## 1. Introduction

### 1.1 Purpose
This document specifies the functional and non-functional requirements for **Clen**, a free, native Android storage cleaner application built with Kotlin and Jetpack Compose. It defines scope, constraints, architecture, and a phased delivery plan so engineering, design, and QA share one source of truth.

### 1.2 Problem Statement
Existing Android cleaner apps on the Play Store suffer from three recurring issues:

1. **Paywalls on core features** — scanning is free, actual cleaning is locked behind a subscription.
2. **Aggressive ads** — interstitials, fullscreen video, push notifications.
3. **Misleading claims** — "boost RAM by 200%", "speed up CPU 5x" — most are placebo on modern Android because the OS already manages memory.

Clen targets the gap: a transparent, ad-free, no-paywall, no-fake-claims cleaner that does only what Android actually permits a third-party app to do, and does it well.

### 1.3 Product Vision
> A pocket file janitor. Honest about what it can and cannot clean. Fast. Beautiful. Free. Forever.

### 1.4 Intended Audience
- **Primary**: Android users frustrated with paid cleaners and full storage.
- **Secondary**: Developers/maintainers contributing to or auditing the codebase.
- **Tertiary**: Reviewers (Play Store policy team, security auditors).

### 1.5 Definitions and Acronyms

| Term | Meaning |
|---|---|
| SAF | Storage Access Framework — user-mediated file access |
| MES | `MANAGE_EXTERNAL_STORAGE` — broad all-files access permission |
| MediaStore | Android indexed media/document provider for shared storage access |
| Junk | Files the user is highly likely to want removed (cache, residuals, empty folders, old APKs, screenshots of OTPs, duplicates) |
| Scan | Read-only enumeration of files matching junk heuristics |
| Clean | Deletion of files explicitly confirmed by the user |
| Quick Clean | Single-tap path that scans and presents a pre-selected default set for confirmation |

---

## 2. Scope

### 2.1 In Scope
- Storage scanning across user-accessible sources: indexed MediaStore collections (Downloads, DCIM, Pictures, Movies, Music, Documents where exposed), SAF-granted folders, and Clen's own app-specific cache.
- Detection categories: large files, duplicate files, old downloaded files, residual APKs, empty folders, large media, screenshots older than N days.
- Per-app storage overview with deep-link to system settings (Android does not allow programmatic third-party cache clearing — see §6).
- Own-app cache cleanup (Clen's own `cacheDir`).
- Storage dashboard with totals, free space, breakdown by category.
- Manual file browser for user-selected directories via SAF, with persisted URI grants where Android allows it.
- Scheduled background scan with notification when reclaimable space exceeds a threshold.
- Material 3 expressive UI with light/dark/dynamic theming.
- Localization-ready strings (English first, Indonesian second).

### 2.2 Out of Scope (Explicitly)
- Clearing **other apps'** caches without root. Android has not allowed this since API 23 (2015).
- "RAM Booster" / "CPU Cooler" — these are placebo features on modern Android.
- Antivirus / malware scanning.
- VPN, network speed test, battery saver — separate concerns.
- Cloud backup integration (may be reconsidered in v2).
- Root-only features (kept out to maximize device coverage).
- Ads, analytics SDKs, third-party trackers.
- In-app purchases or subscriptions.

### 2.3 Assumptions
- Device runs Android 8.0 (API 26) or higher. Confirmed by `minSdk = 26` in [app/build.gradle.kts](file:///Users/ahmadsufyan/Documents/personal-project/frontend/android/jetpack-compose/mini-project/clen/app/build.gradle.kts).
- App targets Android 16 / API 36 (`targetSdk = 36`) while preserving Android 15 behavior and restrictions for API 35 devices.
- User is willing to grant storage permission for scanning.
- Distribution via Google Play Store as primary channel; F-Droid as secondary.
- App is single-module to start; modularization deferred until size warrants it.

---

## 3. Stakeholders and User Personas

### 3.1 Primary Persona — "Budi, Storage-Anxious Student"
- Age 21, Android mid-range device, 64 GB storage, perpetually 95% full.
- Downloads memes, WhatsApp media, lecture PDFs.
- Tried two cleaner apps; both demanded payment after scanning.
- Wants: one tap, see what's hogging space, delete safely, no surprises.

### 3.2 Secondary Persona — "Sarah, Privacy-Conscious Professional"
- Refuses apps with ads or trackers.
- Will read permissions carefully; rejects MES if not justified.
- Wants: clear explanation of what each permission does, ability to deny optional ones.

### 3.3 Tertiary Persona — "Andi, Power User"
- Manages files manually, knows directory structure.
- Wants: filtering by extension/size/date, batch operations, no nagging.

---

## 4. Functional Requirements

Each requirement carries a stable ID (`FR-x.y`), priority (M=Must, S=Should, C=Could), and an acceptance criterion.

### 4.1 Onboarding and Permissions

| ID | Priority | Requirement | Acceptance |
|---|---|---|---|
| FR-1.1 | M | First-launch flow explains app purpose in 3 screens before requesting permissions. | User can complete onboarding in under 30 seconds. |
| FR-1.2 | M | Storage permission requested only when user initiates the first scan. | Permission rationale dialog appears before the system prompt. |
| FR-1.3 | M | All optional permissions declined gracefully — app still works in reduced mode. | Denying SAF access still lets the user clean Clen's own cache. |
| FR-1.4 | S | Permission state visible in Settings screen with deep-link to system settings. | Tapping the permission row opens `ACTION_APPLICATION_DETAILS_SETTINGS`. |

### 4.2 Storage Dashboard (Home)

| ID | Priority | Requirement | Acceptance |
|---|---|---|---|
| FR-2.1 | M | Display total storage, used, free in GB with a circular progress indicator. | Values match `StorageManager`/`StatFs` volume output within 1% margin. |
| FR-2.2 | M | Breakdown by category: Apps, Photos, Videos, Audio, Documents, Other. | Each category shows size and last scan time. |
| FR-2.3 | M | Quick Clean CTA prominently displayed. | One tap initiates default scan. |
| FR-2.4 | S | "Last cleaned" timestamp persisted across app restarts. | Stored in DataStore. |
| FR-2.5 | C | Storage trend chart (last 7 days). | Optional; ship after v1. |

### 4.3 Scan Engine

| ID | Priority | Requirement | Acceptance |
|---|---|---|---|
| FR-3.1 | M | Scan must run on a background coroutine; UI remains responsive. | Frame drops during scan < 5%. |
| FR-3.2 | M | User can cancel an in-progress scan. | Cancellation propagates within 200ms. |
| FR-3.3 | M | Scan progress reported as percentage and current category. | Updates at minimum every 500ms. |
| FR-3.4 | M | Scan results persisted in Room DB; resumable across app kills. | Cold-start re-displays previous results until next scan. |
| FR-3.5 | S | Incremental scan — prefer MediaStore `DATE_MODIFIED` and SAF document metadata to re-scan only changed areas. | Subsequent scans complete in < 50% of initial scan time on unchanged storage. |
| FR-3.6 | S | Scheduled scan via WorkManager, default weekly. | Notification fires only if reclaimable > 100 MB. |

### 4.4 Junk Detection Categories

| ID | Priority | Requirement | Acceptance |
|---|---|---|---|
| FR-4.1 | M | **Cache (own app)** — list and clear Clen's own cache directory. | `cacheDir` size shown; clear deletes contents. |
| FR-4.2 | M | **Residual APKs** — `.apk` files in Downloads. | All APKs older than 7 days flagged. |
| FR-4.3 | M | **Large files** — files above user-defined threshold (default 100 MB) from MediaStore and SAF grants. | Sortable by size desc. |
| FR-4.4 | M | **Empty folders** in SAF-granted user-accessible storage. | Recursive empty-folder detection for tree URIs; unavailable folders show a clear grant CTA. |
| FR-4.5 | M | **Old downloads** — files in Downloads older than user-defined days (default 30). | Date filter configurable. |
| FR-4.6 | S | **Duplicate files** — same SHA-256 hash. | Hash-based detection with configurable size threshold to skip tiny files. |
| FR-4.7 | S | **Duplicate photos** — visual similarity (perceptual hash). | aHash/dHash comparison; opt-in. |
| FR-4.8 | S | **Old screenshots** — files in `Pictures/Screenshots` older than N days. | Default 30 days, configurable. |
| FR-4.9 | C | **WhatsApp media cleanup** — surface WA media in scoped paths. | Requires explicit SAF user grant on Android 11+ and must degrade gracefully if WhatsApp paths are inaccessible. |

### 4.5 Per-App Storage View

| ID | Priority | Requirement | Acceptance |
|---|---|---|---|
| FR-5.1 | M | List installed apps with storage breakdown (app size, data, cache) via `StorageStatsManager`. | Requires `PACKAGE_USAGE_STATS` user grant. |
| FR-5.2 | M | Sort by total size, cache size, last used. | Sort persisted. |
| FR-5.3 | M | Tap an app → deep-link to system "App Info" screen. | Clen does NOT silently clear other-app cache (impossible without system signature). |
| FR-5.4 | C | Highlight apps unused for > 90 days. | "Hibernate suggestion" with deep-link. |

### 4.6 Cleaning Flow

| ID | Priority | Requirement | Acceptance |
|---|---|---|---|
| FR-6.1 | M | All deletions require explicit user confirmation. | Confirmation dialog lists count + total size. |
| FR-6.2 | M | Deletions are reversible within 24 hours only when the source URI can be safely copied to local app-internal trash before deletion. | Restorable items show "Restore"; non-restorable items are clearly marked before confirmation. |
| FR-6.3 | M | Files larger than 100 MB or files without reliable read access skip the trash and warn user before permanent delete. | Warning dialog uses explicit "Permanently delete" action text. |
| FR-6.4 | M | User-initiated cleaning runs as a foreground service for runs longer than 10 seconds. | `dataSync` foreground service type with progress notification on Android 14+. |
| FR-6.5 | S | "Whitelist" — user can mark files/folders to exclude from future scans. | Persisted in DataStore. |
| FR-6.6 | M | MediaStore deletion uses platform-mediated delete requests where required. | On API 30+, deletion routes through `MediaStore.createDeleteRequest` or SAF `DocumentsContract` instead of silent raw-file deletes. |

### 4.7 Settings

| ID | Priority | Requirement | Acceptance |
|---|---|---|---|
| FR-7.1 | M | Theme: System / Light / Dark + dynamic color toggle (Android 12+). | Persisted, applies immediately. |
| FR-7.2 | M | Language: English, Bahasa Indonesia. | Switchable without app restart. |
| FR-7.3 | M | Schedule: off / weekly / monthly. | Default off until user opts in. |
| FR-7.4 | M | Thresholds: large file size, old file age. | Sliders with sensible defaults. |
| FR-7.5 | S | Export/import settings (JSON via SAF). | For backup. |
| FR-7.6 | M | About: version, open-source license, link to privacy policy. | Privacy policy must reflect zero-tracking promise. |

---

## 5. Non-Functional Requirements

| ID | Category | Requirement | Target |
|---|---|---|---|
| NFR-1 | Performance | Cold start to interactive | < 1.5s on Pixel 6 |
| NFR-2 | Performance | Initial full scan of 64 GB device | < 60s |
| NFR-3 | Performance | UI frame rate during scan | ≥ 55 fps |
| NFR-4 | Memory | Peak heap during scan | < 150 MB |
| NFR-5 | Battery | One full scan | < 1% battery on a 4000 mAh device |
| NFR-6 | Reliability | No data loss on process kill mid-scan | 100% — Room transactions atomic |
| NFR-7 | Reliability | Crash-free sessions | ≥ 99.5% |
| NFR-8 | Privacy | Network requests | Zero by default. No analytics. No ads. |
| NFR-9 | Security | No telemetry; no PII leaves device. | Verified by audit of dependencies. |
| NFR-10 | Accessibility | TalkBack supported on all screens | All actionable elements labelled. |
| NFR-11 | Accessibility | Min touch target | 48dp |
| NFR-12 | Accessibility | Contrast | WCAG AA |
| NFR-13 | Localization | All user-facing strings in `strings.xml` | No hardcoded strings in Compose. |
| NFR-14 | Compatibility | Android API range | minSdk 26, targetSdk 36, compileSdk 36 |
| NFR-15 | Maintainability | Test coverage on `data` and `domain` layers | ≥ 70% |
| NFR-16 | Maintainability | Public Kotlin API documented | KDoc on all public symbols. |
| NFR-17 | Apk size | Release AAB | < 8 MB |

---

## 6. Platform Constraints (Critical — Read Before Building)

These are not preferences. They are Android platform realities that shape what Clen can and cannot ship.

### 6.1 Cannot Clear Other Apps' Caches
Since **Android 6.0 / API 23 (2015)**, `android.permission.CLEAR_APP_CACHE` is signature-only. A third-party Play-distributed app **cannot** programmatically clear the cache of another app.

**Implication**: Clen's per-app view will deep-link to `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`. The user clears the cache via the system UI. We will be transparent about this. Apps that claim otherwise either use AccessibilityService misuse (Play Store rejection) or require root.

### 6.2 Scoped Storage (Android 11+)
On API 30+, raw access to `/sdcard` is restricted. Clen will:

- Use `MediaStore` for media scanning (Photos, Videos, Audio, Downloads).
- Use **Storage Access Framework (SAF)** with `ACTION_OPEN_DOCUMENT_TREE` for user-selected folders.
- **NOT** request `MANAGE_EXTERNAL_STORAGE` in v1. Google Play tightened policy in August 2024; cleaner apps without strong justification are rejected. We will revisit if user feedback proves the limited mode insufficient — and if so, prepare a Play Console declaration package.
- Handle Android 14+ partial visual-media grants. If the user grants only selected photos/videos, Clen scans only the granted set and explains why broader results require broader permission or SAF.
- Use platform delete flows (`MediaStore.createDeleteRequest` on API 30+) whenever the app lacks direct write access to shared media.

### 6.3 Foreground Service Types (Android 14+)
Long-running cleaning on API 34+ requires declaring an FGS type. Clen will use `dataSync` only for user-initiated cleanup operations exceeding 10 seconds and will stop the service as soon as the operation completes.

### 6.4 Android 15/16 Target Behavior
Because Clen targets API 36, it must be built and tested against Android 15/16 platform behavior:

- Edge-to-edge UI must be supported across onboarding, dialogs, navigation, and long lists.
- Runtime permission UX copy must account for partial media grants and notification opt-in.
- Foreground services, exact alarms, and background work must avoid policy-sensitive behavior unless directly user initiated.
- Play policy documentation must match the declared no-network, no-ads, no-tracking implementation.

### 6.5 Battery and RAM Optimization Claims
Modern Android (Doze, App Standby, low-memory killer) handles RAM and battery optimization at the OS level. **Clen will not implement "RAM boost" or "battery saver" features** because:
1. They produce no measurable benefit on Android 8+.
2. They mislead users.
3. Killing background apps is no longer permitted via public APIs.

### 6.6 Accessibility Service
We will **not** use `AccessibilityService` to automate tapping "Clear Cache" in system settings. This pattern is high-risk for Play Store removal under the "deceptive use of accessibility" policy.

### 6.7 Permission Summary

| Permission | Purpose | When Requested | Optional |
|---|---|---|---|
| `READ_MEDIA_IMAGES` (33+) | Scan photos | First scan | Yes (reduces features) |
| `READ_MEDIA_VIDEO` (33+) | Scan videos | First scan | Yes |
| `READ_MEDIA_AUDIO` (33+) | Scan audio | First scan | Yes |
| `READ_MEDIA_VISUAL_USER_SELECTED` (34+) | Respect partial photo/video access | First scan if visual media requested | Yes |
| `READ_EXTERNAL_STORAGE` (≤32) | Pre-Android 13 fallback | First scan | Yes |
| `POST_NOTIFICATIONS` (33+) | Scheduled-scan notifications | When user enables schedule | Yes |
| `PACKAGE_USAGE_STATS` (special) | Per-app storage view | When user opens that screen | Yes |
| `FOREGROUND_SERVICE` | Long cleanups | At install | Required |
| `FOREGROUND_SERVICE_DATA_SYNC` (34+) | FGS type declaration | At install | Required |
| `RECEIVE_BOOT_COMPLETED` | Re-arm scheduled scans | At install | Only if user enables schedule |
| `INTERNET` | **NOT REQUESTED** | Never | — |

---

## 7. System Architecture

### 7.1 Architectural Style
Clean Architecture, MVI on the presentation layer, single-Activity host with Compose Navigation.

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│  Compose UI → ViewModel (StateFlow) → UI State + Events │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                     Domain Layer                         │
│           UseCases · Models · Repository contracts       │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                      Data Layer                          │
│  Repositories → DataSources (MediaStore, FS, SAF, Room) │
└─────────────────────────────────────────────────────────┘
```

### 7.2 Module Structure (single Gradle module to start)

```
app/src/main/java/com/zerodev/clen/
├── core/             utility, extensions, dispatchers
├── data/
│   ├── local/        Room DB, DataStore
│   ├── system/       MediaStoreDataSource, FileSystemDataSource, StorageStatsDataSource, PackageDataSource
│   └── repository/   ScanRepositoryImpl, AppInfoRepositoryImpl, SettingsRepositoryImpl
├── domain/
│   ├── model/        FileItem, JunkCategory, ScanResult, AppInfo, SettingsModel
│   ├── repository/   ScanRepository, AppInfoRepository, SettingsRepository (interfaces)
│   └── usecase/      ScanStorageUseCase, CleanFilesUseCase, GetAppListUseCase, ...
├── presentation/
│   ├── home/         HomeScreen, HomeViewModel, HomeState
│   ├── scan/         ScanScreen, ScanResultsScreen
│   ├── apps/         AppsScreen
│   ├── settings/     SettingsScreen
│   ├── onboarding/
│   └── common/       reusable Composables, theme tokens
├── service/          CleanForegroundService, ScheduledScanWorker
└── di/               Hilt modules
```

### 7.3 Key Components

- **ScanEngine** — orchestrates parallel category scanners using `flow { }` and `Flow.combine` for progress aggregation.
- **CategoryScanner** — interface with one impl per junk category (LargeFileScanner, ApkScanner, EmptyFolderScanner, DuplicateScanner, OldDownloadScanner, OwnCacheScanner).
- **DeletionExecutor** — copies files to local trash when safely restorable, otherwise performs explicit permanent deletion. Wraps `MediaStore.createDeleteRequest`, `DocumentsContract`, and direct app-cache file paths.
- **StorageProbe** — wraps `StorageStatsManager` and `StorageManager` for whole-device numbers.
- **TrashKeeper** — periodic worker that purges entries older than 24h.

### 7.4 Threading Model
- One IO dispatcher pool sized to `min(8, Runtime.getRuntime().availableProcessors() * 2)`.
- One Default dispatcher for hashing/perceptual-hash CPU work.
- Main dispatcher reserved for state collection only.

---

## 8. Tech Stack

### 8.1 Confirmed (already wired or required)
- **Kotlin** + Coroutines + Flow
- **Jetpack Compose** with Material 3 BOM (already in [app/build.gradle.kts](file:///Users/ahmadsufyan/Documents/personal-project/frontend/android/jetpack-compose/mini-project/clen/app/build.gradle.kts))
- **Compose Navigation** (type-safe, Kotlin Serialization-based)
- **AndroidX Lifecycle** (already present)

### 8.2 To Add
| Library | Purpose | Notes |
|---|---|---|
| `hilt-android` + `hilt-navigation-compose` | DI | Constructor injection across all layers |
| `androidx.room` | Scan-result + trash persistence | KSP processor |
| `androidx.datastore:datastore-preferences` | Settings, last-scan timestamp | |
| `androidx.work:work-runtime-ktx` | Scheduled scans | Hilt-Work integration |
| `coil-compose` | Image previews in duplicate-photo flow | |
| `kotlinx.serialization` | Type-safe nav args, settings export | |
| `androidx.activity:activity-compose` | Already present | Predictive back |
| `accompanist-permissions` *(optional)* | Permission helpers | Or roll own with `rememberLauncherForActivityResult` |
| `mockk` + `turbine` + `kotest-assertions` | Test stack | |

### 8.3 Build Tooling
- AGP 9.x (current catalog: 9.2.1) with `compileSdk 36`
- Kotlin 2.x (Compose Compiler is part of Kotlin since 2.0)
- KSP for Room and Hilt
- Gradle Version Catalogs (`gradle/libs.versions.toml` — already in repo)
- Detekt for static analysis
- Ktlint via Spotless

---

## 9. UI/UX Direction

### 9.1 Design System
- **Material 3 Expressive** — large, friendly, content-first.
- Dynamic color on Android 12+; fallback brand palette (deep teal + warm amber) elsewhere.
- Single-Activity, bottom-navigation host: **Home · Scan · Apps · Settings**.
- Motion: `materialSharedAxisX` for top-level transitions, container transform for list-to-detail.

### 9.2 Key Screens
1. **Onboarding** — 3 cards explaining what Clen does, what it cannot do, and privacy promise.
2. **Home** — storage donut, category breakdown, "Quick Clean" hero CTA, last-cleaned banner.
3. **Scan** — animated progress, current category, cancel button.
4. **Scan Results** — grouped by category, expandable, multi-select, primary action "Clean (X.X GB)".
5. **Apps** — list with size bars, sort menu, tap to system info.
6. **Settings** — theme, language, schedule, thresholds, whitelist, about.
7. **Trash** — recently deleted, restore eligible files, empty trash now.

### 9.3 Empty / Error States
Every screen ships with a designed empty state and a designed error state. No raw stack traces shown to users.

---

## 10. Data Model (sketch)

```kotlin
@Entity data class FileItemEntity(
    @PrimaryKey val uri: String,
    val displayName: String,
    val sizeBytes: Long,
    val mimeType: String?,
    val lastModified: Long,
    val category: JunkCategory,
    val sha256: String?,        // null until duplicate scan runs
    val perceptualHash: Long?,  // null unless image
    val whitelisted: Boolean = false,
)

@Entity data class ScanRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val finishedAt: Long?,
    val totalBytesFound: Long,
    val status: ScanStatus,     // RUNNING, COMPLETED, CANCELLED, FAILED
)

@Entity data class TrashEntryEntity(
    @PrimaryKey val id: String,
    val originalUri: String,
    val cachedPath: String,
    val sizeBytes: Long,
    val deletedAt: Long,
    val expiresAt: Long,        // deletedAt + 24h
    val restorable: Boolean,
)
```

---

## 11. Roadmap (phased delivery)

### Phase 0 — Foundation (week 1)
- DI (Hilt), Room, DataStore, Navigation skeleton.
- Theme tokens, base Composables, onboarding flow.
- Single feature: clear own app cache.
- **Exit criteria**: ship-able APK that does one honest thing.

### Phase 1 — MVP Scan + Clean (weeks 2–3)
- Storage dashboard.
- Scan engine + 5 categories (large files, residual APKs, empty folders, old downloads, own cache).
- Multi-select clean flow with confirmation + trash for eligible files.
- English only.
- **Exit criteria**: internal alpha, full happy path stable.

### Phase 2 — Per-App + Schedule (week 4)
- Per-app storage screen with deep-link.
- WorkManager scheduled scans + notifications.
- Settings: thresholds, schedule, theme.
- **Exit criteria**: closed beta on Play Console.

### Phase 3 — Duplicates + Polish (week 5)
- Duplicate detection (SHA-256).
- Duplicate-photo perceptual hash (opt-in).
- Trash screen.
- Bahasa Indonesia localization.
- **Exit criteria**: open beta.

### Phase 4 — Production (week 6)
- Crash & telemetry decision (likely **none** — preserve zero-tracking promise; rely on Play Console vitals).
- Accessibility audit + WCAG fixes.
- Performance pass against NFR-1 through NFR-5.
- Privacy policy, license screen, store assets.
- **Exit criteria**: production release.

### Phase 5 — Iteration (post-launch)
- Visual duplicate clustering.
- WhatsApp media helper (SAF-based).
- Storage trend chart.
- Localization expansion.
- F-Droid release.

---

## 12. Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Play Store rejects MES request if we add it later | Medium | High | Defer MES indefinitely; rely on MediaStore + SAF. |
| Scan engine slow on 128 GB+ devices | Medium | Medium | Incremental scanning (FR-3.5); benchmark on real devices. |
| Users expect "RAM boost" feature | High | Low | Onboarding screen #2 explicitly explains why we don't ship it. |
| File deletion bug causes user data loss | Low | Critical | 24h trash (FR-6.2); large-file warning (FR-6.3); extensive instrumentation tests. |
| FGS policy violation on API 34+ | Low | High | Strict `dataSync` typing; only run FGS during user-initiated cleanup. |
| AccessibilityService temptation creeps in | Low | Critical | Constraint encoded here in §6.6. |
| Compose Material 3 Expressive APIs still evolving | Low | Low | Pin BOM; abstract custom components in `presentation/common`. |

---

## 13. Success Metrics

These are deliberately product- and quality-focused, not engagement-baiting.

| Metric | Target |
|---|---|
| Crash-free sessions | ≥ 99.5% |
| Avg space reclaimed per active user / week | ≥ 200 MB |
| Quick Clean completion rate | ≥ 80% of users who start a scan |
| Permission grant rate (storage) | ≥ 70% |
| Play Store rating | ≥ 4.5 |
| Negative reviews mentioning "ads" or "paywall" | 0 |

---

## 14. Compliance and Privacy

- **Data collection**: none. No analytics, no crashlytics, no ads SDK.
- **Network**: app does not declare `INTERNET` permission.
- **Play Data Safety form**: declare zero data collection, zero data sharing.
- **Privacy policy**: hosted as a static page; states "Clen collects no data."
- **Open source**: target Apache-2.0 or MIT; publish on GitHub for verifiability.

---

## 15. Open Questions

These need product/owner decisions before Phase 1 closes:

1. Brand name — keep "Clen" or rename for store discoverability?
2. App icon direction — broom, sparkle, abstract?
3. Privacy policy hosting — GitHub Pages or own domain?
4. F-Droid release a v1 goal or later?
5. Visual duplicate detection — opt-in only, or default-on with a prominent toggle?
6. Trash retention — 24h fixed or user-configurable?

---

## 16. Glossary of Files Referenced

- [build.gradle.kts (root)](file:///Users/ahmadsufyan/Documents/personal-project/frontend/android/jetpack-compose/mini-project/clen/build.gradle.kts)
- [app/build.gradle.kts](file:///Users/ahmadsufyan/Documents/personal-project/frontend/android/jetpack-compose/mini-project/clen/app/build.gradle.kts)
- [settings.gradle.kts](file:///Users/ahmadsufyan/Documents/personal-project/frontend/android/jetpack-compose/mini-project/clen/settings.gradle.kts)
- [AndroidManifest.xml](file:///Users/ahmadsufyan/Documents/personal-project/frontend/android/jetpack-compose/mini-project/clen/app/src/main/AndroidManifest.xml)
- [MainActivity.kt](file:///Users/ahmadsufyan/Documents/personal-project/frontend/android/jetpack-compose/mini-project/clen/app/src/main/java/com/zerodev/clen/MainActivity.kt)

---

## 17. Approval

| Role | Name | Date | Signature |
|---|---|---|---|
| Product Owner | zerodev | | |
| Lead Engineer | | | |
| QA Lead | | | |

*This is a living document. Changes after approval are tracked via PR with the `docs/srs` label.*
