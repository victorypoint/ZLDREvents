# ZLDR Events

An Android app for the ZLDR (Zwift Long Distance Runners and Riders) community that displays all upcoming ZLDR cycling and running events directly from Zwift, and provides links to easily join events.

> **Warning:** This app uses undocumented Zwift API endpoints that may break without notice if Zwift changes their backend.

## Features

**ZLDR Events** is the simplest way to stay across every upcoming ZLDR ride and run. Sign in once with your Zwift account and the app does the rest — automatically discovering all ZLDR events from Zwift and presenting them in a clean, easy-to-browse list.

- **Cycling & Running tabs** — events are split by sport so you can focus on what matters to you. Tap any event to see the full details: route, duration, distance, subgroups, and sign-up count.
- **Always up to date** — pull the latest events any time with the refresh button. The app queries Zwift directly, so you're always seeing real data, not a cached snapshot.
- **Event detail view** — tap any event to see the complete information: route name, distance, elevation, category breakdown, and how many riders or runners have signed up.
- **Secure sign-in** — your Zwift password is never stored. Only the session tokens are saved, encrypted with AES-256-GCM, and wiped the moment you sign out.

<p align="center">
  <img src="docs/ss_cycling.png" width="22%" alt="Cycling events tab" />
  &nbsp;
  <img src="docs/ss_running.png" width="22%" alt="Running events tab" />
  &nbsp;
  <img src="docs/ss_detail.png" width="22%" alt="Event detail" />
  &nbsp;
  <img src="docs/ss_settings.png" width="22%" alt="Settings" />
</p>

---

## Download

Go to the [Releases](../../releases) page to download the latest APK.

> **Install note:** You will need to allow installation from unknown sources on your Android device. Go to **Settings → Apps → Special app access → Install unknown apps** and enable it for your browser or file manager.

## User Guide

- [User Guide (HTML)](https://victorypoint.github.io/ZLDRevents/docs/userguide.html)
- [User Guide (PDF)](docs/userguide.pdf)

## Requirements

- Android 8.0 (Oreo) or higher
- A Zwift account

---

## First launch

On first launch you will see a sign-in screen. Enter your Zwift account username (or email) and password. The app authenticates against Zwift's OAuth2 endpoint and stores only the tokens — your password is never written to disk.

Tokens are stored in `EncryptedSharedPreferences` (AES-256-GCM) and are wiped when you tap the sign-out button.

## Building

```bash
# Debug APK (Windows)
.\gradlew.bat assembleDebug

# Install on connected device
%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

The Gradle wrapper (`gradlew.bat` and `gradle-wrapper.jar`) is included in the repository.

## Project structure

```
app/src/main/java/com/victorypoint/zldrevents/
├── data/auth/          OAuth2 login, token refresh, encrypted token store
├── data/events/        Events API + repository + DTOs
├── data/model/         Domain models (ZwiftEvent, EventSubgroup, Sport)
├── ui/login/           Login screen + ViewModel
├── ui/events/          Events screen with Cycling / Running tabs
├── ui/detail/          Event detail screen + ViewModel
├── ui/navigation/      Compose NavHost
├── ui/theme/           Material 3 dark theme
├── ZldrApplication.kt  Manual DI wiring
└── MainActivity.kt
```

## API endpoints used

| Purpose | URL |
|---|---|
| Login | `https://secure.zwift.com/auth/realms/zwift/protocol/openid-connect/token` |
| Token refresh | Same URL with `grant_type=refresh_token` |
| Events list | `https://us-or-rly101.zwift.com/api/public/events/upcoming` |
| Event detail | `https://us-or-rly101.zwift.com/api/public/events/{id}` |

`client_id` is `Zwift_Mobile_Link` (from the zwift-client reference implementation).
