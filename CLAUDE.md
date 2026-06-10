# ZLDR Events — Claude Code Instructions

## Project
Android app that displays upcoming Zwift events filtered by the ZLDR (Zwift Long Distance Runners and Riders) club. Read-only viewer — no joining or calendar features.
Package: com.victorypoint.zldrevents
Version: 1.2.0 (versionCode 3)
Min SDK: 26, Target SDK: 34
Project path: C:/Users/alanu/AndroidStudioProjects/ZLDREvents/

## Build & Deploy
```
.\gradlew.bat assembleDebug        # debug build
.\gradlew.bat assembleRelease      # release build (requires keystore)
```

Always run `.\gradlew.bat assembleDebug` after any changes to confirm clean compile.
Fix all compilation errors before finishing any task.

After any successful `.\gradlew.bat assembleDebug`, automatically install to connected Android device:
```
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

For a release/production build (only when explicitly requested):
```
.\gradlew.bat assembleRelease
```
APK output: app/build/outputs/apk/release/app-release.apk
Install release APK to connected device:
```
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\release\app-release.apk
```

If install succeeds, confirm with device model and ADB output.
If install fails, report the ADB error and stop.
If no device is connected, report and stop — do not retry.

## Gradle / JDK
Gradle wrapper: 8.13 (gradle-wrapper.properties)
AGP: 8.13.2
Kotlin: 2.0.0
JDK: Android Studio JBR at C:/Program Files/Android/Android Studio/jbr (Java 21)
Set in gradle.properties: `org.gradle.java.home=C\:\\Program Files\\Android\\Android Studio\\jbr`
gradle.properties: `org.gradle.jvmargs=-Xmx1536m` (reduced from 2048m to avoid OOM on constrained machines)
gradlew.bat and gradle-wrapper.jar are copied from CyanBridge29 (Android Studio does not generate them on project open — must copy manually or run `gradle wrapper`).

## Architecture
- MVVM with ViewModels + StateFlow sealed UiState classes
- Manual DI via ZldrApplication lazy properties — no Hilt
- Retrofit 2 + OkHttp 4 + Moshi (KSP codegen)
- EncryptedSharedPreferences (security-crypto 1.1.0-alpha06) for token + username storage
- Plain SharedPreferences (AppPrefsStore) for non-sensitive app state (e.g. battery prompt shown flag)
- Jetpack Compose + Material 3 dark theme
- No background scanning or WorkManager — all data is fetched on demand when the app is opened or manually refreshed

## Zwift API
| Purpose | URL |
|---|---|
| Login | https://secure.zwift.com/auth/realms/zwift/protocol/openid-connect/token |
| Token refresh | Same URL, grant_type=refresh_token |
| Events list | https://us-or-rly101.zwift.com/api/public/events/upcoming |
| Event detail (public) | https://us-or-rly101.zwift.com/api/public/events/{id} |
| Event detail + counts (authenticated) | https://us-or-rly101.zwift.com/api/events/{id} |

client_id: `Zwift_Mobile_Link` (from zwift-client reference implementation)
Required headers: `Authorization: Bearer <token>`, `Accept: application/json`, `Zwift-Api-Version: 2.5`
User-Agent: `Zwift/115 CFNetwork/758.0.2 Darwin/15.0.0`

### API Pagination & Discovery
`GET /api/public/events/upcoming?limit=200&start=N` supports numeric offset pagination.
`start=0`, `start=200`, `start=400` each return the next 200 events.
The name-prefix scan fetches up to 5 pages (1000 events max) and stops early when a page returns < 200 events.

**`tags` filter works** — `?tags=zldr` and `?tags=zldriders` return only tagged events and are the primary discovery mechanism.
Other filter params (sport, organizerGroupId, eventStartsAfter, organizerId) are **ignored** by the API.

**Non-public endpoint** `api/events/{id}` (no `/public/` path segment) requires Bearer auth and returns `totalSignedUpCount` — the accurate pre-event sign-up count. The public endpoint always returns 0 for this field.

### API Endpoints That Do NOT Work (for reference)
- `GET /api/clubs/{uuid}/events/upcoming` — 403 (requires Companion app OAuth scope)
- `GET /api/public/clubs/{uuid}/events/upcoming` — 404 (path doesn't exist)
- `POST /api/developer/event/search` — 403 (requires Companion app OAuth scope)
- `GET /api/public/events` (without /upcoming) — 404

### ZLDR Club IDs
- Cycling club: `c5348c4f-77cf-4c30-8815-9af168a02c8e` (from zwift.com/clubs/{uuid}/home)
- Running club: slug `ZLDR` (UUID unknown; zwift.com/clubs/ZLDR/home)
- Note: cycling events (e.g. ZLDR Coffee Mondays) may not appear in the public feed even though the detail endpoint returns them — likely club-only events excluded from the public list.

## ZLDR Discovery Strategy
Two concurrent tag-filtered queries (`?tags=zldr`, `?tags=zldriders`) are the primary source.
A paginated name-prefix scan (events whose name starts with "ZLDR") runs concurrently as a fallback to catch events missing the tag. Both sources are deduplicated by event ID.

After discovery, each event is enriched via `api/events/{id}` (non-public) to obtain accurate
sign-up counts (`totalSignedUpCount`) and fill in any fields missing from the list response.
`EventsRepository` logs how many events had `routeName` populated in the discovery response —
check logcat tag `EventsRepository` after first load to confirm whether the enrichment loop
can be eliminated in a future optimisation.

## Key Files
| File | Purpose |
|------|---------|
| ZldrApplication.kt | Manual DI — all singletons wired here as lazy vals |
| data/AppPrefsStore.kt | Plain SharedPreferences — non-sensitive app state (batteryPromptShown) |
| data/auth/AuthApi.kt | Retrofit interface — separate login() and refresh() methods |
| data/auth/AuthRepository.kt | login (saves username to TokenStore), refresh, logout, sessionExpired flow |
| data/auth/TokenStore.kt | EncryptedSharedPreferences wrapper for tokens + username |
| data/auth/AuthInterceptor.kt | Adds Bearer header, handles 401 → refresh → retry |
| data/events/EventsApi.kt | getUpcomingEvents (tags filter + pagination), getEvent (public detail), getEventWithCounts (non-public detail + counts) |
| data/events/EventsRepository.kt | Tag-filter + name-scan discovery, enrichWithCounts via non-public endpoint, per-sport cache |
| data/events/dto/ | ZwiftEventDto (incl. totalSignedUpCount, totalJoinedCount), EventSubgroupDto |
| data/model/ | ZwiftEvent, EventSubgroup, Sport (domain models + toDomain()) |
| ui/BatteryOptimizationDialog.kt | First-launch dialog prompting battery optimisation exemption (shown once, Samsung-aware) |
| ui/login/ | LoginScreen, LoginViewModel |
| ui/events/ | EventsScreen (tabs + hoisted LazyListState), CyclingTab (contains shared EventTab composable), RunningTab (thin wrapper → EventTab), EventListItem, EventsViewModel |
| ui/detail/ | EventDetailScreen, EventDetailViewModel |
| ui/settings/ | SettingsScreen, SettingsViewModel — sections: About (Help + About dialogs), Zwift Account (username + Log Out), Cache (Clear Cached Data) |
| ui/navigation/ZldrNavGraph.kt | NavHost with login/events/detail/settings routes + sessionExpired redirect + first-launch battery dialog |
| ui/theme/ | Dark M3 theme, ZLDR orange (#FF6B00) as primary |
| docs/userguide.html | User guide — HTML (open in browser) |
| docs/userguide.pdf | User guide — print-ready PDF |

## Scroll Position
LazyListState for each tab is hoisted in EventsScreen (not inside the tab composable).
This preserves scroll position across both tab switches and detail screen navigation.

## Refresh Button
A refresh `IconButton` (`Icons.Default.Refresh`) sits in the TopAppBar to the left of Settings.
While either tab's state is `EventsUiState.Loading`, the button is replaced by a 24dp
`CircularProgressIndicator` in a 48dp Box (same pattern as ZLDREventReporter).
There is no pull-to-refresh — `PullToRefreshBox` was removed.

## First-Launch Battery Optimisation
On first arrival at the events screen after login, `ZldrNavGraph` checks:
1. `AppPrefsStore.batteryPromptShown == false` — prompt not yet shown
2. `PowerManager.isIgnoringBatteryOptimizations()` returns `false` — not already exempted

If both are true, `BatteryOptimizationDialog` is shown. "Open Settings" launches
`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` for the package and marks the prompt shown.
"Skip" also marks it shown. The dialog is never shown again after either action.
Requires `android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` in the manifest.

## App Icon
Launcher icon: `zldr_events.png` (project root) — gold/yellow rounded square with ZLDR EVENTS text and calendar graphic.
Per-density legacy PNGs in `mipmap-*dpi/ic_launcher.png` (48–192 px).
Adaptive icon foreground layers in `drawable-*dpi/ic_launcher_foreground.png` (108–432 px, icon centred in safe zone).
Adaptive icon XMLs in `mipmap-anydpi-v26/ic_launcher.xml` + `ic_launcher_round.xml`.
Background colour: `#EECB58` in `values/colors.xml`.

## Credentials / Security
- Tokens (access_token, refresh_token, expiry) stored in EncryptedSharedPreferences
- Username stored in EncryptedSharedPreferences (set on login, cleared on logout)
- local.properties and *.jks are in .gitignore — never commit them
- Release keystore: `C:/Users/alanu/cyanbridge.jks` — configured in local.properties
  (key alias: `cyanbridge`, same keystore used by CyanBridge29)

## ProGuard (Release)
Release builds use R8 minification. `proguard-rules.pro` includes:
- `-keep` for data/model classes and Moshi adapters
- `-dontwarn okio.**`, `-dontwarn retrofit2.**`
- `-dontwarn com.google.errorprone.annotations.**`, `-dontwarn javax.annotation.**`
  (required to suppress missing-class errors from the `security-crypto` / Tink dependency)

## Do Not
- Do not add Hilt or other DI frameworks
- Do not add crash reporting (no Sentry/Firebase Crashlytics)
- Do not add event join / calendar features (read-only viewer)
- Do not log credentials anywhere
