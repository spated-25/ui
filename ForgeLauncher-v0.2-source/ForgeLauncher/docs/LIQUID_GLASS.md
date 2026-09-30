# Liquid glass implementation

Forge's glass is inspired by the visual ideas of translucent layered glass; it does not copy Apple's private APIs or add refraction.

## Exact Gradle changes

Add these lines inside the `dependencies { ... }` block in `app/build.gradle.kts`:

```kotlin
implementation("androidx.datastore:datastore-preferences:1.2.1")
implementation("dev.chrisbanes.haze:haze-blur:2.0.0")
```

`haze-blur` already brings in Haze's core module, so a second Haze dependency is unnecessary. We intentionally do not add `haze-glass`: this version uses our own tint, border, highlight, and shadow and does not implement refraction.

Haze 2.0 also requires Android SDK Platform 37.2 at compile time. The module's `android { ... }` block therefore contains:

```kotlin
compileSdk {
    version = release(37) {
        minorApiLevel = 2
    }
}
```

This is a compiler setting, not a phone requirement. `minSdk = 26` still allows Android 8.0+ phones to install Forge, while `targetSdk = 36` keeps the currently tested runtime behavior.

## How it is organized

- `ui/glass/GlassSurface.kt` owns the visual recipe and tap animation.
- `ProvideGlassEnvironment` shares one `HazeState` and the current settings with every glass surface.
- `ForgeLauncherApp.kt` marks background Compose layers with `hazeSource`.
- Dock, search, clock, drawer, and app tiles only call `GlassSurface`; they do not duplicate glass code.
- `LauncherPreferences.kt` stores the controls with Preferences DataStore.

In simple terms, a Haze source says, “this content may be sampled,” and `GlassSurface` says, “blur the registered content behind me.” The surface then adds its own translucent tint, brighter top border, inner highlight, and soft shadow.

## Version behavior

| Phone version | Rendering path |
| --- | --- |
| Android 13+ | Haze backdrop blur plus tint, border, highlight, and shadow |
| Android 12 and older | Translucent tint, border, highlight, and shadow without backdrop blur |

The fallback is intentional. It keeps scrolling and taps responsive on older rendering stacks while preserving the same overall design. It does not request a special permission.

## Saved controls

- **Tint:** preset swatches plus a saturation/brightness field and hue strip
- **Transparency:** controls how much of the background remains visible
- **Blur strength:** `0–40 dp`; affects the Haze path
- **Corner roundness:** `8–40 dp`
- **Style:** Light, Dark, or Auto; Auto follows the launcher theme

The settings preview owns a small colored Haze source, so all changes are visible immediately. The ViewModel updates the screen first and saves after a short debounce, which keeps fast slider drags smooth.

## Wallpaper limitation

Android draws the real system wallpaper in a window behind Forge. Haze can sample registered content inside Forge's window, but not pixels owned by that separate wallpaper window. The glass tint remains translucent so the real wallpaper is still visible; drawer glass can also blur Forge's home content beneath it. Reading and duplicating the wallpaper bitmap would add platform restrictions and permissions, so this foundation does not do that.

## Refraction later

`GlassOpticalEffect` is the extension point. It currently exposes only `BackdropBlur`. A future Android 13+ refraction option can add another implementation and a setting without changing every dock, icon, clock, and search call site.
