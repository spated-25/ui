# Forge Launcher 0.2

Forge Launcher is a real native Android Home-screen replacement foundation. It is intentionally small enough to understand and safe to extend one feature at a time.

## What already works

- Eligible to become Android's default Home app
- Reads launchable apps for the current user and visible work profiles
- Opens installed apps with the launcher API
- App drawer with live search
- Swipe up to open and swipe down from the drawer header to close
- Wallpaper-aware home screen
- Live clock and localized date
- Four-app starter dock
- Reusable Liquid-Glass-inspired surfaces on the clock, dock, search, app tiles, and drawer
- Live Haze backdrop blur on Android 13 and newer
- Lightweight translucent fallback on Android 12 and older
- Saved glass tint, transparency, blur, corner, and Light/Dark/Auto controls
- Live glass preview in launcher settings
- Light, dark, and system themes
- Four, five, or six app-drawer columns
- System wallpaper picker shortcut
- Reacts to app installs, removals, and changes
- No runtime permissions and no `QUERY_ALL_PACKAGES` permission

## Deliberately not in version 0.2

Home icon rearranging, persistent favorites, folders, widgets, icon packs, notification badges, backup/restore, custom gestures, multiple pages, and glass refraction come next. See [docs/ROADMAP.md](docs/ROADMAP.md).

## Requirements

- Latest stable Android Studio with Android Gradle Plugin 9.4 support
- Android SDK Platform 37.2 installed (the app still targets API 36)
- JDK 17 (Android Studio's bundled JDK is recommended)
- An Android 8.0 or newer phone

## Fast start

1. Extract the project ZIP.
2. Open Android Studio.
3. Choose **Open** and select the extracted `ForgeLauncher` folder.
4. Let Gradle sync finish. Accept the prompt to install Android SDK Platform 37.2 if shown.
5. Connect your phone with USB debugging enabled.
6. Select your phone in the device list and press the green **Run** button.
7. In Forge Launcher, tap **Set** and approve it as the Home app.

Full beginner instructions are in [docs/RUN_ON_PHONE.md](docs/RUN_ON_PHONE.md). The glass component and its exact Gradle dependencies are explained in [docs/LIQUID_GLASS.md](docs/LIQUID_GLASS.md).

For quick testing, the ZIP also contains `dist/ForgeLauncher-v0.2-debug.apk`. It is a development build, not a Play Store release; the Android Studio workflow above is the recommended way to continue development. If you install the supplied APK first, uninstall it before running your own Android Studio build if Android reports an incompatible update—the two computers use different debug signing keys.

## Safety while testing

Do not uninstall your phone's original launcher. If Forge Launcher crashes or you simply want to leave it, open **Settings → Apps → Default apps → Home app** and select the original launcher again.

## Build from a terminal

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The debug APK will be written to `app/build/outputs/apk/debug/app-debug.apk`.

The supplied build and validation results are recorded in [docs/VALIDATION.md](docs/VALIDATION.md).

## Working name and package

- Display name: `Forge Launcher`
- Package: `com.example.forgelauncher`

Both are placeholders. Rename the package with Android Studio's **Refactor → Rename** before publishing.
