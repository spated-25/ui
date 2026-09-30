# Architecture

## Practical choice

Forge Launcher uses one Android activity, Kotlin, Jetpack Compose, and a small MVVM-style separation. A launcher is deeply tied to Android framework APIs, so native Android is more practical than React Native or Flutter for this project.

## Layers

| Layer | Responsibility | Main files |
| --- | --- | --- |
| Platform | Android Home-role handling | `platform/DefaultLauncherManager.kt` |
| Data | Discover, observe, and launch installed apps; save small settings with DataStore | `data/apps/*`, `data/preferences/*` |
| Domain | Stable app model and search rules | `domain/model/*` |
| UI state | Own screen state and coordinate actions | `ui/LauncherViewModel.kt` |
| UI | Home, drawer, settings, reusable icon, glass rendering, theme | `ui/home`, `ui/drawer`, `ui/settings`, `ui/components`, `ui/glass`, `ui/theme` |
| Entry point | Window/wallpaper integration and system activity results | `MainActivity.kt` |

## Important Android APIs

### Home intent and Home role

The manifest declares an activity for `ACTION_MAIN` plus `CATEGORY_HOME` and `CATEGORY_DEFAULT`. That makes Android recognize Forge Launcher as a Home-app candidate. `RoleManager.ROLE_HOME` asks the user to approve it; the app cannot silently make itself the default.

### LauncherApps

`LauncherApps` returns launchable activities, their labels/icons, and visible user profiles. It also notifies the repository when packages change. `startMainActivity` launches the chosen component as the correct user.

### Package visibility

The manifest's `<queries>` block exposes only apps with a normal launcher activity. We intentionally do not request `QUERY_ALL_PACKAGES`.

### Wallpaper

The Home activity uses Android's show-wallpaper window flag and a transparent window background. The UI adds only translucent surfaces and a readability scrim. Android draws the system wallpaper in a separate window, so Haze captures and blurs registered Compose content in Forge's window; it cannot directly copy pixels from the system wallpaper window. Keeping the wallpaper system-owned avoids storage or wallpaper-reading permissions.

### Glass rendering

`ui/glass/GlassSurface.kt` is the one reusable glass implementation. It combines a translucent tint, brighter top border, inner highlight, shadow, and press animation. On Android 13 and newer it also consumes a Haze backdrop made from registered in-window sources. Android 12 and older deliberately skip that blur and use the inexpensive translucent fallback.

Every screen reads the same `GlassSettings` through a Compose composition local. That keeps dock, search, clock, drawer, and app tile call sites small. `GlassOpticalEffect` currently has only `BackdropBlur`; this is the extension point for a later Android 13+ refraction renderer without rewriting every caller.

See [LIQUID_GLASS.md](LIQUID_GLASS.md) for the dependency and rendering details.

### Saved settings

Preferences DataStore saves theme, drawer columns, and glass controls asynchronously. A one-time migration carries forward the version 0.1 theme and column values from `SharedPreferences`. Slider changes update the UI immediately, then the ViewModel debounces disk writes so dragging does not create dozens of writes per second.

### Widgets later

Real third-party widgets require an `AppWidgetHost`, widget-ID allocation, binding permission flow, sizing, persistence, deletion, and restore handling. This is isolated as a later phase so a partial widget implementation cannot destabilize the working launcher.

## State flow

1. `AndroidAppRepository` loads apps off the UI thread.
2. It publishes them through `StateFlow`.
3. `LauncherViewModel` combines app data with drawer/theme/glass/settings state.
4. Compose observes one immutable `LauncherUiState`.
5. UI events call small ViewModel functions; the UI never talks directly to `LauncherApps`.

## Why min SDK 26

Android 8.0 covers the intended phones while letting the foundation use `java.time` directly for the clock/date. Lower support is possible later if there is a real device requirement.

The project compiles with Android SDK Platform 37.2 because Haze 2.0 requires those compiler definitions. This does not change device support: `minSdk` remains 26, and `targetSdk` remains 36 until API-37 behavior changes have been tested on real phones.

## Rules for future changes

- Persist user layout by stable component name plus user/profile identity, not by list position.
- Keep platform APIs behind data/platform classes.
- Keep small preferences in DataStore. Add Room database storage when icon placement and folders arrive; do not stretch preferences into a layout database.
- Add a dependency only when it saves meaningful code or provides tested platform compatibility.
- Migrate saved data when changing layout schemas.
